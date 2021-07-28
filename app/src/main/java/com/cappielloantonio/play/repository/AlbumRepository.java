package com.cappielloantonio.play.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cappielloantonio.play.App;
import com.cappielloantonio.play.model.Album;
import com.cappielloantonio.play.subsonic.models.ResponseStatus;
import com.cappielloantonio.play.subsonic.models.SubsonicResponse;
import com.cappielloantonio.play.util.MappingUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumRepository {
    private static final String TAG = "AlbumRepository";

    private Application application;

    private MutableLiveData<List<Album>> listLiveRecentlyAddedAlbums = new MutableLiveData<>();
    private MutableLiveData<List<Album>> listLiveMostPlayedAlbums = new MutableLiveData<>();
    private MutableLiveData<List<Album>> listLiveRecentlyPlayedAlbums = new MutableLiveData<>();

    public AlbumRepository(Application application) {
        this.application = application;
    }

    public LiveData<List<Album>> getListLiveAlbums(String type, int size) {
        App.getSubsonicClientInstance(application, false).getAlbumSongListClient()
                .getAlbumList2(type, size, 0)
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(Call<SubsonicResponse> call, Response<SubsonicResponse> response) {
                        List<Album> albums = new ArrayList<>(MappingUtil.mapAlbum(response.body().getAlbumList2().getAlbums()));

                        switch (type) {
                            case "newest":
                                listLiveRecentlyAddedAlbums.setValue(albums);
                                break;
                            case "frequent":
                                listLiveMostPlayedAlbums.setValue(albums);
                                break;
                            case "recent":
                                listLiveRecentlyPlayedAlbums.setValue(albums);
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call<SubsonicResponse> call, Throwable t) {

                    }
                });

        switch (type) {
            case "newest":
                return listLiveRecentlyAddedAlbums;
            case "frequent":
                return listLiveMostPlayedAlbums;
            case "recent":
                return listLiveRecentlyPlayedAlbums;
            default:
                return new MutableLiveData<>();
        }
    }

    public MutableLiveData<List<Album>> getStarredAlbums() {
        MutableLiveData<List<Album>> starredAlbums = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false).getAlbumSongListClient()
                .getStarred2()
                .enqueue(new Callback<SubsonicResponse>() {
                    @Override
                    public void onResponse(Call<SubsonicResponse> call, Response<SubsonicResponse> response) {
                        if (response.body().getStatus().getValue().equals(ResponseStatus.OK)) {
                            List<Album> albums = new ArrayList<>(MappingUtil.mapAlbum(response.body().getStarred2().getAlbums()));
                            starredAlbums.setValue(albums);
                        }
                    }

                    @Override
                    public void onFailure(Call<SubsonicResponse> call, Throwable t) {
                    }
                });

        return starredAlbums;
    }
}
