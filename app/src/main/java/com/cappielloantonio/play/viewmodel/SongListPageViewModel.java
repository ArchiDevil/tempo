package com.cappielloantonio.play.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cappielloantonio.play.repository.ArtistRepository;
import com.cappielloantonio.play.repository.SongRepository;
import com.cappielloantonio.play.subsonic.models.AlbumID3;
import com.cappielloantonio.play.subsonic.models.ArtistID3;
import com.cappielloantonio.play.subsonic.models.Child;
import com.cappielloantonio.play.subsonic.models.Genre;
import com.cappielloantonio.play.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class SongListPageViewModel extends AndroidViewModel {
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;

    public String title;
    public String toolbarTitle;
    public Genre genre;
    public ArtistID3 artist;
    public AlbumID3 album;

    private MutableLiveData<List<Child>> songList;

    public ArrayList<String> filters = new ArrayList<>();
    public ArrayList<String> filterNames = new ArrayList<>();

    public int year = 0;

    public SongListPageViewModel(@NonNull Application application) {
        super(application);

        songRepository = new SongRepository();
        artistRepository = new ArtistRepository();
    }

    public LiveData<List<Child>> getSongList() {
        songList = new MutableLiveData<>(new ArrayList<>());

        switch (title) {
            case Constants.MEDIA_BY_GENRE:
                songList = songRepository.getSongsByGenre(genre.getGenre(), 0);
                break;
            case Constants.MEDIA_BY_ARTIST:
                songList = artistRepository.getTopSongs(artist.getName(), 50);
                break;
            case Constants.MEDIA_BY_GENRES:
                songList = songRepository.getSongsByGenres(filters);
                break;
            case Constants.MEDIA_BY_YEAR:
                songList = songRepository.getRandomSample(500, year, year + 10);
                break;
            case Constants.MEDIA_STARRED:
                songList = songRepository.getStarredSongs(false, -1);
                break;
        }

        return songList;
    }

    public void getSongsByPage(LifecycleOwner owner) {
        switch (title) {
            case Constants.MEDIA_BY_GENRE:
                int page = (songList.getValue() != null ? songList.getValue().size() : 0) / 100;
                songRepository.getSongsByGenre(genre.getGenre(), page).observe(owner, children -> {
                    List<Child> currentMedia = songList.getValue();
                    currentMedia.addAll(children);
                    songList.setValue(currentMedia);
                });
                break;
            case Constants.MEDIA_BY_ARTIST:
            case Constants.MEDIA_BY_GENRES:
            case Constants.MEDIA_BY_YEAR:
            case Constants.MEDIA_STARRED:
                break;
        }
    }

    public String getFiltersTitle() {
        return TextUtils.join(", ", filterNames);
    }
}
