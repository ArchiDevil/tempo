package com.cappielloantonio.play.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cappielloantonio.play.model.Media;
import com.cappielloantonio.play.model.Playlist;
import com.cappielloantonio.play.repository.PlaylistRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistChooserViewModel extends AndroidViewModel {
    private final PlaylistRepository playlistRepository;

    private final MutableLiveData<List<Playlist>> playlists = new MutableLiveData<>(null);
    private Media toAdd;

    public PlaylistChooserViewModel(@NonNull Application application) {
        super(application);

        playlistRepository = new PlaylistRepository(application);
    }

    public LiveData<List<Playlist>> getPlaylistList(LifecycleOwner owner) {
        playlistRepository.getPlaylists(false, -1).observe(owner, playlists::postValue);
        return playlists;
    }

    public void addSongToPlaylist(String playlistId) {
        playlistRepository.addSongToPlaylist(playlistId, new ArrayList(Collections.singletonList(toAdd.getId())));
    }

    public void setSongToAdd(Media song) {
        toAdd = song;
    }

    public Media getSongToAdd() {
        return toAdd;
    }
}
