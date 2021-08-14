package com.cappielloantonio.play.broadcast.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.view.View;

import com.cappielloantonio.play.ui.activity.MainActivity;

public class ConnectivityStatusBroadcastReceiver extends BroadcastReceiver {
    private final MainActivity activity;

    public ConnectivityStatusBroadcastReceiver(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if (noConnectivity) {
                activity.bind.offlineModeTextView.setVisibility(View.VISIBLE);
            } else {
                activity.bind.offlineModeTextView.setVisibility(View.GONE);
            }
        }
    }
}