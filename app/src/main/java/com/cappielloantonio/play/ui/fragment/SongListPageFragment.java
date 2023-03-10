package com.cappielloantonio.play.ui.fragment;

import android.content.ComponentName;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaBrowser;
import androidx.media3.session.SessionToken;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.ui.adapter.SongHorizontalAdapter;
import com.cappielloantonio.play.databinding.FragmentSongListPageBinding;
import com.cappielloantonio.play.interfaces.ClickCallback;
import com.cappielloantonio.play.model.Media;
import com.cappielloantonio.play.service.MediaManager;
import com.cappielloantonio.play.service.MediaService;
import com.cappielloantonio.play.ui.activity.MainActivity;
import com.cappielloantonio.play.util.MusicUtil;
import com.cappielloantonio.play.viewmodel.SongListPageViewModel;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;

@UnstableApi
public class SongListPageFragment extends Fragment implements ClickCallback {
    private FragmentSongListPageBinding bind;
    private MainActivity activity;
    private SongListPageViewModel songListPageViewModel;

    private SongHorizontalAdapter songHorizontalAdapter;

    private ListenableFuture<MediaBrowser> mediaBrowserListenableFuture;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();

        bind = FragmentSongListPageBinding.inflate(inflater, container, false);
        View view = bind.getRoot();
        songListPageViewModel = new ViewModelProvider(requireActivity()).get(SongListPageViewModel.class);

