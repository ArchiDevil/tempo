package com.cappielloantonio.play.ui.activity;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.broadcast.receiver.ConnectivityStatusBroadcastReceiver;
import com.cappielloantonio.play.databinding.ActivityMainBinding;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.service.MusicPlayerRemote;
import com.cappielloantonio.play.ui.activity.base.BaseActivity;
import com.cappielloantonio.play.ui.fragment.PlayerBottomSheetFragment;
import com.cappielloantonio.play.util.PreferenceUtil;
import com.cappielloantonio.play.viewmodel.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.Objects;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    public ActivityMainBinding bind;
    private MainViewModel mainViewModel;

    private FragmentManager fragmentManager;
    private NavHostFragment navHostFragment;
    private BottomNavigationView bottomNavigationView;
    public NavController navController;
    private BottomSheetBehavior bottomSheetBehavior;

    ConnectivityStatusBroadcastReceiver connectivityStatusBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashScreen.installSplashScreen(this);

        bind = ActivityMainBinding.inflate(getLayoutInflater());
        View view = bind.getRoot();
        setContentView(view);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        connectivityStatusBroadcastReceiver = new ConnectivityStatusBroadcastReceiver(this);
        connectivityStatusReceiverManager(true);

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectivityStatusReceiverManager(false);
    }

    public void init() {
        fragmentManager = getSupportFragmentManager();

        initBottomSheet();
        initNavigation();

        if (PreferenceUtil.getInstance(this).getToken() != null) {
            goFromLogin();
        } else {
            goToLogin();
        }
    }

    // BOTTOM SHEET/NAVIGATION
    private void initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.player_bottom_sheet));
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);
        fragmentManager.beginTransaction().replace(R.id.player_bottom_sheet, new PlayerBottomSheetFragment(), "PlayerBottomSheet").commit();

        /*
         * All'apertura mostro il bottom sheet solo se in coda c'è qualcosa
         */
        isBottomSheetInPeek(mainViewModel.isQueueLoaded());
    }

    public void isBottomSheetInPeek(Boolean isVisible) {
        if (isVisible) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void initNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navHostFragment = (NavHostFragment) fragmentManager.findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        /*
         * In questo modo intercetto il cambio schermata tramite navbar e se il bottom sheet è aperto,
         * lo chiudo
         */
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED && (
                    destination.getId() == R.id.homeFragment ||
                            destination.getId() == R.id.libraryFragment ||
                            destination.getId() == R.id.searchFragment ||
                            destination.getId() == R.id.settingsFragment)
            ) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    public void setBottomNavigationBarVisibility(boolean visibility) {
        if (visibility) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        } else {
            bottomNavigationView.setVisibility(View.GONE);
        }
    }

    public void setBottomSheetVisibility(boolean visibility) {
        if (visibility) {
            findViewById(R.id.player_bottom_sheet).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.player_bottom_sheet).setVisibility(View.GONE);
        }
    }

    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback =
            new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View view, int state) {
                    switch (state) {
                        case BottomSheetBehavior.STATE_HIDDEN:
                            MusicPlayerRemote.quitPlaying();
                            break;
                        case BottomSheetBehavior.STATE_COLLAPSED:
                        case BottomSheetBehavior.STATE_SETTLING:
                            PlayerBottomSheetFragment playerBottomSheetFragment = (PlayerBottomSheetFragment) getSupportFragmentManager().findFragmentByTag("PlayerBottomSheet");
                            if (playerBottomSheetFragment != null)
                                playerBottomSheetFragment.scrollOnTop();
                            break;
                    }
                }

                @Override
                public void onSlide(@NonNull View view, float slideOffset) {
                    PlayerBottomSheetFragment playerBottomSheetFragment = (PlayerBottomSheetFragment) getSupportFragmentManager().findFragmentByTag("PlayerBottomSheet");
                    if (playerBottomSheetFragment == null) {
                        return;
                    } else {
                        float condensedSlideOffset = Math.max(0.0f, Math.min(0.2f, slideOffset - 0.2f)) / 0.2f;
                        playerBottomSheetFragment.getPlayerHeader().setAlpha(1 - condensedSlideOffset);
                        playerBottomSheetFragment.getPlayerHeader().setVisibility(condensedSlideOffset > 0.99 ? View.GONE : View.VISIBLE);
                    }
                }
            };

    /*
     * Scroll on top del bottom sheet quando chiudo
     * In questo modo non mi ritrovo al posto dell'header una parte centrale del player
     */
    public void setBottomSheetMusicInfo(Song song) {
        PlayerBottomSheetFragment playerBottomSheetFragment = (PlayerBottomSheetFragment) getSupportFragmentManager().findFragmentByTag("PlayerBottomSheet");
        if (playerBottomSheetFragment == null) return;

        playerBottomSheetFragment.scrollPager(song, 0, false);
    }

    // NAVIGATION
    public void goToLogin() {
        if (Objects.requireNonNull(navController.getCurrentDestination()).getId() == R.id.landingFragment) {
            navController.navigate(R.id.action_landingFragment_to_loginFragment);
        } else if(Objects.requireNonNull(navController.getCurrentDestination()).getId() == R.id.settingsFragment) {
            navController.navigate(R.id.action_settingsFragment_to_loginFragment);
        }
    }

    public void goToHome() {
        bottomNavigationView.setVisibility(View.VISIBLE);

        if (Objects.requireNonNull(navController.getCurrentDestination()).getId() == R.id.landingFragment) {
            navController.navigate(R.id.action_landingFragment_to_homeFragment);
        } else if (Objects.requireNonNull(navController.getCurrentDestination()).getId() == R.id.loginFragment) {
            navController.navigate(R.id.action_loginFragment_to_homeFragment);
        }
    }

    public void goFromLogin() {
        goToHome();
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else
            super.onBackPressed();
    }

    // CONNECTION
    private void connectivityStatusReceiverManager(boolean isActive) {
        if (isActive) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(connectivityStatusBroadcastReceiver, filter);
        } else {
            unregisterReceiver(connectivityStatusBroadcastReceiver);
        }
    }
}