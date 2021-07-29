package com.cappielloantonio.play.subsonic.api.mediaannotation;

import android.util.Log;

import com.cappielloantonio.play.subsonic.Subsonic;
import com.cappielloantonio.play.subsonic.models.SubsonicResponse;
import com.tickaroo.tikxml.TikXml;
import com.tickaroo.tikxml.converter.htmlescape.HtmlEscapeStringConverter;
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

public class MediaAnnotationClient {
    private static final String TAG = "BrowsingClient";

    private Subsonic subsonic;
    private Retrofit retrofit;
    private MediaAnnotationService mediaAnnotationService;

    public MediaAnnotationClient(Subsonic subsonic) {
        this.subsonic = subsonic;

        this.retrofit = new Retrofit.Builder()
                .baseUrl(subsonic.getUrl())
                .addConverterFactory(TikXmlConverterFactory.create(getParser()))
                .client(getOkHttpClient())
                .build();

        this.mediaAnnotationService = retrofit.create(MediaAnnotationService.class);
    }

    public Call<SubsonicResponse> star(String id) {
        Log.d(TAG, "star()");
        return mediaAnnotationService.star(subsonic.getParams(), id);
    }

    public Call<SubsonicResponse> unstar(String id) {
        Log.d(TAG, "unstar()");
        return mediaAnnotationService.unstar(subsonic.getParams(), id);
    }

    public Call<SubsonicResponse> setRating(String id, int star) {
        Log.d(TAG, "setRating()");
        return mediaAnnotationService.setRating(subsonic.getParams(), id, star);
    }

    public Call<SubsonicResponse> scrobble(String id) {
        Log.d(TAG, "scrobble()");
        return mediaAnnotationService.scrobble(subsonic.getParams(), id);
    }

    private TikXml getParser() {
        return new TikXml.Builder()
                .addTypeConverter(String.class, new HtmlEscapeStringConverter()) // HtmlEscapeStringConverter encode / decode html characters. This class ships as optional dependency
                .build();
    }

    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(getHttpLoggingInterceptor())
                .build();
    }

    private HttpLoggingInterceptor getHttpLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return loggingInterceptor;
    }
}
