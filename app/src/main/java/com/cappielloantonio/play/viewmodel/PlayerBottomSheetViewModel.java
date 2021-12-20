package com.cappielloantonio.play.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cappielloantonio.play.model.Artist;
import com.cappielloantonio.play.model.Queue;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.repository.ArtistRepository;
import com.cappielloantonio.play.repository.QueueRepository;
import com.cappielloantonio.play.repository.SongRepository;
import com.cappielloantonio.play.service.MusicPlayerRemote;
import com.cappielloantonio.play.util.DownloadUtil;
import com.cappielloantonio.play.util.PreferenceUtil;

import java.util.Collections;
import java.util.List;

public class PlayerBottomSheetViewModel extends AndroidViewModel {
    private static final String TAG = "PlayerBottomSheetViewModel";

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final QueueRepository queueRepository;

    private final MutableLiveData<String> lyricsLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<Song> songLiveData = new MutableLiveData<>(null);

    private final LiveData<List<Queue>> queueSong;

    public PlayerBottomSheetViewModel(@NonNull Application application) {
        super(application);

        songRepository = new SongRepository(application);
        artistRepository = new ArtistRepository(application);
        queueRepository = new QueueRepository(application);

        queueSong = queueRepository.getLiveQueue();
    }

    public LiveData<List<Queue>> getQueueSong() {
        return queueSong;
    }

    public Song getCurrentSong() {
        return MusicPlayerRemote.getCurrentSong();
    }

    public void setFavorite(Context context) {
        Song song = getCurrentSong();

        if (song != null) {
            if (song.isFavorite()) {
                songRepository.unstar(song.getId());
                song.setFavorite(false);
            } else {
                songRepository.star(song.getId());
                song.setFavorite(true);

                if(PreferenceUtil.getInstance(context).isStarredSyncEnabled()) {
                    DownloadUtil.getDownloadTracker(context).download(Collections.singletonList(song), null, null);
                }
            }
        }
    }

    public void orderSongAfterSwap(List<Song> songs) {
        queueRepository.insertAllAndStartNew(songs);
    }

    public void removeSong(int position) {
        queueRepository.deleteByPosition(position);
    }

    public LiveData<Artist> getArtist() {
        Song song = getCurrentSong();
        return artistRepository.getArtist(song.getArtistId());
    }

    public LiveData<Song> getLiveSong() {
        return songLiveData;
    }

    public LiveData<String> getLiveLyrics() {
        return lyricsLiveData;
    }

    public void refreshSongInfo(LifecycleOwner owner, Song song) {
        songLiveData.postValue(song);
        songRepository.getSongLyrics(song).observe(owner, lyricsLiveData::postValue);
    }

}
