package com.cappielloantonio.play.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.viewpager2.widget.ViewPager2;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.adapter.AlbumAdapter;
import com.cappielloantonio.play.adapter.AlbumHorizontalAdapter;
import com.cappielloantonio.play.adapter.ArtistHorizontalAdapter;
import com.cappielloantonio.play.adapter.DiscoverSongAdapter;
import com.cappielloantonio.play.adapter.RecentMusicAdapter;
import com.cappielloantonio.play.adapter.SongHorizontalAdapter;
import com.cappielloantonio.play.adapter.YearAdapter;
import com.cappielloantonio.play.databinding.FragmentHomeBinding;
import com.cappielloantonio.play.interfaces.MediaCallback;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.ui.activity.MainActivity;
import com.cappielloantonio.play.viewmodel.HomeViewModel;

import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "CategoriesFragment";

    private FragmentHomeBinding bind;
    private MainActivity activity;
    private HomeViewModel homeViewModel;

    private YearAdapter yearAdapter;
    private SongHorizontalAdapter starredSongAdapter;
    private AlbumHorizontalAdapter starredAlbumAdapter;
    private ArtistHorizontalAdapter starredArtistAdapter;
    private RecentMusicAdapter dowanloadedMusicAdapter;

    // ---------------------------------------------------- SUBSONIC ADAPTER
    private DiscoverSongAdapter discoverSongAdapter;
    private AlbumAdapter recentlyAddedAlbumAdapter;
    private AlbumAdapter recentlyPlayedAlbumAdapter;
    private AlbumAdapter mostPlayedAlbumAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();

        bind = FragmentHomeBinding.inflate(inflater, container, false);
        View view = bind.getRoot();
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        init();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initDiscoverSongSlideView();
        initMostPlayedAlbumView();
        initRecentPlayedAlbumView();
        initStarredTracksView();
        initStarredAlbumsView();
        initStarredArtistsView();
        initYearSongView();
        initRecentAddedAlbumView();
        initDownloadedSongView();
    }

    @Override
    public void onStart() {
        super.onStart();
        activity.setBottomNavigationBarVisibility(true);
        activity.setBottomSheetVisibility(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
    }

    private void init() {
        bind.recentlyAddedAlbumsTextViewClickable.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(Song.RECENTLY_ADDED, Song.RECENTLY_ADDED);
            activity.navController.navigate(R.id.action_homeFragment_to_songListPageFragment, bundle);
        });

        bind.recentlyPlayedAlbumsTextViewClickable.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(Song.RECENTLY_PLAYED, Song.RECENTLY_PLAYED);
            activity.navController.navigate(R.id.action_homeFragment_to_songListPageFragment, bundle);
        });

        bind.mostPlayedAlbumsTextViewClickable.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(Song.MOST_PLAYED, Song.MOST_PLAYED);
            activity.navController.navigate(R.id.action_homeFragment_to_songListPageFragment, bundle);
        });

        bind.starredTracksTextViewClickable.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(Song.IS_FAVORITE, Song.IS_FAVORITE);
            activity.navController.navigate(R.id.action_homeFragment_to_songListPageFragment, bundle);
        });

        bind.starredAlbumsTextViewClickable.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(Song.IS_FAVORITE, Song.IS_FAVORITE);
            activity.navController.navigate(R.id.action_homeFragment_to_songListPageFragment, bundle);
        });

        bind.starredArtistsTextViewClickable.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(Song.IS_FAVORITE, Song.IS_FAVORITE);
            activity.navController.navigate(R.id.action_homeFragment_to_songListPageFragment, bundle);
        });

        bind.downloadedTracksTextViewClickable.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(Song.DOWNLOADED, Song.DOWNLOADED);
            activity.navController.navigate(R.id.action_homeFragment_to_songListPageFragment, bundle);
        });

        bind.homeSettingsImageView.setOnClickListener(v -> activity.navController.navigate(R.id.action_homeFragment_to_settingsFragment));
    }

    private void initDiscoverSongSlideView() {
        bind.discoverSongViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        homeViewModel.getSongRepository().getRandomSample(10, new MediaCallback() {
            @Override
            public void onError(Exception exception) {
                Toast.makeText(requireContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoadMedia(List<?> media) {
                discoverSongAdapter = new DiscoverSongAdapter(activity, requireContext(), (List<Song>) media);
                bind.discoverSongViewPager.setAdapter(discoverSongAdapter);
                bind.discoverSongViewPager.setOffscreenPageLimit(3);
                setDiscoverSongSlideViewOffset(20, 16);
            }
        });
    }

    private void initMostPlayedAlbumView() {
        bind.mostPlayedAlbumsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        bind.mostPlayedAlbumsRecyclerView.setHasFixedSize(true);

        mostPlayedAlbumAdapter = new AlbumAdapter(requireContext());
        bind.mostPlayedAlbumsRecyclerView.setAdapter(mostPlayedAlbumAdapter);
        homeViewModel.getMostPlayedAlbums().observe(requireActivity(), albums -> {
            if(albums.size() < 10) reorder();
            if(bind != null) bind.homeMostPlayedAlbumsSector.setVisibility(!albums.isEmpty() ? View.VISIBLE : View.GONE);
            mostPlayedAlbumAdapter.setItems(albums);
        });
    }

    private void initRecentPlayedAlbumView() {
        bind.recentlyPlayedAlbumsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        bind.recentlyPlayedAlbumsRecyclerView.setHasFixedSize(true);

        recentlyPlayedAlbumAdapter = new AlbumAdapter(requireContext());
        bind.recentlyPlayedAlbumsRecyclerView.setAdapter(recentlyPlayedAlbumAdapter);
        homeViewModel.getRecentlyPlayedAlbumList().observe(requireActivity(), albums -> {
            if(bind != null) bind.homeRecentlyPlayedAlbumsSector.setVisibility(!albums.isEmpty() ? View.VISIBLE : View.GONE);
            recentlyPlayedAlbumAdapter.setItems(albums);
        });
    }

    private void initYearSongView() {
        bind.yearsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        bind.yearsRecyclerView.setHasFixedSize(true);

        yearAdapter = new YearAdapter(requireContext(), homeViewModel.getYearList());
        yearAdapter.setClickListener((view, position) -> {
            Bundle bundle = new Bundle();
            bundle.putString(Song.BY_YEAR, Song.BY_YEAR);
            bundle.putInt("year_object", yearAdapter.getItem(position));
            activity.navController.navigate(R.id.action_homeFragment_to_songListPageFragment, bundle);
        });
        bind.yearsRecyclerView.setAdapter(yearAdapter);
    }

    private void initStarredTracksView() {
        bind.starredTracksRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 5, GridLayoutManager.HORIZONTAL, false));
        bind.starredTracksRecyclerView.setHasFixedSize(true);

        starredSongAdapter = new SongHorizontalAdapter(activity, requireContext(), getChildFragmentManager());
        bind.starredTracksRecyclerView.setAdapter(starredSongAdapter);
        homeViewModel.getStarredTracks().observe(requireActivity(), songs -> {
            if(bind != null) bind.homeStarredTracksSector.setVisibility(!songs.isEmpty() ? View.VISIBLE : View.GONE);
            starredSongAdapter.setItems(songs);
        });

        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(bind.starredTracksRecyclerView);
    }

    private void initStarredAlbumsView() {
        bind.starredAlbumsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 5, GridLayoutManager.HORIZONTAL, false));
        bind.starredAlbumsRecyclerView.setHasFixedSize(true);

        starredAlbumAdapter = new AlbumHorizontalAdapter(activity, requireContext(), getChildFragmentManager());
        bind.starredAlbumsRecyclerView.setAdapter(starredAlbumAdapter);
        homeViewModel.getStarredAlbums().observe(requireActivity(), albums -> {
            if(bind != null) bind.homeStarredAlbumsSector.setVisibility(!albums.isEmpty() ? View.VISIBLE : View.GONE);
            starredAlbumAdapter.setItems(albums);
        });

        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(bind.starredAlbumsRecyclerView);
    }

    private void initStarredArtistsView() {
        bind.starredArtistsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 5, GridLayoutManager.HORIZONTAL, false));
        bind.starredArtistsRecyclerView.setHasFixedSize(true);

        starredArtistAdapter = new ArtistHorizontalAdapter(activity, requireContext(), getChildFragmentManager());
        bind.starredArtistsRecyclerView.setAdapter(starredArtistAdapter);
        homeViewModel.getStarredArtists().observe(requireActivity(), artists -> {
            if(bind != null) bind.homeStarredArtistsSector.setVisibility(!artists.isEmpty() ? View.VISIBLE : View.GONE);
            starredArtistAdapter.setItems(artists);
        });

        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(bind.starredArtistsRecyclerView);
    }

    private void initRecentAddedAlbumView() {
        bind.recentlyAddedAlbumsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        bind.recentlyAddedAlbumsRecyclerView.setHasFixedSize(true);

        recentlyAddedAlbumAdapter = new AlbumAdapter(requireContext());
        bind.recentlyAddedAlbumsRecyclerView.setAdapter(recentlyAddedAlbumAdapter);
        homeViewModel.getMostRecentlyAddedAlbums().observe(requireActivity(), albums -> {
            recentlyAddedAlbumAdapter.setItems(albums);
        });
    }

    private void initDownloadedSongView() {
        bind.downloadedTracksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        bind.downloadedTracksRecyclerView.setHasFixedSize(true);

        dowanloadedMusicAdapter = new RecentMusicAdapter(activity, requireContext(), getChildFragmentManager());
        bind.downloadedTracksRecyclerView.setAdapter(dowanloadedMusicAdapter);
        homeViewModel.getDownloaded().observe(requireActivity(), songs -> {
            if(bind != null) bind.homeDownloadedTracksSector.setVisibility(!songs.isEmpty() ? View.VISIBLE : View.GONE);
            dowanloadedMusicAdapter.setItems(songs);
        });
    }

    private void setDiscoverSongSlideViewOffset(float pageOffset, float pageMargin) {
        bind.discoverSongViewPager.setPageTransformer((page, position) -> {
            float myOffset = position * -(2 * pageOffset + pageMargin);
            if (bind.discoverSongViewPager.getOrientation() == ViewPager2.ORIENTATION_HORIZONTAL) {
                if (ViewCompat.getLayoutDirection(bind.discoverSongViewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    page.setTranslationX(-myOffset);
                } else {
                    page.setTranslationX(myOffset);
                }
            } else {
                page.setTranslationY(myOffset);
            }
        });
    }

    /*
     * Il layout di default prevede questa sequenza:
     * - Discovery - Most_played - Last_played - Year - Favorite - Downloaded - Recently_added
     *
     * Se però non ho ancora ascoltato nessuna canzone e quindi Most_played e Last_played sono vuoti, modifico come segue
     * - Discovery - Recently_added - Year - Favorite - Downloaded - Most_played - Last_played
     */
    public void reorder() {
        if(bind != null) {
            bind.homeLinearLayoutContainer.removeAllViews();
            bind.homeLinearLayoutContainer.addView(bind.homeDiscoverSector);
            bind.homeLinearLayoutContainer.addView(bind.homeRecentlyAddedAlbumsSector);
            bind.homeLinearLayoutContainer.addView(bind.homeFlashbackSector);
            bind.homeLinearLayoutContainer.addView(bind.homeStarredTracksSector);
            bind.homeLinearLayoutContainer.addView(bind.homeStarredAlbumsSector);
            bind.homeLinearLayoutContainer.addView(bind.homeStarredArtistsSector);
            bind.homeLinearLayoutContainer.addView(bind.homeDownloadedTracksSector);
            bind.homeLinearLayoutContainer.addView(bind.homeMostPlayedAlbumsSector);
            bind.homeLinearLayoutContainer.addView(bind.homeRecentlyPlayedAlbumsSector);
        }
    }
}
