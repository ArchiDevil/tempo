package com.cappielloantonio.play.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.cappielloantonio.play.model.Album;
import com.cappielloantonio.play.model.Artist;
import com.cappielloantonio.play.model.Media;
import com.cappielloantonio.play.repository.AlbumRepository;
import com.cappielloantonio.play.repository.ArtistRepository;
import com.cappielloantonio.play.repository.SongRepository;

public class RatingViewModel extends AndroidViewModel {
    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;

    private Media song;
    private Album album;
    private Artist artist;

    public RatingViewModel(@NonNull Application application) {
        super(application);

        songRepository = new SongRepository(application);
        albumRepository = new AlbumRepository(application);
        artistRepository = new ArtistRepository(application);
    }

    public Media getSong() {
        return song;
    }

    public LiveData<Media> getLiveSong() {
        return songRepository.getSong(song.getId());
    }

    public void setSong(Media song) {
        this.song = song;
        this.album = null;
        this.artist = null;
    }

    public Album getAlbum() {
        return album;
    }

    public LiveData<Album> getLiveAlbum() {
        return albumRepository.getAlbum(album.getId());
    }

    public void setAlbum(Album album) {
        this.song = null;
        this.album = album;
        this.artist = null;
    }

    public Artist getArtist() {
        return artist;
    }

    public LiveData<Artist> getLiveArtist() {
        return artistRepository.getArtist(artist.getId());
    }

    public void setArtist(Artist artist) {
        this.song = null;
        this.album = null;
        this.artist = artist;
    }

    public void rate(int star) {
        if (song != null) {
            songRepository.setRating(song.getId(), star);
        } else if (album != null) {
            albumRepository.setRating(album.getId(), star);
        } else if (artist != null) {
            artistRepository.setRating(artist.getId(), star);
        }
    }
}
