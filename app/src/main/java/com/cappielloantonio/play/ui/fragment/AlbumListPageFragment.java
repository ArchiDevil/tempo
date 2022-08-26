package com.cappielloantonio.play.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.adapter.AlbumHorizontalAdapter;
import com.cappielloantonio.play.databinding.FragmentAlbumListPageBinding;
import com.cappielloantonio.play.model.Album;
import com.cappielloantonio.play.ui.activity.MainActivity;
import com.cappielloantonio.play.util.MusicUtil;
import com.cappielloantonio.play.viewmodel.AlbumListPageViewModel;

public class AlbumListPageFragment extends Fragment {
    private FragmentAlbumListPageBinding bind;

    private MainActivity activity;
    private AlbumListPageViewModel albumListPageViewModel;
    private AlbumHorizontalAdapter albumHorizontalAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();

        bind = FragmentAlbumListPageBinding.inflate(inflater, container, false);
        View view = bind.getRoot();
        albumListPageViewModel = new ViewModelProvider(requireActivity()).get(AlbumListPageViewModel.class);

        init();
        initAppBar();
        initAlbumListView();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
    }

    private void init() {
        if (requireArguments().getString(Album.RECENTLY_PLAYED) != null) {
            albumListPageViewModel.title = Album.RECENTLY_PLAYED;
            bind.pageTitleLabel.setText(R.string.album_list_page_recently_played);
        } else if (requireArguments().getString(Album.MOST_PLAYED) != null) {
            albumListPageViewModel.title = Album.MOST_PLAYED;
            bind.pageTitleLabel.setText(R.string.album_list_page_most_played);
        } else if (requireArguments().getString(Album.RECENTLY_ADDED) != null) {
            albumListPageViewModel.title = Album.RECENTLY_ADDED;
            bind.pageTitleLabel.setText(R.string.album_list_page_recently_added);
        } else if (requireArguments().getString(Album.STARRED) != null) {
            albumListPageViewModel.title = Album.STARRED;
            bind.pageTitleLabel.setText(R.string.album_list_page_starred);
        } else if (requireArguments().getString(Album.NEW_RELEASES) != null) {
            albumListPageViewModel.title = Album.NEW_RELEASES;
            bind.pageTitleLabel.setText(R.string.album_list_page_new_releases);
        } else if (requireArguments().getString(Album.DOWNLOADED) != null) {
            albumListPageViewModel.title = Album.DOWNLOADED;
            bind.pageTitleLabel.setText(R.string.album_list_page_downloaded);
        } else if (requireArguments().getParcelable("artist_object") != null) {
            albumListPageViewModel.artist = requireArguments().getParcelable("artist_object");
            albumListPageViewModel.title = Album.FROM_ARTIST;
            bind.pageTitleLabel.setText(MusicUtil.getReadableString(albumListPageViewModel.artist.getName()));
        }
    }

    private void initAppBar() {
        activity.setSupportActionBar(bind.toolbar);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        bind.toolbar.setNavigationOnClickListener(v -> activity.navController.navigateUp());

        bind.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if ((bind.albumInfoSector.getHeight() + verticalOffset) < (2 * ViewCompat.getMinimumHeight(bind.toolbar))) {
                bind.toolbar.setTitle(R.string.album_list_page_title);
            } else {
                bind.toolbar.setTitle(R.string.empty_string);
            }
        });
    }

    private void initAlbumListView() {
        bind.albumListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bind.albumListRecyclerView.setHasFixedSize(true);

        albumHorizontalAdapter = new AlbumHorizontalAdapter(requireContext(),
                (albumListPageViewModel.title.equals(Album.DOWNLOADED) || albumListPageViewModel.title.equals(Album.FROM_ARTIST))
        );

        bind.albumListRecyclerView.setAdapter(albumHorizontalAdapter);
        albumListPageViewModel.getAlbumList(requireActivity()).observe(getViewLifecycleOwner(), albums -> albumHorizontalAdapter.setItems(albums));
    }
}