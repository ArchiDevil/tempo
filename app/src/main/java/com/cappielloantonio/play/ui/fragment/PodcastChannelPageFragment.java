package com.cappielloantonio.play.ui.fragment;

import android.content.ComponentName;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaBrowser;
import androidx.media3.session.SessionToken;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.databinding.FragmentPodcastChannelPageBinding;
import com.cappielloantonio.play.glide.CustomGlideRequest;
import com.cappielloantonio.play.interfaces.ClickCallback;
import com.cappielloantonio.play.service.MediaManager;
import com.cappielloantonio.play.service.MediaService;
import com.cappielloantonio.play.ui.activity.MainActivity;
import com.cappielloantonio.play.ui.adapter.PodcastEpisodeAdapter;
import com.cappielloantonio.play.util.Constants;
import com.cappielloantonio.play.util.MusicUtil;
import com.cappielloantonio.play.util.UIUtil;
import com.cappielloantonio.play.viewmodel.PodcastChannelPageViewModel;
import com.google.common.util.concurrent.ListenableFuture;

@UnstableApi
public class PodcastChannelPageFragment extends Fragment implements ClickCallback {
    private FragmentPodcastChannelPageBinding bind;
    private MainActivity activity;
    private PodcastChannelPageViewModel podcastChannelPageViewModel;

    private PodcastEpisodeAdapter podcastEpisodeAdapter;

    private ListenableFuture<MediaBrowser> mediaBrowserListenableFuture;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();

        bind = FragmentPodcastChannelPageBinding.inflate(inflater, container, false);
        View view = bind.getRoot();
        podcastChannelPageViewModel = new ViewModelProvider(requireActivity()).get(PodcastChannelPageViewModel.class);

        init();
        initAppBar();
        initPodcastChannelInfo();
        initPodcastChannelEpisodesView();

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
    }

    private void init() {
        podcastChannelPageViewModel.setPodcastChannel(requireArguments().getParcelable(Constants.PODCAST_CHANNEL_OBJECT));
    }

    private void initAppBar() {
        activity.setSupportActionBar(bind.animToolbar);
        if (activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bind.collapsingToolbar.setTitle(MusicUtil.getReadableString(podcastChannelPageViewModel.getPodcastChannel().getTitle()));
        bind.animToolbar.setNavigationOnClickListener(v -> activity.navController.navigateUp());
        bind.collapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.white, null));
    }

    private void initPodcastChannelInfo() {
        String normalizePodcastChannelDescription = MusicUtil.forceReadableString(podcastChannelPageViewModel.getPodcastChannel().getDescription());

        if (bind != null)
            bind.podcastChannelDescriptionTextView.setVisibility(!normalizePodcastChannelDescription.trim().isEmpty() ? View.VISIBLE : View.GONE);

        if (getContext() != null && bind != null) CustomGlideRequest.Builder
                .from(requireContext(), podcastChannelPageViewModel.getPodcastChannel().getCoverArtId())
                .build()
                .into(bind.podcastChannelBackdropImageView);

        if (bind != null)
            bind.podcastChannelDescriptionTextView.setText(normalizePodcastChannelDescription);
    }

    private void initPodcastChannelEpisodesView() {
        bind.mostStreamedSongRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bind.mostStreamedSongRecyclerView.addItemDecoration(UIUtil.getDividerItemDecoration(requireContext()));

        podcastEpisodeAdapter = new PodcastEpisodeAdapter(this);
        bind.mostStreamedSongRecyclerView.setAdapter(podcastEpisodeAdapter);
        podcastChannelPageViewModel.getPodcastChannelEpisodes().observe(getViewLifecycleOwner(), channels -> {
            if (channels == null) {
                if (bind != null)
                    bind.podcastChannelPageEpisodesPlaceholder.placeholder.setVisibility(View.VISIBLE);
                if (bind != null) bind.podcastChannelPageEpisodesSector.setVisibility(View.GONE);
            } else {
                if (bind != null)
                    bind.podcastChannelPageEpisodesPlaceholder.placeholder.setVisibility(View.GONE);

                if (channels.get(0) != null && channels.get(0).getEpisodes() != null) {
                    if (bind != null)
                        bind.podcastChannelPageEpisodesSector.setVisibility(!channels.get(0).getEpisodes().isEmpty() ? View.VISIBLE : View.GONE);
                    podcastEpisodeAdapter.setItems(channels.get(0).getEpisodes());
                }
            }
        });
    }

    private void initializeMediaBrowser() {
        mediaBrowserListenableFuture = new MediaBrowser.Builder(requireContext(), new SessionToken(requireContext(), new ComponentName(requireContext(), MediaService.class))).buildAsync();
    }

    private void releaseMediaBrowser() {
        MediaBrowser.releaseFuture(mediaBrowserListenableFuture);
    }

    @Override
    public void onPodcastEpisodeClick(Bundle bundle) {
        MediaManager.startPodcast(mediaBrowserListenableFuture, bundle.getParcelable(Constants.PODCAST_OBJECT));
        activity.setBottomSheetInPeek(true);
    }

    @Override
    public void onPodcastEpisodeLongClick(Bundle bundle) {
        Toast.makeText(requireContext(), "Long click!", Toast.LENGTH_SHORT).show();
    }
}