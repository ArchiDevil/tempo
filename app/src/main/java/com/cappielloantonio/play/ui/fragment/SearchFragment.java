package com.cappielloantonio.play.ui.fragment;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.session.MediaBrowser;
import androidx.media3.session.SessionToken;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.adapter.AlbumAdapter;
import com.cappielloantonio.play.adapter.ArtistAdapter;
import com.cappielloantonio.play.adapter.SongHorizontalAdapter;
import com.cappielloantonio.play.databinding.FragmentSearchBinding;
import com.cappielloantonio.play.helper.recyclerview.CustomLinearSnapHelper;
import com.cappielloantonio.play.service.MediaService;
import com.cappielloantonio.play.ui.activity.MainActivity;
import com.cappielloantonio.play.util.MusicUtil;
import com.cappielloantonio.play.viewmodel.SearchViewModel;
import com.google.android.material.elevation.SurfaceColors;
import com.google.common.util.concurrent.ListenableFuture;
import com.paulrybitskyi.persistentsearchview.adapters.model.SuggestionItem;
import com.paulrybitskyi.persistentsearchview.listeners.OnSuggestionChangeListener;
import com.paulrybitskyi.persistentsearchview.utils.SuggestionCreationUtil;

public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";

    private FragmentSearchBinding bind;
    private MainActivity activity;
    private SearchViewModel searchViewModel;

    private ArtistAdapter artistAdapter;
    private AlbumAdapter albumAdapter;
    private SongHorizontalAdapter songHorizontalAdapter;

    private ListenableFuture<MediaBrowser> mediaBrowserListenableFuture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();

        bind = FragmentSearchBinding.inflate(inflater, container, false);
        View view = bind.getRoot();
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        initSearchResultView();
        initSearchView();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        initializeMediaBrowser();
        activity.setBottomNavigationBarVisibility(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setMediaBrowserListenableFuture();
        inputFocus();
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

    private void initSearchResultView() {
        // Artists
        bind.searchResultArtistRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        bind.searchResultArtistRecyclerView.setHasFixedSize(true);

        artistAdapter = new ArtistAdapter((MainActivity) requireActivity(), requireContext());
        bind.searchResultArtistRecyclerView.setAdapter(artistAdapter);

        CustomLinearSnapHelper artistSnapHelper = new CustomLinearSnapHelper();
        artistSnapHelper.attachToRecyclerView(bind.searchResultArtistRecyclerView);

        // Albums
        bind.searchResultAlbumRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        bind.searchResultAlbumRecyclerView.setHasFixedSize(true);

        albumAdapter = new AlbumAdapter(requireContext());
        bind.searchResultAlbumRecyclerView.setAdapter(albumAdapter);

        CustomLinearSnapHelper albumSnapHelper = new CustomLinearSnapHelper();
        albumSnapHelper.attachToRecyclerView(bind.searchResultAlbumRecyclerView);

        // Songs
        bind.searchResultTracksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bind.searchResultTracksRecyclerView.setHasFixedSize(true);

        songHorizontalAdapter = new SongHorizontalAdapter(activity, requireContext(), true);
        bind.searchResultTracksRecyclerView.setAdapter(songHorizontalAdapter);
    }

    private void initSearchView() {
        bind.persistentSearchView.setCardBackgroundColor(SurfaceColors.getColorForElevation(requireContext(), 2));

        if (isQueryValid(searchViewModel.getQuery())) {
            search(searchViewModel.getQuery());
        }

        bind.persistentSearchView.setInputQuery(searchViewModel.getQuery());
        setSuggestions();

        bind.persistentSearchView.setOnSearchQueryChangeListener((searchView, oldQuery, newQuery) -> {
            if (!newQuery.trim().equals("") && newQuery.trim().length() > 1) {
                searchViewModel.getSearchSuggestion(newQuery).observe(requireActivity(), suggestions -> searchView.setSuggestions(SuggestionCreationUtil.asRegularSearchSuggestions(MusicUtil.getReadableStrings(suggestions)), false));
            } else {
                setSuggestions();
            }
        });

        bind.persistentSearchView.setOnSuggestionChangeListener(new OnSuggestionChangeListener() {
            @Override
            public void onSuggestionPicked(SuggestionItem suggestion) {
                search(suggestion.getItemModel().getText());
            }

            @Override
            public void onSuggestionRemoved(SuggestionItem suggestion) {
            }
        });

        bind.persistentSearchView.setOnSearchConfirmedListener((searchView, query) -> {
            if (isQueryValid(query)) {
                searchView.collapse();
                search(query);
            } else {
                Toast.makeText(requireContext(), getString(R.string.search_info_minimum_characters), Toast.LENGTH_SHORT).show();
            }
        });

        bind.persistentSearchView.setOnSuggestionChangeListener(new OnSuggestionChangeListener() {
            @Override
            public void onSuggestionPicked(SuggestionItem suggestion) {
                search(suggestion.getItemModel().getText());
            }

            @Override
            public void onSuggestionRemoved(SuggestionItem suggestion) {
                searchViewModel.deleteRecentSearch(suggestion.getItemModel().getText());
            }
        });

        bind.persistentSearchView.setOnClearInputBtnClickListener(v -> searchViewModel.setQuery(""));
    }

    private void setSuggestions() {
        bind.persistentSearchView.setSuggestions(SuggestionCreationUtil.asRecentSearchSuggestions(searchViewModel.getRecentSearchSuggestion()), false);
    }

    public void search(String query) {
        searchViewModel.setQuery(query);

        bind.persistentSearchView.setInputQuery(query);
        performSearch(query);
    }

    private void performSearch(String query) {
        searchViewModel.searchArtist(query).observe(requireActivity(), artists -> {
            if (bind != null)
                bind.searchArtistSector.setVisibility(!artists.isEmpty() ? View.VISIBLE : View.GONE);
            artistAdapter.setItems(artists);
        });

        searchViewModel.searchAlbum(query).observe(requireActivity(), albums -> {
            if (bind != null)
                bind.searchAlbumSector.setVisibility(!albums.isEmpty() ? View.VISIBLE : View.GONE);
            albumAdapter.setItems(albums);
        });

        searchViewModel.searchSong(query).observe(requireActivity(), songs -> {
            if (bind != null)
                bind.searchSongSector.setVisibility(!songs.isEmpty() ? View.VISIBLE : View.GONE);
            songHorizontalAdapter.setItems(songs);
        });

        bind.searchResultLayout.setVisibility(View.VISIBLE);
    }

    private boolean isQueryValid(String query) {
        return !query.equals("") && query.trim().length() > 2;
    }

    private void inputFocus() {
        if (!isQueryValid(searchViewModel.getQuery())) {
            bind.persistentSearchView.expand();
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void initializeMediaBrowser() {
        mediaBrowserListenableFuture = new MediaBrowser.Builder(requireContext(), new SessionToken(requireContext(), new ComponentName(requireContext(), MediaService.class))).buildAsync();
    }

    private void releaseMediaBrowser() {
        MediaBrowser.releaseFuture(mediaBrowserListenableFuture);
    }

    private void setMediaBrowserListenableFuture() {
        songHorizontalAdapter.setMediaBrowserListenableFuture(mediaBrowserListenableFuture);
    }
}
