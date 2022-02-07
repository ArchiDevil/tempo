package com.cappielloantonio.play.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.cappielloantonio.play.App;
import com.cappielloantonio.play.interfaces.MediaCallback;
import com.cappielloantonio.play.model.Media;
import com.cappielloantonio.play.subsonic.models.SubsonicResponse;
import com.cappielloantonio.play.util.MappingUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongRepository {
    private static final String TAG = "SongRepository";

    private final Application application;

    public SongRepository(Application application) {
        this.application = application;
    }

    public MutableLiveData<List<Media>> getStarredSongs(boolean random, int size) {
        MutableLiveData<List<Media>> starredSongs = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getAlbumSongListClient()
                .getStarred2()
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getStarred2() != null) {
                            List<Media> songs = new ArrayList<>(MappingUtil.mapSong(response.body().getStarred2().getSongs()));

                            if (!random) {
                                starredSongs.setValue(songs);
                            } else {
                                Collections.shuffle(songs);
                                starredSongs.setValue(songs.subList(0, Math.min(size, songs.size())));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return starredSongs;
    }

    public void getInstantMix(Media song, int count, MediaCallback callback) {
        App.getSubsonicClientInstance(application, false)
                .getBrowsingClient()
                .getSimilarSongs2(song.getId(), count)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getSimilarSongs2() != null) {
                            List<Media> songs = new ArrayList<>(MappingUtil.mapSong(response.body().getSimilarSongs2().getSongs()));

                            if (songs.size() <= 1) {
                                songs.add(song);
                            }

                            callback.onLoadMedia(songs);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {
                        List<Media> songs = new ArrayList<>();
                        songs.add(song);
                        callback.onLoadMedia(songs);
                    }
                });
    }

    public MutableLiveData<List<Media>> getRandomSample(int number, Integer fromYear, Integer toYear) {
        MutableLiveData<List<Media>> randomSongsSample = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getAlbumSongListClient()
                .getRandomSongs(number, fromYear, toYear)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        List<Media> songs = new ArrayList<>();

                        if (response.isSuccessful() && response.body() != null && response.body().getRandomSongs() != null) {
                            songs.addAll(MappingUtil.mapSong(response.body().getRandomSongs().getSongs()));
                        }

                        randomSongsSample.setValue(songs);
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return randomSongsSample;
    }

    public void scrobble(String id) {
        App.getSubsonicClientInstance(application, false)
                .getMediaAnnotationClient()
                .scrobble(id)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {

                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });
    }

    public void star(String id) {
        App.getSubsonicClientInstance(application, false)
                .getMediaAnnotationClient()
                .star(id, null, null)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {

                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });
    }

    public void unstar(String id) {
        App.getSubsonicClientInstance(application, false)
                .getMediaAnnotationClient()
                .unstar(id, null, null)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {

                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });
    }

    public void setRating(String id, int rating) {
        App.getSubsonicClientInstance(application, false)
                .getMediaAnnotationClient()
                .setRating(id, rating)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {

                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });
    }

    public MutableLiveData<List<Media>> getSongsByGenre(String id) {
        MutableLiveData<List<Media>> songsByGenre = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getAlbumSongListClient()
                .getSongsByGenre(id, 500, 0)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getSongsByGenre() != null) {
                            List<Media> newSongs = new ArrayList<>(MappingUtil.mapSong(response.body().getSongsByGenre().getSongs()));
                            List<Media> songs = songsByGenre.getValue();

                            if (songs == null) songs = new ArrayList<>();
                            songs.addAll(newSongs);
                            Collections.shuffle(songs);

                            LinkedHashSet<Media> hashSet = new LinkedHashSet<>(songs);
                            ArrayList<Media> songsWithoutDuplicates = new ArrayList<>(hashSet);

                            songsByGenre.setValue(songsWithoutDuplicates);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return songsByGenre;
    }

    public MutableLiveData<List<Media>> getSongsByGenres(ArrayList<String> genresId) {
        MutableLiveData<List<Media>> songsByGenre = new MutableLiveData<>();

        for (String id : genresId)
            App.getSubsonicClientInstance(application, false)
                    .getAlbumSongListClient()
                    .getSongsByGenre(id, 500, 0)
                    .enqueue(new Callback<SubsonicResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                            List<Media> songs = new ArrayList<>();

                            if (response.isSuccessful() && response.body() != null && response.body().getSongsByGenre() != null) {
                                songs.addAll(MappingUtil.mapSong(response.body().getSongsByGenre().getSongs()));
                            }

                            songsByGenre.setValue(songs);
                        }

                        @Override
                        public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                        }
                    });

        return songsByGenre;
    }

    public MutableLiveData<Media> getSong(String id) {
        MutableLiveData<Media> song = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getBrowsingClient()
                .getSong(id)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            song.setValue(MappingUtil.mapSong(response.body().getSong()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return song;
    }

    public MutableLiveData<String> getSongLyrics(Media song) {
        MutableLiveData<String> lyrics = new MutableLiveData<>(null);

        App.getSubsonicClientInstance(application, false)
                .getMediaRetrievalClient()
                .getLyrics(song.getArtistName(), song.getTitle())
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getLyrics() != null) {
                            lyrics.setValue(response.body().getLyrics().getContent());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return lyrics;
    }
}
