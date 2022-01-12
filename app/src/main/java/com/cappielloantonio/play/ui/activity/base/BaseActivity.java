package com.cappielloantonio.play.ui.activity.base;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.exoplayer.offline.DownloadService;
import androidx.media3.session.MediaBrowser;
import androidx.media3.session.SessionToken;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.service.DownloaderManager;
import com.cappielloantonio.play.service.DownloaderService;
import com.cappielloantonio.play.service.MediaService;
import com.cappielloantonio.play.util.DownloadUtil;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.material.elevation.SurfaceColors;
import com.google.common.util.concurrent.ListenableFuture;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    private ListenableFuture<MediaBrowser> mediaBrowserListenableFuture;
    private DownloaderManager downloaderManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeCastContext();
        initializeDownloader();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeBarColors();
        initializeBrowser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBatteryOptimization();
    }

    @Override
    protected void onStop() {
        releaseBrowser();
        super.onStop();
    }

    private void checkBatteryOptimization() {
        if (detectBatteryOptimization()) {
            showBatteryOptimizationDialog();
        }
    }

    private boolean detectBatteryOptimization() {
        String packageName = getPackageName();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        return !powerManager.isIgnoringBatteryOptimizations(packageName);
    }

    private void showBatteryOptimizationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.activity_battery_optimizations_summary)
                .setTitle(R.string.activity_battery_optimizations_title)
                .setNegativeButton(R.string.activity_negative_button, null)
                .setPositiveButton(R.string.activity_neutral_button, (dialog, id) -> openPowerSettings())
                .show();
    }

    private void openPowerSettings() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        startActivity(intent);
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void initializeBrowser() {
        mediaBrowserListenableFuture = new MediaBrowser.Builder(this, new SessionToken(this, new ComponentName(this, MediaService.class))).buildAsync();
    }

    private void releaseBrowser() {
        MediaBrowser.releaseFuture(mediaBrowserListenableFuture);
    }

    public ListenableFuture<MediaBrowser> getMediaBrowserListenableFuture() {
        return mediaBrowserListenableFuture;
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void initializeDownloader() {
        downloaderManager = DownloadUtil.getDownloadTracker(this);

        try {
            DownloadService.start(this, DownloaderService.class);
        } catch (IllegalStateException e) {
            DownloadService.startForeground(this, DownloaderService.class);
        }
    }

    private void initializeCastContext() {
        CastContext.getSharedInstance(this);
    }

    private void initializeBarColors() {
        getWindow().setStatusBarColor(SurfaceColors.getColorForElevation(this, 10));
        getWindow().setNavigationBarColor(SurfaceColors.getColorForElevation(this, 10));
    }
}
