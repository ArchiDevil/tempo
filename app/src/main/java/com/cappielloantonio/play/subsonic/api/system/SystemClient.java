package com.cappielloantonio.play.subsonic.api.system;

import android.content.Context;
import android.util.Log;

import com.cappielloantonio.play.subsonic.Subsonic;
import com.cappielloantonio.play.subsonic.base.ApiResponse;
import com.cappielloantonio.play.subsonic.models.SubsonicResponse;
import com.cappielloantonio.play.subsonic.utils.CacheUtil;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SystemClient {
    private static final String TAG = "SystemClient";

    private final Context context;
    private final Subsonic subsonic;
    private final SystemService systemService;

    public SystemClient(Context context, Subsonic subsonic) {
        this.context = context;
        this.subsonic = subsonic;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(subsonic.getUrl())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                .client(getOkHttpClient())
                .build();

        this.systemService = retrofit.create(SystemService.class);
    }

    public Call<ApiResponse> ping() {
        Log.d(TAG, "ping()");
        return systemService.ping(subsonic.getParams());
    }

    public Call<ApiResponse> getLicense() {
        Log.d(TAG, "getLicense()");
        return systemService.getLicense(subsonic.getParams());
    }

    private OkHttpClient getOkHttpClient() {
        CacheUtil cacheUtil = new CacheUtil(context, 0, 60 * 60 * 24 * 30);

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
        return context != null ? new Cache(context.getCacheDir(), cacheSize) : null;
    }
}
