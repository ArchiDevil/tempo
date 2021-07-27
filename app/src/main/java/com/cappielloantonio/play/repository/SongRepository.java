package com.cappielloantonio.play.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cappielloantonio.play.App;
import com.cappielloantonio.play.database.AppDatabase;
import com.cappielloantonio.play.database.dao.SongDao;
import com.cappielloantonio.play.database.dao.SongGenreCrossDao;
import com.cappielloantonio.play.interfaces.MediaCallback;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.model.SongGenreCross;
import com.cappielloantonio.play.subsonic.api.albumsonglist.AlbumSongListClient;
import com.cappielloantonio.play.subsonic.api.browsing.BrowsingClient;
import com.cappielloantonio.play.subsonic.models.ResponseStatus;
import com.cappielloantonio.play.subsonic.models.SubsonicResponse;
import com.cappielloantonio.play.util.MappingUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongRepository {
    private static final String TAG = "SongRepository";

    private AlbumSongListClient albumSongListClient;
    private BrowsingClient browsingClient;

    private SongDao songDao;
    private SongGenreCrossDao songGenreCrossDao;
    private LiveData<List<Song>> searchListLiveSongs;
    private LiveData<List<Song>> listLiveSampleDiscoverSongs;
    private LiveData<List<Song>> listLiveSampleRecentlyAddedSongs;
    private LiveData<List<Song>> listLiveSampleRecentlyPlayedSongs;
    private LiveData<List<Song>> listLiveSampleMostPlayedSongs;
    private LiveData<List<Song>> listLiveSampleArtistTopSongs;
    private LiveData<List<Song>> listLiveAlbumSongs;
    private LiveData<List<Song>> listLivePlaylistSongs;
    private LiveData<List<Song>> listLiveSongByGenre;
    private LiveData<List<Song>> listLiveFilteredSongs;
    private LiveData<List<Song>> listLiveSongByYear;
    private LiveData<List<Song>> listLiveSampleFavoritesSong;
    private LiveData<List<Song>> listLiveFavoritesSong;
    private LiveData<List<Song>> listLiveSampleDownloadedSong;
    private LiveData<List<Song>> listLiveDownloadedSong;

    public SongRepository(Application application) {
        albumSongListClient = App.getSubsonicClientInstance(application, false).getAlbumSongListClient();
        browsingClient = App.getSubsonicClientInstance(application, false).getBrowsingClient();

        AppDatabase database = AppDatabase.getInstance(application);
        songDao = database.songDao();
        songGenreCrossDao = database.songGenreCrossDao();
    }

    public MutableLiveData<List<Song>> getSongs() {
        MutableLiveData<List<Song>> liveSongs = new MutableLiveData<>();

        albumSongListClient
                .getRandomSongs(10)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(Call<SubsonicResponse> call, Response<SubsonicResponse> response) {
                        if (response.body().getStatus().getValue().equals(ResponseStatus.OK)) {
                            List<Song> songs = new ArrayList<>(MappingUtil.mapSong(response.body().getRandomSongs().getSongs()));
                            liveSongs.setValue(songs);
                        }
                    }

                    @Override
                    public void onFailure(Call<SubsonicResponse> call, Throwable t) {

                    }
                });

        return liveSongs;
    }

    public void getInstantMix(Song song, int count, MediaCallback callback) {
        browsingClient
                .getSimilarSongs2(song.getId(), count)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(Call<SubsonicResponse> call, Response<SubsonicResponse> response) {
                        if (response.body().getStatus().getValue().equals(ResponseStatus.OK)) {
                            List<Song> songs = new ArrayList<>(MappingUtil.mapSong(response.body().getSimilarSongs2().getSongs()));
                            if(songs.size() > 1) {
                                callback.onLoadMedia(songs);
                            }
                            else {
                                songs.add(song);
                                callback.onLoadMedia(songs);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SubsonicResponse> call, Throwable t) {
                        List<Song> songs = new ArrayList<>();
                        songs.add(song);
                        callback.onLoadMedia(songs);
                    }
                });
    }

    public LiveData<List<Song>> searchListLiveSong(String title, int limit) {
        searchListLiveSongs = songDao.searchSong(title, limit);
        return searchListLiveSongs;
    }

    public LiveData<List<Song>> getListLiveDiscoverSampleSong(List<Integer> pseudoRandomNumber) {
        listLiveSampleDiscoverSongs = songDao.getDiscoverySample(pseudoRandomNumber);
        return listLiveSampleDiscoverSongs;
    }

    public LiveData<List<Song>> getListLiveRecentlyAddedSampleSong(int number) {
        listLiveSampleRecentlyAddedSongs = songDao.getRecentlyAddedSample(number);
        return listLiveSampleRecentlyAddedSongs;
    }

    public LiveData<List<Song>> getListLiveRecentlyPlayedSampleSong(int number) {
        listLiveSampleRecentlyPlayedSongs = songDao.getRecentlyPlayedSample(number);
        return listLiveSampleRecentlyPlayedSongs;
    }

    public LiveData<List<Song>> getListLiveMostPlayedSampleSong(int number) {
        listLiveSampleMostPlayedSongs = songDao.getMostPlayedSample(number);
        return listLiveSampleMostPlayedSongs;
    }

    public LiveData<List<Song>> getListLiveSongByGenre(String genreID) {
        listLiveSongByGenre = songDao.getSongByGenre(genreID);
        return listLiveSongByGenre;
    }

    public LiveData<List<Song>> getArtistListLiveTopSongSample(String artistID) {
        listLiveSampleArtistTopSongs = songDao.getArtistTopSongsSample(artistID, 5);
        return listLiveSampleArtistTopSongs;
    }

    public LiveData<List<Song>> getArtistListLiveTopSong(String artistID) {
        listLiveSampleArtistTopSongs = songDao.getArtistTopSongs(artistID);
        return listLiveSampleArtistTopSongs;
    }

    public List<Song> getArtistListLiveRandomSong(String artistID) {
        List<Song> songs = new ArrayList<>();

        GetRandomSongsByArtistIDThreadSafe randomArtistSongThread = new GetRandomSongsByArtistIDThreadSafe(songDao, artistID, 100);
        Thread thread = new Thread(randomArtistSongThread);
        thread.start();

        try {
            thread.join();
            songs = randomArtistSongThread.getSongs();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return songs;
    }

    public LiveData<List<Song>> getAlbumListLiveSong(String albumID) {
        listLiveAlbumSongs = songDao.getLiveAlbumSong(albumID);
        return listLiveAlbumSongs;
    }

    public LiveData<List<Song>> getPlaylistLiveSong(String playlistID) {
        listLivePlaylistSongs = songDao.getLivePlaylistSong(playlistID);
        return listLivePlaylistSongs;
    }

    public List<Song> getAlbumListSong(String albumID, boolean randomOrder) {
        List<Song> songs = new ArrayList<>();

        GetSongsByAlbumIDThreadSafe suggestionsThread = new GetSongsByAlbumIDThreadSafe(songDao, albumID);
        Thread thread = new Thread(suggestionsThread);
        thread.start();

        try {
            thread.join();
            songs = suggestionsThread.getSongs();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (randomOrder) {
            Collections.shuffle(songs);
        }

        return songs;
    }

    public LiveData<List<Song>> getFilteredListLiveSong(ArrayList<String> filters) {
        listLiveFilteredSongs = songDao.getFilteredSong(filters);
        return listLiveFilteredSongs;
    }

    public List<String> getSearchSuggestion(String query) {
        List<String> suggestions = new ArrayList<>();

        SearchSuggestionsThreadSafe suggestionsThread = new SearchSuggestionsThreadSafe(songDao, query, 5);
        Thread thread = new Thread(suggestionsThread);
        thread.start();

        try {
            thread.join();
            suggestions = suggestionsThread.getSuggestions();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return suggestions;
    }

    /*
     * Funzione che ritorna l'intero set di canzoni.
     * Utilizzato per l'aggiornamento del catalogo.
     */
    public List<Song> getCatalogue() {
        List<Song> catalogue = new ArrayList<>();

        GetCatalogueThreadSafe getCatalogueThread = new GetCatalogueThreadSafe(songDao);
        Thread thread = new Thread(getCatalogueThread);
        thread.start();

        try {
            thread.join();
            catalogue = getCatalogueThread.getCatalogue();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return catalogue;
    }

    public List<Integer> getYearList() {
        List<Integer> years = new ArrayList<>();

        GetYearListThreadSafe getYearListThreadSafe = new GetYearListThreadSafe(songDao);
        Thread thread = new Thread(getYearListThreadSafe);
        thread.start();

        try {
            thread.join();
            years = getYearListThreadSafe.getYearList();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return years;
    }

    public LiveData<List<Song>> getSongByYearListLive(int year) {
        listLiveSongByYear = songDao.getSongsByYear(year);
        return listLiveSongByYear;
    }

    public LiveData<List<Song>> getListLiveFavoritesSampleSong(int number) {
        listLiveSampleFavoritesSong = songDao.getFavoriteSongSample(number);
        return listLiveSampleFavoritesSong;
    }

    public LiveData<List<Song>> getListLiveFavoritesSong() {
        listLiveFavoritesSong = songDao.getFavoriteSong();
        return listLiveFavoritesSong;
    }

    public LiveData<List<Song>> getListLiveDownloadedSampleSong(int number) {
        listLiveSampleDownloadedSong = songDao.getDownloadedSongSample(number);
        return listLiveSampleDownloadedSong;
    }

    public LiveData<List<Song>> getListLiveDownloadedSong() {
        listLiveDownloadedSong = songDao.getDownloadedSong();
        return listLiveDownloadedSong;
    }

    public void insertAll(ArrayList<Song> songs) {
        try {
            final Thread deleteAll = new Thread(new DeleteAllSongThreadSafe(songDao));
            final Thread insertAll = new Thread(new InsertAllThreadSafe(songDao, songGenreCrossDao, songs));

            deleteAll.start();
            deleteAll.join();
            insertAll.start();
            insertAll.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        InsertAllThreadSafe insertAll = new InsertAllThreadSafe(songDao, songGenreCrossDao, songs);
        Thread thread = new Thread(insertAll);
        thread.start();
    }

    public void increasePlayCount(Song song) {
        if (song.nowPlaying()) {
            UpdateThreadSafe update = new UpdateThreadSafe(songDao, song);
            Thread thread = new Thread(update);
            thread.start();
        }
    }

    public void setFavoriteStatus(Song song) {
        UpdateThreadSafe update = new UpdateThreadSafe(songDao, song);
        Thread thread = new Thread(update);
        thread.start();
    }

    public void setOfflineStatus(Song song) {
        UpdateThreadSafe update = new UpdateThreadSafe(songDao, song);
        Thread thread = new Thread(update);
        thread.start();
    }

    public void setAllOffline() {
        SetAllOfflineThreadSafe update = new SetAllOfflineThreadSafe(songDao);
        Thread thread = new Thread(update);
        thread.start();
    }

    public void insertSongPerGenre(ArrayList<SongGenreCross> songGenreCrosses) {
        InsertPerGenreThreadSafe insertPerGenre = new InsertPerGenreThreadSafe(songGenreCrossDao, songGenreCrosses);
        Thread thread = new Thread(insertPerGenre);
        thread.start();
    }

    public void deleteAllSong() {
        DeleteAllSongThreadSafe delete = new DeleteAllSongThreadSafe(songDao);
        Thread thread = new Thread(delete);
        thread.start();
    }

    public void deleteAllSongGenreCross() {
        DeleteAllSongGenreCrossThreadSafe delete = new DeleteAllSongGenreCrossThreadSafe(songGenreCrossDao);
        Thread thread = new Thread(delete);
        thread.start();
    }

    /*public List<Song> getRandomSample(int number) {
        List<Song> sample = new ArrayList<>();

        PickRandomThreadSafe randomThread = new PickRandomThreadSafe(songDao, number);
        Thread thread = new Thread(randomThread);
        thread.start();

        try {
            thread.join();
            sample = randomThread.getSample();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return sample;
    }*/

    public void getRandomSample(int number, MediaCallback callback) {
        albumSongListClient
                .getRandomSongs(number)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(Call<SubsonicResponse> call, Response<SubsonicResponse> response) {
                        List<Song> songs = new ArrayList<>();

                        if (response.body().getStatus().getValue().equals(ResponseStatus.OK)) {
                            songs = new ArrayList<>(MappingUtil.mapSong(response.body().getRandomSongs().getSongs()));
                        }

                        callback.onLoadMedia(songs);
                    }

                    @Override
                    public void onFailure(Call<SubsonicResponse> call, Throwable t) {
                        callback.onError(new Exception(t.getMessage()));
                    }
                });
    }

    public List<Song> getPlaylistSong(String playlistID) {
        List<Song> songs = new ArrayList<>();

        GetSongsByPlaylistIDThreadSafe playlistThread = new GetSongsByPlaylistIDThreadSafe(songDao, playlistID);
        Thread thread = new Thread(playlistThread);
        thread.start();

        try {
            thread.join();
            songs = playlistThread.getSongs();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return songs;
    }

    private static class GetRandomSongsByArtistIDThreadSafe implements Runnable {
        private SongDao songDao;
        private String artistID;
        private int limit;
        private List<Song> songs = new ArrayList<>();

        public GetRandomSongsByArtistIDThreadSafe(SongDao songDao, String artistID, int limit) {
            this.songDao = songDao;
            this.artistID = artistID;
            this.limit = limit;
        }

        @Override
        public void run() {
            songs = songDao.getArtistRandomSongs(artistID, limit);
        }

        public List<Song> getSongs() {
            return songs;
        }
    }

    private static class GetSongsByAlbumIDThreadSafe implements Runnable {
        private SongDao songDao;
        private String albumID;
        private List<Song> songs = new ArrayList<>();

        public GetSongsByAlbumIDThreadSafe(SongDao songDao, String albumID) {
            this.songDao = songDao;
            this.albumID = albumID;
        }

        @Override
        public void run() {
            songs = songDao.getAlbumSong(albumID);
        }

        public List<Song> getSongs() {
            return songs;
        }
    }

    private static class SearchSuggestionsThreadSafe implements Runnable {
        private SongDao songDao;
        private String query;
        private int number;
        private List<String> suggestions = new ArrayList<>();

        public SearchSuggestionsThreadSafe(SongDao songDao, String query, int number) {
            this.songDao = songDao;
            this.query = query;
            this.number = number;
        }

        @Override
        public void run() {
            suggestions = songDao.searchSuggestions(query, number);
        }

        public List<String> getSuggestions() {
            return suggestions;
        }
    }

    private static class GetCatalogueThreadSafe implements Runnable {
        private SongDao songDao;
        private List<Song> catalogue = new ArrayList<>();

        public GetCatalogueThreadSafe(SongDao songDao) {
            this.songDao = songDao;
        }

        @Override
        public void run() {
            catalogue = songDao.getAllList();
        }

        public List<Song> getCatalogue() {
            return catalogue;
        }
    }

    private static class GetYearListThreadSafe implements Runnable {
        private SongDao songDao;
        private List<Integer> years = new ArrayList<>();
        private List<Integer> decades = new ArrayList<>();

        public GetYearListThreadSafe(SongDao songDao) {
            this.songDao = songDao;
        }

        @Override
        public void run() {
            years = songDao.getYearList();

            for (int year : years) {
                if (!decades.contains(year - year % 10)) {
                    decades.add(year);
                }
            }
        }

        public List<Integer> getYearList() {
            return years;
        }

        public List<Integer> getDecadeList() {
            return decades;
        }
    }

    private static class InsertAllThreadSafe implements Runnable {
        private SongDao songDao;
        private SongGenreCrossDao songGenreCrossDao;
        private ArrayList<Song> songs;

        public InsertAllThreadSafe(SongDao songDao, SongGenreCrossDao songGenreCrossDao, ArrayList<Song> songs) {
            this.songDao = songDao;
            this.songGenreCrossDao = songGenreCrossDao;
            this.songs = songs;
        }

        @Override
        public void run() {
            songDao.insertAll(songs);
        }
    }

    private static class UpdateThreadSafe implements Runnable {
        private SongDao songDao;
        private Song song;

        public UpdateThreadSafe(SongDao songDao, Song song) {
            this.songDao = songDao;
            this.song = song;
        }

        @Override
        public void run() {
            songDao.update(song);
        }
    }

    private static class SetAllOfflineThreadSafe implements Runnable {
        private SongDao songDao;

        public SetAllOfflineThreadSafe(SongDao songDao) {
            this.songDao = songDao;
        }

        @Override
        public void run() {
            songDao.updateAllOffline();
        }
    }

    private static class InsertPerGenreThreadSafe implements Runnable {
        private SongGenreCrossDao songGenreCrossDao;
        private ArrayList<SongGenreCross> cross;

        public InsertPerGenreThreadSafe(SongGenreCrossDao songGenreCrossDao, ArrayList<SongGenreCross> cross) {
            this.songGenreCrossDao = songGenreCrossDao;
            this.cross = cross;
        }

        @Override
        public void run() {
            songGenreCrossDao.insertAll(cross);
        }
    }

    private static class DeleteAllSongThreadSafe implements Runnable {
        private SongDao songDao;

        public DeleteAllSongThreadSafe(SongDao songDao) {
            this.songDao = songDao;
        }

        @Override
        public void run() {
            songDao.deleteAll();
        }
    }

    private static class DeleteAllSongGenreCrossThreadSafe implements Runnable {
        private SongGenreCrossDao songGenreCrossDao;

        public DeleteAllSongGenreCrossThreadSafe(SongGenreCrossDao songGenreCrossDao) {
            this.songGenreCrossDao = songGenreCrossDao;
        }

        @Override
        public void run() {
            songGenreCrossDao.deleteAll();
        }
    }

    private static class PickRandomThreadSafe implements Runnable {
        private SongDao songDao;
        private int elementNumber;
        private List<Song> sample;

        public PickRandomThreadSafe(SongDao songDao, int number) {
            this.songDao = songDao;
            this.elementNumber = number;
        }

        @Override
        public void run() {
            sample = songDao.random(elementNumber);
        }

        public List<Song> getSample() {
            return sample;
        }
    }

    private static class GetSongsByPlaylistIDThreadSafe implements Runnable {
        private SongDao songDao;
        private String playlistID;
        private List<Song> songs = new ArrayList<>();

        public GetSongsByPlaylistIDThreadSafe(SongDao songDao, String playlistID) {
            this.songDao = songDao;
            this.playlistID = playlistID;
        }

        @Override
        public void run() {
            songs = songDao.getPlaylistSong(playlistID);
        }

        public List<Song> getSongs() {
            return songs;
        }
    }
}
