package com.cappielloantonio.play.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.cappielloantonio.play.database.AppDatabase;
import com.cappielloantonio.play.database.dao.SongDao;
import com.cappielloantonio.play.model.Album;
import com.cappielloantonio.play.model.Song;

import java.util.ArrayList;
import java.util.List;

public class SongRepository {
    private SongDao songDao;
    private LiveData<List<Song>> searchListLiveSongs;
    private LiveData<List<Song>> listLiveSampleRecentlyAddedSongs;
    private LiveData<List<Song>> listLiveSampleRecentlyPlayedSongs;
    private LiveData<List<Song>> listLiveSampleMostPlayedSongs;
    private LiveData<List<Song>> listLiveSampleArtistTopSongs;
    private LiveData<List<Song>> listLiveAlbumSongs;
    private LiveData<List<Song>> listLiveSongByGenre;


    public SongRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        songDao = database.songDao();
    }

    public LiveData<List<Song>> searchListLiveSong(String title) {
        searchListLiveSongs = songDao.searchSong(title);
        return searchListLiveSongs;
    }

    public LiveData<List<Song>> getListLiveRecentlyAddedSampleSong(int number) {
        listLiveSampleRecentlyAddedSongs = songDao.getRecentlyAddedSample(number);
        return listLiveSampleRecentlyAddedSongs;
    }

    public LiveData<List<Song>> getListLiveRecentlyPlayedSampleSong(int number) {
        listLiveSampleRecentlyPlayedSongs = songDao.getRecentlyPlayedSample(number);
        return listLiveSampleRecentlyPlayedSongs;
    }

    public LiveData<List<Song>> getListLiveMostPlayedSampleSong(int number) {
        listLiveSampleMostPlayedSongs = songDao.getMostPlayedSample(number);
        return listLiveSampleMostPlayedSongs;
    }

    public LiveData<List<Song>> getListLiveSongByGenre(String genreID) {
        listLiveSongByGenre = songDao.getSongByGenre(genreID);
        return listLiveSongByGenre;
    }

    public LiveData<List<Song>> getArtistListLiveTopSongSample(String artistID) {
        listLiveSampleArtistTopSongs = songDao.getArtistTopSongsSample(artistID, 5);
        return listLiveSampleArtistTopSongs;
    }

    public LiveData<List<Song>> getArtistListLiveTopSong(String artistID) {
        listLiveSampleArtistTopSongs = songDao.getArtistTopSongs(artistID);
        return listLiveSampleArtistTopSongs;
    }

    public LiveData<List<Song>> getAlbumListLiveSong(String albumID) {
        listLiveAlbumSongs = songDao.getAlbumSong(albumID);
        return listLiveAlbumSongs;
    }

    public boolean exist(Song song) {
        boolean exist = false;

        ExistThreadSafe existThread = new ExistThreadSafe(songDao, song);
        Thread thread = new Thread(existThread);
        thread.start();

        try {
            thread.join();
            exist = existThread.exist();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return exist;
    }

    public void insert(Song song) {
        InsertThreadSafe insert = new InsertThreadSafe(songDao, song);
        Thread thread = new Thread(insert);
        thread.start();
    }

    public void insertAll(ArrayList<Song> songs) {
        InsertAllThreadSafe insertAll = new InsertAllThreadSafe(songDao, songs);
        Thread thread = new Thread(insertAll);
        thread.start();
    }

    public void delete(Song song) {
        DeleteThreadSafe delete = new DeleteThreadSafe(songDao, song);
        Thread thread = new Thread(delete);
        thread.start();
    }

    public void update(Song song) {
        song.nowPlaying();

        UpdateThreadSafe update = new UpdateThreadSafe(songDao, song);
        Thread thread = new Thread(update);
        thread.start();
    }

    public List<Song> getRandomSample(int number) {
        List<Song> sample = new ArrayList<>();

        PickRandomThreadSafe randomThread = new PickRandomThreadSafe(songDao, number);
        Thread thread = new Thread(randomThread);
        thread.start();

        try {
            thread.join();
            sample = randomThread.getSample();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return sample;
    }

    private static class ExistThreadSafe implements Runnable {
        private SongDao songDao;
        private Song song;
        private boolean exist = false;

        public ExistThreadSafe(SongDao songDao, Song song) {
            this.songDao = songDao;
            this.song = song;
        }

        @Override
        public void run() {
            exist = songDao.exist(song.getId());
        }

        public boolean exist() {
            return exist;
        }
    }

    private static class InsertThreadSafe implements Runnable {
        private SongDao songDao;
        private Song song;

        public InsertThreadSafe(SongDao songDao, Song song) {
            this.songDao = songDao;
            this.song = song;
        }

        @Override
        public void run() {
            songDao.insert(song);
        }
    }

    private static class InsertAllThreadSafe implements Runnable {
        private SongDao songDao;
        private ArrayList<Song> songs;

        public InsertAllThreadSafe(SongDao songDao, ArrayList<Song> songs) {
            this.songDao = songDao;
            this.songs = songs;
        }

        @Override
        public void run() {
            songDao.insertAll(songs);
        }
    }

    private static class DeleteThreadSafe implements Runnable {
        private SongDao songDao;
        private Song song;

        public DeleteThreadSafe(SongDao songDao, Song song) {
            this.songDao = songDao;
            this.song = song;
        }

        @Override
        public void run() {
            songDao.delete(song);
        }
    }

    private static class UpdateThreadSafe implements Runnable {
        private SongDao songDao;
        private Song song;

        public UpdateThreadSafe(SongDao songDao, Song song) {
            this.songDao = songDao;
            this.song = song;
        }

        @Override
        public void run() {
            songDao.update(song);
        }
    }

    private static class PickRandomThreadSafe implements Runnable {
        private SongDao songDao;
        private int elementNumber;
        private List<Song> sample;

        public PickRandomThreadSafe(SongDao songDao, int number) {
            this.songDao = songDao;
            this.elementNumber = number;
        }

        @Override
        public void run() {
            sample = songDao.random(elementNumber);
        }

        public List<Song> getSample() {
            return sample;
        }
    }
}
