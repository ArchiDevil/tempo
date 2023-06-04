package com.cappielloantonio.play.ui.fragment.bottomsheetdialog;

import android.content.ComponentName;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaBrowser;
import androidx.media3.session.SessionToken;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.glide.CustomGlideRequest;
import com.cappielloantonio.play.service.MediaService;
import com.cappielloantonio.play.subsonic.models.PodcastChannel;
import com.cappielloantonio.play.util.Constants;
import com.cappielloantonio.play.util.MusicUtil;
import com.cappielloantonio.play.viewmodel.PodcastChannelBottomSheetViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.common.util.concurrent.ListenableFuture;

@UnstableApi
public class PodcastChannelBottomSheetDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    private PodcastChannelBottomSheetViewModel podcastChannelBottomSheetViewModel;
    private PodcastChannel podcastChannel;

    private ListenableFuture<MediaBrowser> mediaBrowserListenableFuture;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_podcast_channel_dialog, container, false);

        podcastChannel = requireArguments().getParcelable(Constants.PODCAST_CHANNEL_OBJECT);

        podcastChannelBottomSheetViewModel = new ViewModelProvider(requireActivity()).get(PodcastChannelBottomSheetViewModel.class);
        podcastChannelBottomSheetViewModel.setPodcastChannel(podcastChannel);

        init(view);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        initializeMediaBrowser();
    }

    @Override
    public void onStop() {
        releaseMediaBrowser();
        super.onStop();
    }

    private void init(View view) {
        ImageView coverPodcast = view.findViewById(R.id.podcast_cover_image_view);

        CustomGlideRequest.Builder
                .from(requireContext(), podcastChannelBottomSheetViewModel.getPodcastChannel().getCoverArtId())
                .build()
                .into(coverPodcast);

        TextView titlePodcast = view.findViewById(R.id.podcast_title_text_view);
        titlePodcast.setText(MusicUtil.getReadableString(podcastChannelBottomSheetViewModel.getPodcastChannel().getTitle()));

        TextView delete = view.findViewById(R.id.delete_text_view);
        delete.setOnClickListener(v -> {
            podcastChannelBottomSheetViewModel.deletePodcastChannel();
            dismissBottomSheet();
        });
    }

    @Override
    public void onClick(View v) {
        dismissBottomSheet();
    }

    private void dismissBottomSheet() {
        dismiss();
    }

    private void initializeMediaBrowser() {
        mediaBrowserListenableFuture = new MediaBrowser.Builder(requireContext(), new SessionToken(requireContext(), new ComponentName(requireContext(), MediaService.class))).buildAsync();
    }

    private void releaseMediaBrowser() {
        MediaBrowser.releaseFuture(mediaBrowserListenableFuture);
    }
}