        init();
        initAppBar();
        initButtons();
        initSongListView();

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
        if (requireArguments().getString(Media.RECENTLY_PLAYED) != null) {
            songListPageViewModel.title = Media.RECENTLY_PLAYED;
            songListPageViewModel.toolbarTitle = getString(R.string.song_list_page_recently_played);
            bind.pageTitleLabel.setText(R.string.song_list_page_recently_played);
        } else if (requireArguments().getString(Media.MOST_PLAYED) != null) {
            songListPageViewModel.title = Media.MOST_PLAYED;
            songListPageViewModel.toolbarTitle = getString(R.string.song_list_page_most_played);
            bind.pageTitleLabel.setText(R.string.song_list_page_most_played);
        } else if (requireArguments().getString(Media.RECENTLY_ADDED) != null) {
            songListPageViewModel.title = Media.RECENTLY_ADDED;
            songListPageViewModel.toolbarTitle = getString(R.string.song_list_page_recently_added);
            bind.pageTitleLabel.setText(R.string.song_list_page_recently_added);
        } else if (requireArguments().getString(Media.BY_GENRE) != null) {
            songListPageViewModel.title = Media.BY_GENRE;
            songListPageViewModel.genre = requireArguments().getParcelable("genre_object");
            songListPageViewModel.toolbarTitle = MusicUtil.getReadableString(songListPageViewModel.genre.getGenre());
            bind.pageTitleLabel.setText(MusicUtil.getReadableString(songListPageViewModel.genre.getGenre()));
        } else if (requireArguments().getString(Media.BY_ARTIST) != null) {
            songListPageViewModel.title = Media.BY_ARTIST;
            songListPageViewModel.artist = requireArguments().getParcelable("artist_object");
            songListPageViewModel.toolbarTitle = getString(R.string.song_list_page_top, MusicUtil.getReadableString(songListPageViewModel.artist.getName()));
            bind.pageTitleLabel.setText(getString(R.string.song_list_page_top, MusicUtil.getReadableString(songListPageViewModel.artist.getName())));
        } else if (requireArguments().getString(Media.BY_GENRES) != null) {
            songListPageViewModel.title = Media.BY_GENRES;
            songListPageViewModel.filters = requireArguments().getStringArrayList("filters_list");
            songListPageViewModel.filterNames = requireArguments().getStringArrayList("filter_name_list");
            songListPageViewModel.toolbarTitle = songListPageViewModel.getFiltersTitle();
            bind.pageTitleLabel.setText(songListPageViewModel.getFiltersTitle());
        } else if (requireArguments().getString(Media.BY_YEAR) != null) {
            songListPageViewModel.title = Media.BY_YEAR;
            songListPageViewModel.year = requireArguments().getInt("year_object");
            songListPageViewModel.toolbarTitle = getString(R.string.song_list_page_year, songListPageViewModel.year);
            bind.pageTitleLabel.setText(getString(R.string.song_list_page_year, songListPageViewModel.year));
        } else if (requireArguments().getString(Media.STARRED) != null) {
            songListPageViewModel.title = Media.STARRED;
            songListPageViewModel.toolbarTitle = getString(R.string.song_list_page_starred);
            bind.pageTitleLabel.setText(R.string.song_list_page_starred);
        } else if (requireArguments().getString(Media.DOWNLOADED) != null) {
            songListPageViewModel.title = Media.DOWNLOADED;
            songListPageViewModel.toolbarTitle = getString(R.string.song_list_page_downloaded);
            bind.pageTitleLabel.setText(getString(R.string.song_list_page_downloaded));
        } else if (requireArguments().getParcelable("album_object") != null) {
            songListPageViewModel.album = requireArguments().getParcelable("album_object");
            songListPageViewModel.title = Media.FROM_ALBUM;
            songListPageViewModel.toolbarTitle = MusicUtil.getReadableString(songListPageViewModel.album.getName());
            bind.pageTitleLabel.setText(MusicUtil.getReadableString(songListPageViewModel.album.getName()));
        }
    }

    private void initAppBar() {
        activity.setSupportActionBar(bind.toolbar);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (bind != null)
            bind.toolbar.setNavigationOnClickListener(v -> activity.navController.navigateUp());

        if (bind != null)
            bind.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                if ((bind.albumInfoSector.getHeight() + verticalOffset) < (2 * ViewCompat.getMinimumHeight(bind.toolbar))) {
                    bind.toolbar.setTitle(songListPageViewModel.toolbarTitle);
                } else {
                    bind.toolbar.setTitle(R.string.empty_string);
                }
            });
    }

    private void initButtons() {
        songListPageViewModel.getSongList(getViewLifecycleOwner()).observe(getViewLifecycleOwner(), songs -> {
            if (bind != null) {
                bind.songListShuffleImageView.setOnClickListener(v -> {
                    Collections.shuffle(songs);
                    MediaManager.startQueue(mediaBrowserListenableFuture, requireContext(), songs.subList(0, Math.min(25, songs.size())), 0);
                    activity.setBottomSheetInPeek(true);
                });
            }
        });
    }

    private void initSongListView() {
        bind.songListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bind.songListRecyclerView.setHasFixedSize(true);

        songHorizontalAdapter = new SongHorizontalAdapter(this, true);
        bind.songListRecyclerView.setAdapter(songHorizontalAdapter);
        songListPageViewModel.getSongList(getViewLifecycleOwner()).observe(getViewLifecycleOwner(), songs -> songHorizontalAdapter.setItems(songs));
    }

    private void initializeMediaBrowser() {
        mediaBrowserListenableFuture = new MediaBrowser.Builder(requireContext(), new SessionToken(requireContext(), new ComponentName(requireContext(), MediaService.class))).buildAsync();
    }

    private void releaseMediaBrowser() {
        MediaBrowser.releaseFuture(mediaBrowserListenableFuture);
    }

    @Override
    public void onMediaClick(Bundle bundle) {
        MediaManager.startQueue(mediaBrowserListenableFuture, requireContext(), bundle.getParcelableArrayList("songs_object"), bundle.getInt("position"));
        activity.setBottomSheetInPeek(true);
    }

    @Override
    public void onMediaLongClick(Bundle bundle) {
        Navigation.findNavController(requireView()).navigate(R.id.songBottomSheetDialog, bundle);
    }
}