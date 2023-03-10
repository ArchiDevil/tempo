package com.cappielloantonio.play.subsonic.api.podcast;

import android.util.Log;

import com.cappielloantonio.play.App;
import com.cappielloantonio.play.subsonic.Subsonic;
import com.cappielloantonio.play.subsonic.base.ApiResponse;
import com.cappielloantonio.play.subsonic.utils.CacheUtil;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PodcastClient {
    private static final String TAG = "SystemClient";

    private final Subsonic subsonic;
    private final PodcastService podcastService;

    public PodcastClient(Subsonic subsonic) {
        this.subsonic = subsonic;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(subsonic.getUrl())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                .client(getOkHttpClient())
                .build();

        this.podcastService = retrofit.create(PodcastService.class);
    }

    public Call<ApiResponse> getPodcasts(boolean includeEpisodes, String channelId) {
        Log.d(TAG, "getPodcasts()");
        return podcastService.getPodcasts(subsonic.getParams(), includeEpisodes, channelId);
    }

    public Call<ApiResponse> getNewestPodcasts(int count) {
        Log.d(TAG, "getNewestPodcasts()");
        return podcastService.getNewestPodcasts(subsonic.getParams(), count);
    }

    public Call<ApiResponse> refreshPodcasts() {
        Log.d(TAG, "refreshPodcasts()");
        return podcastService.refreshPodcasts(subsonic.getParams());
    }

    private OkHttpClient getOkHttpClient() {
        CacheUtil cacheUtil = new CacheUtil(60, 60 * 60 * 24 * 30);

        return new OkHttpClient.Builder()
                .callTimeout(2, TimeUnit.MINUTES)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(getHttpLoggingInterceptor())
                .addInterceptor(cacheUtil.offlineInterceptor)
                .addNetworkInterceptor(cacheUtil.onlineInterceptor)
                .cache(getCache())
                .build();
    }

    private HttpLoggingInterceptor getHttpLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return loggingInterceptor;
    }

    private Cache getCache() {
        int cacheSize = 10 * 1024 * 1024;
        return new Cache(App.getContext().getCacheDir(), cacheSize);
    }
}
