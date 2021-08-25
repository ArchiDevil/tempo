package com.cappielloantonio.play.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.cappielloantonio.play.App;
import com.cappielloantonio.play.database.AppDatabase;
import com.cappielloantonio.play.database.dao.RecentSearchDao;
import com.cappielloantonio.play.model.Album;
import com.cappielloantonio.play.model.Artist;
import com.cappielloantonio.play.model.RecentSearch;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.subsonic.models.AlbumID3;
import com.cappielloantonio.play.subsonic.models.ArtistID3;
import com.cappielloantonio.play.subsonic.models.Child;
import com.cappielloantonio.play.subsonic.models.SubsonicResponse;
import com.cappielloantonio.play.util.MappingUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchingRepository {
    private final RecentSearchDao recentSearchDao;
    private final Application application;

    public SearchingRepository(Application application) {
        this.application = application;

        AppDatabase database = AppDatabase.getInstance(application);
        recentSearchDao = database.recentSearchDao();
    }

    public MutableLiveData<List<Song>> getSearchedSongs(String query) {
        MutableLiveData<List<Song>> searchedSongs = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getSearchingClient()
                .search3(query, 20, 0, 0)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        List<Song> songs = new ArrayList<>();

                        if (response.isSuccessful() && response.body() != null && response.body().getSearchResult3() != null) {
                            songs.addAll(MappingUtil.mapSong(response.body().getSearchResult3().getSongs()));
                        }

                        searchedSongs.setValue(songs);
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return searchedSongs;
    }

    public MutableLiveData<List<Album>> getSearchedAlbums(String query) {
        MutableLiveData<List<Album>> searchedAlbums = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getSearchingClient()
                .search3(query, 0, 20, 0)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        List<Album> albums = new ArrayList<>();

                        if (response.isSuccessful() && response.body() != null && response.body().getSearchResult3() != null) {
                            albums.addAll(MappingUtil.mapAlbum(response.body().getSearchResult3().getAlbums()));
                        }

                        searchedAlbums.setValue(albums);
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return searchedAlbums;
    }

    public MutableLiveData<List<Artist>> getSearchedArtists(String query) {
        MutableLiveData<List<Artist>> searchedArtists = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getSearchingClient()
                .search3(query, 0, 0, 20)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        List<Artist> artists = new ArrayList<>();

                        if (response.isSuccessful() && response.body() != null && response.body().getSearchResult3() != null) {
                            artists.addAll(MappingUtil.mapArtist(response.body().getSearchResult3().getArtists()));
                        }

                        searchedArtists.setValue(artists);
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return searchedArtists;
    }

    public MutableLiveData<List<String>> getSuggestions(String query) {
        MutableLiveData<List<String>> suggestions = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getSearchingClient()
                .search3(query, 5, 5, 5)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        List<String> newSuggestions = new ArrayList();

                        if (response.isSuccessful() && response.body() != null) {
                            for (ArtistID3 artistID3 : response.body().getSearchResult3().getArtists()) {
                                newSuggestions.add(artistID3.getName());
                            }

                            for (AlbumID3 albumID3 : response.body().getSearchResult3().getAlbums()) {
                                newSuggestions.add(albumID3.getName());
                            }

                            for (Child song : response.body().getSearchResult3().getSongs()) {
                                newSuggestions.add(song.getTitle());
                            }

                            LinkedHashSet<String> hashSet = new LinkedHashSet<>(newSuggestions);
                            ArrayList<String> suggestionsWithoutDuplicates = new ArrayList<>(hashSet);

                            suggestions.setValue(suggestionsWithoutDuplicates);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return suggestions;
    }

    public void insert(RecentSearch recentSearch) {
        InsertThreadSafe insert = new InsertThreadSafe(recentSearchDao, recentSearch);
        Thread thread = new Thread(insert);
        thread.start();
    }

    public void delete(RecentSearch recentSearch) {
        DeleteThreadSafe delete = new DeleteThreadSafe(recentSearchDao, recentSearch);
        Thread thread = new Thread(delete);
        thread.start();
    }

    public List<String> getRecentSearchSuggestion(int limit) {
        List<String> recent = new ArrayList<>();

        RecentThreadSafe suggestionsThread = new RecentThreadSafe(recentSearchDao, limit);
        Thread thread = new Thread(suggestionsThread);
        thread.start();

        try {
            thread.join();
            recent = suggestionsThread.getRecent();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return recent;
    }

    private static class DeleteThreadSafe implements Runnable {
        private final RecentSearchDao recentSearchDao;
        private final RecentSearch recentSearch;

        public DeleteThreadSafe(RecentSearchDao recentSearchDao, RecentSearch recentSearch) {
            this.recentSearchDao = recentSearchDao;
            this.recentSearch = recentSearch;
        }

        @Override
        public void run() {
            recentSearchDao.delete(recentSearch);
        }
    }

    private static class InsertThreadSafe implements Runnable {
        private final RecentSearchDao recentSearchDao;
        private final RecentSearch recentSearch;

        public InsertThreadSafe(RecentSearchDao recentSearchDao, RecentSearch recentSearch) {
            this.recentSearchDao = recentSearchDao;
            this.recentSearch = recentSearch;
        }

        @Override
        public void run() {
            recentSearchDao.insert(recentSearch);
        }
    }

    private static class RecentThreadSafe implements Runnable {
        private final RecentSearchDao recentSearchDao;
        private final int limit;
        private List<String> recent = new ArrayList<>();

        public RecentThreadSafe(RecentSearchDao recentSearchDao, int limit) {
            this.recentSearchDao = recentSearchDao;
            this.limit = limit;
        }

        @Override
        public void run() {
            recent = recentSearchDao.getRecent(limit);
        }

        public List<String> getRecent() {
            return recent;
        }
    }
}
