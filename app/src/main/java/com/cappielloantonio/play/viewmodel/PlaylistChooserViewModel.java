package com.cappielloantonio.play.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cappielloantonio.play.model.Playlist;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.repository.PlaylistRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistChooserViewModel extends AndroidViewModel {
    private PlaylistRepository playlistRepository;

    private MutableLiveData<List<Playlist>> playlists;
    private Song toAdd;

    public PlaylistChooserViewModel(@NonNull Application application) {
        super(application);

        playlistRepository = new PlaylistRepository(application);

        playlists = playlistRepository.getPlaylists(false, -1);
    }

    public LiveData<List<Playlist>> getPlaylistList() {
        return playlists;
    }

    public void addSongToPlaylist(String playlistId) {
        playlistRepository.addSongToPlaylist(playlistId, new ArrayList(Collections.singletonList(toAdd.getId())));
    }

    public void setSongToAdd(Song song) {
        toAdd = song;
    }

    public Song getSongToAdd() {
        return toAdd;
    }
}
