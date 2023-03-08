package com.cappielloantonio.play.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.cappielloantonio.play.App;
import com.cappielloantonio.play.subsonic.base.ApiResponse;
import com.cappielloantonio.play.subsonic.models.PodcastChannel;
import com.cappielloantonio.play.subsonic.models.PodcastEpisode;
import com.cappielloantonio.play.subsonic.models.SubsonicResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PodcastRepository {
    private static final String TAG = "SongRepository";

    private final Application application;

    public PodcastRepository(Application application) {
        this.application = application;
    }

    public MutableLiveData<List<PodcastChannel>> getPodcastChannels(boolean includeEpisodes, String channelId) {
        MutableLiveData<List<PodcastChannel>> livePodcastChannel = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getPodcastClient()
                .getPodcasts(includeEpisodes, channelId)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getSubsonicResponse().getPodcasts() != null) {
                            livePodcastChannel.setValue(response.body().getSubsonicResponse().getPodcasts().getChannels());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {

                    }
                });

        return livePodcastChannel;
    }

    public MutableLiveData<List<PodcastEpisode>> getNewestPodcastEpisodes(int count) {
        MutableLiveData<List<PodcastEpisode>> liveNewestPodcastEpisodes = new MutableLiveData<>();

        App.getSubsonicClientInstance(application, false)
                .getPodcastClient()
                .getNewestPodcasts(count)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getSubsonicResponse().getNewestPodcasts() != null) {
                            liveNewestPodcastEpisodes.setValue(response.body().getSubsonicResponse().getNewestPodcasts().getEpisodes());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                        Log.d(TAG, "onFailure()");
                    }
                });

        return liveNewestPodcastEpisodes;
    }

    public void refreshPodcasts() {
        App.getSubsonicClientInstance(application, false)
                .getPodcastClient()
                .refreshPodcasts()
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {

                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {

                    }
                });
    }
}
