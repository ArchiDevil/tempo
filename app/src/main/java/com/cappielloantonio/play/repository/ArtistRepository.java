package com.cappielloantonio.play.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.cappielloantonio.play.App;
import com.cappielloantonio.play.interfaces.MediaCallback;
import com.cappielloantonio.play.model.Album;
import com.cappielloantonio.play.model.Artist;
import com.cappielloantonio.play.model.Media;
import com.cappielloantonio.play.subsonic.models.IndexID3;
import com.cappielloantonio.play.subsonic.models.SubsonicResponse;
import com.cappielloantonio.play.util.MappingUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistRepository {
    private final Application application;

    public ArtistRepository(Application application) {
        this.application = application;
    }

    public MutableLiveData<List<Artist>> getStarredArtists(boolean random, int size) {
        MutableLiveData<List<Artist>> starredArtists = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getAlbumSongListClient()
                .getStarred2()
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getStarred2() != null) {
                            List<Artist> artists = new ArrayList<>(MappingUtil.mapArtist(response.body().getStarred2().getArtists()));

                            if (!random) {
                                getArtistInfo(artists, starredArtists);
                            } else {
                                Collections.shuffle(artists);
                                getArtistInfo(artists.subList(0, Math.min(size, artists.size())), starredArtists);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return starredArtists;
    }

    public MutableLiveData<List<Artist>> getArtists(boolean random, int size) {
        MutableLiveData<List<Artist>> listLiveArtists = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getBrowsingClient()
                .getArtists()
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Artist> artists = new ArrayList<>();

                            for (IndexID3 index : response.body().getArtists().getIndices()) {
                                artists.addAll(MappingUtil.mapArtist(index.getArtists()));
                            }

                            if (random) {
                                Collections.shuffle(artists);
                                getArtistInfo(artists.subList(0, artists.size() / size > 0 ? size : artists.size()), listLiveArtists);
                            } else {
                                listLiveArtists.setValue(artists);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {
                    }
                });

        return listLiveArtists;
    }

    /*
     * Metodo che mi restituisce le informazioni essenzionali dell'artista (cover, numero di album...)
     */
    public void getArtistInfo(List<Artist> artists, MutableLiveData<List<Artist>> list) {
        List<Artist> liveArtists = list.getValue();
        if (liveArtists == null) liveArtists = new ArrayList<>();
        list.setValue(liveArtists);

        for (Artist artist : artists) {
            App.getSubsonicClientInstance(application, false)
                    .getBrowsingClient()
                    .getArtist(artist.getId())
                    .enqueue(new Callback<SubsonicResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getArtist() != null) {
                                addToMutableLiveData(list, MappingUtil.mapArtistWithAlbum(response.body().getArtist()));
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                        }
                    });
        }
    }

    public MutableLiveData<Artist> getArtistInfo(String id) {
        MutableLiveData<Artist> artist = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getBrowsingClient()
                .getArtist(id)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getArtist() != null) {
                            artist.setValue(MappingUtil.mapArtistWithAlbum(response.body().getArtist()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return artist;
    }

    /*
     * Metodo che mi restituisce le informazioni complete dell'artista (bio, immagini prese da last.fm, artisti simili...)
     */
    public MutableLiveData<Artist> getArtistFullInfo(String id) {
        MutableLiveData<Artist> artistFullInfo = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getBrowsingClient()
                .getArtistInfo2(id)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getArtistInfo2() != null) {
                            artistFullInfo.setValue(MappingUtil.mapArtist(response.body().getArtistInfo2()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return artistFullInfo;
    }

    public void star(String id) {
        App.getSubsonicClientInstance(application, false)
                .getMediaAnnotationClient()
                .star(null, null, id)
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
                .unstar(null, null, id)
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

    public MutableLiveData<Artist> getArtist(String id) {
        MutableLiveData<Artist> artist = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getBrowsingClient()
                .getArtist(id)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getArtist() != null) {
                            artist.setValue(MappingUtil.mapArtist(response.body().getArtist()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return artist;
    }

    public MutableLiveData<ArrayList<Media>> getInstantMix(Artist artist, int count) {
        MutableLiveData<ArrayList<Media>> instantMix = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getBrowsingClient()
                .getSimilarSongs2(artist.getId(), count)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getSimilarSongs2() != null) {
                            instantMix.setValue(MappingUtil.mapSong(response.body().getSimilarSongs2().getSongs()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return instantMix;
    }

    public MutableLiveData<ArrayList<Media>> getArtistRandomSong(LifecycleOwner owner, Artist artist, int count) {
        MutableLiveData<ArrayList<Media>> randomSongs = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getBrowsingClient()
                .getArtist(artist.getId())
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getArtist() != null && response.body().getArtist().getAlbums() != null) {
                            List<Album> albums = new ArrayList<>(MappingUtil.mapAlbum(response.body().getArtist().getAlbums()));

                            if (albums.size() > 0) {
                                AlbumRepository albumRepository = new AlbumRepository(App.getInstance());

                                for (int index = 0; index < albums.size(); index++) {
                                    albumRepository.getAlbumTracks(albums.get(index).getId()).observe(owner, songs -> {
                                        ArrayList<Media> liveSongs = randomSongs.getValue();
                                        if (liveSongs == null) liveSongs = new ArrayList<>();
                                        Collections.shuffle(liveSongs);
                                        liveSongs.addAll(songs);
                                        randomSongs.setValue(liveSongs);
                                    });
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return randomSongs;
    }

    public MutableLiveData<List<Media>> getTopSongs(String artistName, int count) {
        MutableLiveData<List<Media>> topSongs = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getBrowsingClient()
                .getTopSongs(artistName, count)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubsonicResponse> call, @NonNull Response<SubsonicResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getTopSongs() != null) {
                            topSongs.setValue(MappingUtil.mapSong(response.body().getTopSongs().getSongs()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubsonicResponse> call, @NonNull Throwable t) {

                    }
                });

        return topSongs;
    }

    private void addToMutableLiveData(MutableLiveData<List<Artist>> liveData, Artist artist) {
        List<Artist> liveArtists = liveData.getValue();
        if (liveArtists != null) liveArtists.add(artist);
        liveData.setValue(liveArtists);
    }
}
