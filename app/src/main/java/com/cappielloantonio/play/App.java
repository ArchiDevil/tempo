package com.cappielloantonio.play;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.cappielloantonio.play.helper.ThemeHelper;
import com.cappielloantonio.play.util.PreferenceUtil;

import org.jellyfin.apiclient.AppInfo;
import org.jellyfin.apiclient.Jellyfin;
import org.jellyfin.apiclient.JellyfinAndroidKt;
import org.jellyfin.apiclient.JellyfinOptions;
import org.jellyfin.apiclient.interaction.AndroidDevice;
import org.jellyfin.apiclient.interaction.ApiClient;
import org.jellyfin.apiclient.interaction.ApiEventListener;
import org.jellyfin.apiclient.logging.NullLogger;

public class App extends Application {
    private static final String TAG = "App";
    private static App instance;
    private static ApiClient apiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        ThemeHelper.applyTheme(PreferenceUtil.getInstance(getApplicationContext()).getTheme());
    }

    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public static ApiClient getApiClientInstance(Context context) {
        if (apiClient == null) {
            apiClient = getApiClient(context);
        }
        return apiClient;
    }

    public RequestQueue getRequestQueue(Context context) {
        return Volley.newRequestQueue(context);
    }

    private static ApiClient getApiClient(Context context) {
        String server = PreferenceUtil.getInstance(context).getServer();

        JellyfinOptions.Builder options = new JellyfinOptions.Builder();
        options.setLogger(new NullLogger());
        options.setAppInfo(new AppInfo(context.getString(R.string.app_name), BuildConfig.VERSION_NAME));
        JellyfinAndroidKt.android(options, context);

        Jellyfin jellyfin = new Jellyfin(options.build());

        return jellyfin.createApi(server, null, AndroidDevice.fromContext(context), new ApiEventListener());
    }
}
