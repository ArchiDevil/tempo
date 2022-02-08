package com.cappielloantonio.play.ui.fragment;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.session.MediaBrowser;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.cappielloantonio.play.R;
import com.cappielloantonio.play.databinding.FragmentPlayerBottomSheetBinding;
import com.cappielloantonio.play.glide.CustomGlideRequest;
import com.cappielloantonio.play.service.MediaService;
import com.cappielloantonio.play.ui.fragment.pager.PlayerControllerVerticalPager;
import com.cappielloantonio.play.util.MusicUtil;
import com.cappielloantonio.play.viewmodel.PlayerBottomSheetViewModel;
import com.google.android.material.elevation.SurfaceColors;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

public class PlayerBottomSheetFragment extends Fragment {
    private static final String TAG = "PlayerBottomSheetFragment";

    private FragmentPlayerBottomSheetBinding bind;

    private PlayerBottomSheetViewModel playerBottomSheetViewModel;
    private ListenableFuture<MediaBrowser> mediaBrowserListenableFuture;

    // TODO: Collegare la seekbar all'exo_progress

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bind = FragmentPlayerBottomSheetBinding.inflate(inflater, container, false);
        View view = bind.getRoot();

        playerBottomSheetViewModel = new ViewModelProvider(requireActivity()).get(PlayerBottomSheetViewModel.class);

        customizeBottomSheetBackground();
        initViewPager();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        initializeMediaBrowser();
        bindMediaController();
    }

    @Override
    public void onStop() {
        releaseMediaBrowser();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
    }

    private void customizeBottomSheetBackground() {
        bind.playerHeaderLayout.getRoot().setBackgroundColor(SurfaceColors.getColorForElevation(requireContext(), 2));
    }

    private void initViewPager() {
        bind.playerBodyLayout.playerBodyBottomSheetViewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        bind.playerBodyLayout.playerBodyBottomSheetViewPager.setAdapter(new PlayerControllerVerticalPager(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void initializeMediaBrowser() {
        mediaBrowserListenableFuture = new MediaBrowser.Builder(requireContext(), new SessionToken(requireContext(), new ComponentName(requireContext(), MediaService.class))).buildAsync();
    }

    private void releaseMediaBrowser() {
        MediaController.releaseFuture(mediaBrowserListenableFuture);
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void bindMediaController() {
        mediaBrowserListenableFuture.addListener(() -> {
            try {
                MediaBrowser mediaBrowser = mediaBrowserListenableFuture.get();

                setMediaControllerListener(mediaBrowser);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }, MoreExecutors.directExecutor());
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void setMediaControllerListener(MediaBrowser mediaBrowser) {
        setMetadata(mediaBrowser.getMediaMetadata());
        setContentDuration(mediaBrowser.getContentDuration());
        setPlayingState(mediaBrowser.isPlaying());
        setHeaderMediaController();
        setHeaderNextButtonState(mediaBrowser.hasNextMediaItem());

        mediaBrowser.addListener(new Player.Listener() {
            @Override
            public void onMediaMetadataChanged(@NonNull MediaMetadata mediaMetadata) {
                setMetadata(mediaMetadata);
                setContentDuration(mediaBrowser.getContentDuration());
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                setPlayingState(isPlaying);
            }

            @Override
            public void onEvents(Player player, Player.Events events) {
                setHeaderNextButtonState(mediaBrowser.hasNextMediaItem());
            }
        });
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void setMetadata(MediaMetadata mediaMetadata) {
        if (mediaMetadata.extras != null)
            playerBottomSheetViewModel.setLiveMedia(requireActivity(), mediaMetadata.extras.getString("mediaType"), mediaMetadata.extras.getString("id"));
        if (mediaMetadata.extras != null)
            playerBottomSheetViewModel.setLiveArtist(requireActivity(), mediaMetadata.extras.getString("mediaType"), mediaMetadata.extras.getString("artistId"));

        bind.playerHeaderLayout.playerHeaderMediaTitleLabel.setText(MusicUtil.getReadableString(String.valueOf(mediaMetadata.title)));
        bind.playerHeaderLayout.playerHeaderMediaArtistLabel.setText(MusicUtil.getReadableString(String.valueOf(mediaMetadata.artist)));

        if (mediaMetadata.extras != null) CustomGlideRequest.Builder
                .from(requireContext(), mediaMetadata.extras.getString("id"), CustomGlideRequest.SONG_PIC, null)
                .build()
                .transform(new RoundedCorners(CustomGlideRequest.CORNER_RADIUS))
                .into(bind.playerHeaderLayout.playerHeaderMediaCoverImage);
    }

    private void setContentDuration(long duration) {
        bind.playerHeaderLayout.playerHeaderSeekBar.setMax((int) (duration / 1000));
    }

    private void setPlayingState(boolean isPlaying) {
        bind.playerHeaderLayout.playerHeaderButton.setChecked(isPlaying);
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void setHeaderMediaController() {
        bind.playerHeaderLayout.playerHeaderButton.setOnClickListener(view -> {
            if (bind.playerHeaderLayout.playerHeaderButton.isChecked()) {
                bind.getRoot().findViewById(R.id.exo_play).performClick();
            } else {
                bind.getRoot().findViewById(R.id.exo_pause).performClick();
            }
        });

        bind.playerHeaderLayout.playerHeaderNextMediaButton.setOnClickListener(view -> bind.getRoot().findViewById(R.id.exo_next).performClick());
    }

    private void setHeaderNextButtonState(boolean isEnabled) {
        bind.playerHeaderLayout.playerHeaderNextMediaButton.setEnabled(isEnabled);
        bind.playerHeaderLayout.playerHeaderNextMediaButton.setAlpha(isEnabled ? (float) 1.0 : (float) 0.3);
    }

    public View getPlayerHeader() {
        return requireView().findViewById(R.id.player_header_layout);
    }

    public void goBackToFirstPage() {
        bind.playerBodyLayout.playerBodyBottomSheetViewPager.setCurrentItem(0, false);
        goToControllerPage();
    }

    public void goToControllerPage() {
        PlayerControllerVerticalPager playerControllerVerticalPager = (PlayerControllerVerticalPager) bind.playerBodyLayout.playerBodyBottomSheetViewPager.getAdapter();
        if (playerControllerVerticalPager != null) {
            PlayerControllerFragment playerControllerFragment = (PlayerControllerFragment) playerControllerVerticalPager.getRegisteredFragment(0);
            if (playerControllerFragment != null) {
                playerControllerFragment.goToControllerPage();
            }
        }
    }

    public void goToLyricsPage() {
        PlayerControllerVerticalPager playerControllerVerticalPager = (PlayerControllerVerticalPager) bind.playerBodyLayout.playerBodyBottomSheetViewPager.getAdapter();
        if (playerControllerVerticalPager != null) {
            PlayerControllerFragment playerControllerFragment = (PlayerControllerFragment) playerControllerVerticalPager.getRegisteredFragment(0);
            if (playerControllerFragment != null) {
                playerControllerFragment.goToLyricsPage();
            }
        }
    }
}
