package com.cappielloantonio.play.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.adapter.AlbumAdapter;
import com.cappielloantonio.play.adapter.ArtistAdapter;
import com.cappielloantonio.play.adapter.DiscoverSongAdapter;
import com.cappielloantonio.play.adapter.GenreAdapter;
import com.cappielloantonio.play.adapter.PlaylistAdapter;
import com.cappielloantonio.play.databinding.FragmentLibraryBinding;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.ui.activity.MainActivity;
import com.cappielloantonio.play.util.PreferenceUtil;
import com.cappielloantonio.play.viewmodel.LibraryViewModel;

public class LibraryFragment extends Fragment {
    private static final String TAG = "LibraryFragment";

    private FragmentLibraryBinding bind;
    private MainActivity activity;
    private LibraryViewModel libraryViewModel;

    private AlbumAdapter albumAdapter;
    private ArtistAdapter artistAdapter;
    private GenreAdapter genreAdapter;
    private PlaylistAdapter playlistAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();

        bind = FragmentLibraryBinding.inflate(inflater, container, false);
        View view = bind.getRoot();
        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);

        init();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initAlbumView();
        initArtistView();
        initGenreView();
        initPlaylistSlideView();
    }

    @Override
    public void onStart() {
        super.onStart();
        activity.setBottomNavigationBarVisibility(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
    }

    private void init() {
        bind.albumCatalogueTextViewClickable.setOnClickListener(v -> activity.navController.navigate(R.id.action_libraryFragment_to_albumCatalogueFragment));
        bind.artistCatalogueTextViewClickable.setOnClickListener(v -> activity.navController.navigate(R.id.action_libraryFragment_to_artistCatalogueFragment));
        bind.genreCatalogueTextViewClickable.setOnClickListener(v -> activity.navController.navigate(R.id.action_libraryFragment_to_genreCatalogueFragment));
        bind.playlistCatalogueTextViewClickable.setOnClickListener(v -> activity.navController.navigate(R.id.action_libraryFragment_to_playlistCatalogueFragment));
    }

    private void initAlbumView() {
        bind.albumRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        bind.albumRecyclerView.setHasFixedSize(true);

        albumAdapter = new AlbumAdapter(requireContext());
        bind.albumRecyclerView.setAdapter(albumAdapter);
        libraryViewModel.getAlbumSample().observe(requireActivity(), albums -> {
            if(bind != null) bind.libraryAlbumSector.setVisibility(!albums.isEmpty() ? View.VISIBLE : View.GONE);
            albumAdapter.setItems(albums);
        });
    }

    private void initArtistView() {
        bind.artistRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        bind.artistRecyclerView.setHasFixedSize(true);

        artistAdapter = new ArtistAdapter(requireContext());
        bind.artistRecyclerView.setAdapter(artistAdapter);
        libraryViewModel.getArtistSample().observe(requireActivity(), artists -> {
            if(bind != null) bind.libraryArtistSector.setVisibility(!artists.isEmpty() ? View.VISIBLE : View.GONE);
            artistAdapter.setItems(artists);
        });
    }

    private void initGenreView() {
        bind.genreRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false));
        bind.genreRecyclerView.setHasFixedSize(true);

        genreAdapter = new GenreAdapter(requireContext());
        genreAdapter.setClickListener((view, position) -> {
            Bundle bundle = new Bundle();
            bundle.putString(Song.BY_GENRE, Song.BY_GENRE);
            bundle.putParcelable("genre_object", genreAdapter.getItem(position));
            activity.navController.navigate(R.id.action_libraryFragment_to_songListPageFragment, bundle);
        });
        bind.genreRecyclerView.setAdapter(genreAdapter);
        libraryViewModel.getGenreSample().observe(requireActivity(), genres -> {
            if(bind != null) bind.libraryGenresSector.setVisibility(!genres.isEmpty() ? View.VISIBLE : View.GONE);
            genreAdapter.setItems(genres);
        });
    }

    private void initPlaylistSlideView() {
        bind.playlistViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        playlistAdapter = new PlaylistAdapter(activity, requireContext());
        bind.playlistViewPager.setAdapter(playlistAdapter);
        bind.playlistViewPager.setOffscreenPageLimit(3);
        libraryViewModel.getPlaylistSample().observe(requireActivity(), playlists -> {
            if(bind != null) bind.libraryPlaylistSector.setVisibility(!playlists.isEmpty() ? View.VISIBLE : View.GONE);
            playlistAdapter.setItems(playlists);
        });

        setDiscoverSongSlideViewOffset(20, 16);
    }

    private void setDiscoverSongSlideViewOffset(float pageOffset, float pageMargin) {
        bind.playlistViewPager.setPageTransformer((page, position) -> {
            float myOffset = position * -(2 * pageOffset + pageMargin);
            if (bind.playlistViewPager.getOrientation() == ViewPager2.ORIENTATION_HORIZONTAL) {
                if (ViewCompat.getLayoutDirection(bind.playlistViewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    page.setTranslationX(-myOffset);
                } else {
                    page.setTranslationX(myOffset);
                }
            } else {
                page.setTranslationY(myOffset);
            }
        });
    }
}
