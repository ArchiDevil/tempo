package com.cappielloantonio.play.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cappielloantonio.play.App;
import com.cappielloantonio.play.model.Album;
import com.cappielloantonio.play.model.Artist;
import com.cappielloantonio.play.model.Playlist;
import com.cappielloantonio.play.model.PodcastEpisode;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.repository.AlbumRepository;
import com.cappielloantonio.play.repository.ArtistRepository;
import com.cappielloantonio.play.repository.PlaylistRepository;
import com.cappielloantonio.play.repository.PodcastRepository;
import com.cappielloantonio.play.repository.SongRepository;
import com.cappielloantonio.play.subsonic.models.NewestPodcasts;
import com.cappielloantonio.play.util.PreferenceUtil;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private static final String TAG = "HomeViewModel";

    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final PlaylistRepository playlistRepository;
    private final PodcastRepository podcastRepository;

    private final MutableLiveData<List<Song>> dicoverSongSample = new MutableLiveData<>(null);
    private final MutableLiveData<List<Album>> newReleasedAlbum = new MutableLiveData<>(null);
    private final MutableLiveData<List<Song>> starredTracksSample = new MutableLiveData<>(null);
    private final MutableLiveData<List<Artist>> starredArtistsSample = new MutableLiveData<>(null);
    private final MutableLiveData<List<Song>> starredTracks = new MutableLiveData<>(null);
    private final MutableLiveData<List<Album>> starredAlbums = new MutableLiveData<>(null);
    private final MutableLiveData<List<Artist>> starredArtists = new MutableLiveData<>(null);
    private final MutableLiveData<List<Album>> mostPlayedAlbumSample = new MutableLiveData<>(null);
    private final MutableLiveData<List<Album>> recentlyPlayedAlbumSample = new MutableLiveData<>(null);
    private final MutableLiveData<List<Integer>> years = new MutableLiveData<>(null);
    private final MutableLiveData<List<Album>> recentlyAddedAlbumSample = new MutableLiveData<>(null);
    private final MutableLiveData<List<Playlist>> pinnedPlaylists = new MutableLiveData<>(null);
    private final MutableLiveData<List<PodcastEpisode>> newestPodcastEpisodes = new MutableLiveData<>(null);

    public HomeViewModel(@NonNull Application application) {
        super(application);

        songRepository = new SongRepository(application);
        albumRepository = new AlbumRepository(application);
        artistRepository = new ArtistRepository(application);
        playlistRepository = new PlaylistRepository(application);
        podcastRepository = new PodcastRepository(application);

        songRepository.getRandomSample(10, null, null).observeForever(dicoverSongSample::postValue);
        songRepository.getStarredSongs(true, 10).observeForever(starredTracksSample::postValue);
        artistRepository.getStarredArtists(true, 10).observeForever(starredArtistsSample::postValue);
    }

    public LiveData<List<Song>> getDiscoverSongSample() {
        return dicoverSongSample;
    }

    public LiveData<List<Album>> getRecentlyReleasedAlbums(LifecycleOwner owner) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        albumRepository.getAlbums("byYear", 500, currentYear, currentYear).observe(owner, albums -> {
            albums.sort(Comparator.comparing(Album::getCreated).reversed());
            newReleasedAlbum.postValue(albums.subList(0, Math.min(20, albums.size())));
        });

        return newReleasedAlbum;
    }

    public LiveData<List<Song>> getStarredTracksSample() {
        return starredTracksSample;
    }

    public LiveData<List<Artist>> getStarredArtistsSample() {
        return starredArtistsSample;
    }

    public LiveData<List<Song>> getStarredTracks(LifecycleOwner owner) {
        songRepository.getStarredSongs(true, 20).observe(owner, starredTracks::postValue);
        return starredTracks;
    }

    public LiveData<List<Album>> getStarredAlbums(LifecycleOwner owner) {
        albumRepository.getStarredAlbums(true, 20).observe(owner, starredAlbums::postValue);
        return starredAlbums;
    }

    public LiveData<List<Artist>> getStarredArtists(LifecycleOwner owner) {
        artistRepository.getStarredArtists(true, 20).observe(owner, starredArtists::postValue);
        return starredArtists;
    }

    public LiveData<List<Integer>> getYearList(LifecycleOwner owner) {
        albumRepository.getDecades().observe(owner, years::postValue);
        return years;
    }

    public LiveData<List<Album>> getMostPlayedAlbums(LifecycleOwner owner) {
        albumRepository.getAlbums("frequent", 20, null, null).observe(owner, mostPlayedAlbumSample::postValue);
        return mostPlayedAlbumSample;
    }

    public LiveData<List<Album>> getMostRecentlyAddedAlbums(LifecycleOwner owner) {
        albumRepository.getAlbums("newest", 20, null, null).observe(owner, recentlyAddedAlbumSample::postValue);
        return recentlyAddedAlbumSample;
    }

    public LiveData<List<Album>> getRecentlyPlayedAlbumList(LifecycleOwner owner) {
        albumRepository.getAlbums("recent", 20, null, null).observe(owner, recentlyPlayedAlbumSample::postValue);
        return recentlyPlayedAlbumSample;
    }

    public LiveData<List<Playlist>> getPinnedPlaylistList(LifecycleOwner owner, int maxNumber, boolean random) {
        playlistRepository.getPinnedPlaylists(PreferenceUtil.getInstance(App.getInstance()).getServerId()).observe(owner, playlists -> {
            if (random) Collections.shuffle(playlists);
            List<Playlist> subPlaylist = playlists.subList(0, Math.min(maxNumber, playlists.size()));
            pinnedPlaylists.postValue(subPlaylist);
        });
        return pinnedPlaylists;
    }

    public LiveData<List<Song>> getPlaylistSongLiveList(String playlistId) {
        return playlistRepository.getPlaylistSongs(playlistId);
    }

    public LiveData<List<PodcastEpisode>> getNewestPodcastEpisodes(LifecycleOwner owner) {
        podcastRepository.getNewestPodcastEpisodes(20).observe(owner, newestPodcastEpisodes::postValue);
        return newestPodcastEpisodes;
    }

    public void refreshDiscoverySongSample(LifecycleOwner owner) {
        songRepository.getRandomSample(10, null, null).observe(owner, dicoverSongSample::postValue);
    }

    public void refreshSimilarSongSample(LifecycleOwner owner) {
        songRepository.getStarredSongs(true, 10).observe(owner, starredTracksSample::postValue);
    }

    public void refreshRadioArtistSample(LifecycleOwner owner) {
        artistRepository.getStarredArtists(true, 10).observe(owner, starredArtistsSample::postValue);
    }

    public void refreshStarredTracks(LifecycleOwner owner) {
        songRepository.getStarredSongs(true, 20).observe(owner, starredTracks::postValue);
    }

    public void refreshStarredAlbums(LifecycleOwner owner) {
        albumRepository.getStarredAlbums(true, 20).observe(owner, starredAlbums::postValue);
    }

    public void refreshStarredArtists(LifecycleOwner owner) {
        artistRepository.getStarredArtists(true, 20).observe(owner, starredArtists::postValue);
    }

    public void refreshMostPlayedAlbums(LifecycleOwner owner) {
        albumRepository.getAlbums("frequent", 20, null, null).observe(owner, mostPlayedAlbumSample::postValue);
    }

    public void refreshMostRecentlyAddedAlbums(LifecycleOwner owner) {
        albumRepository.getAlbums("newest", 20, null, null).observe(owner, recentlyAddedAlbumSample::postValue);
    }

    public void refreshRecentlyPlayedAlbumList(LifecycleOwner owner) {
        albumRepository.getAlbums("recent", 20, null, null).observe(owner, recentlyPlayedAlbumSample::postValue);
    }
}
