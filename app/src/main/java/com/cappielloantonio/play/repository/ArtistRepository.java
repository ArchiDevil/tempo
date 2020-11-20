package com.cappielloantonio.play.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.cappielloantonio.play.database.AppDatabase;
import com.cappielloantonio.play.database.dao.ArtistDao;
import com.cappielloantonio.play.model.Artist;

import java.util.ArrayList;
import java.util.List;

public class ArtistRepository {
    private ArtistDao artistDao;
    private LiveData<List<Artist>> listLiveArtists;

    public ArtistRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        artistDao = database.artistDao();
        listLiveArtists = artistDao.getAll();
    }

    public LiveData<List<Artist>> getListLiveArtists() {
        return listLiveArtists;
    }

    public boolean exist(Artist artist) {
        boolean exist = false;

        ExistThreadSafe existThread = new ExistThreadSafe(artistDao, artist);
        Thread thread = new Thread(existThread);
        thread.start();

        try {
            thread.join();
            exist = existThread.exist();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return exist;
    }

    public void insert(Artist artist) {
        InsertThreadSafe insert = new InsertThreadSafe(artistDao, artist);
        Thread thread = new Thread(insert);
        thread.start();
    }

    public void insertAll(ArrayList<Artist> artists) {
        InsertAllThreadSafe insertAll = new InsertAllThreadSafe(artistDao, artists);
        Thread thread = new Thread(insertAll);
        thread.start();
    }

    public void delete(Artist artist) {
        DeleteThreadSafe delete = new DeleteThreadSafe(artistDao, artist);
        Thread thread = new Thread(delete);
        thread.start();
    }

    private static class ExistThreadSafe implements Runnable {
        private ArtistDao artistDao;
        private Artist artist;
        private boolean exist = false;

        public ExistThreadSafe(ArtistDao artistDao, Artist artist) {
            this.artistDao = artistDao;
            this.artist = artist;
        }

        @Override
        public void run() {
            exist = artistDao.exist(artist.getId());
        }

        public boolean exist() {
            return exist;
        }
    }

    private static class InsertThreadSafe implements Runnable {
        private ArtistDao artistDao;
        private Artist artist;

        public InsertThreadSafe(ArtistDao artistDao, Artist artist) {
            this.artistDao = artistDao;
            this.artist = artist;
        }

        @Override
        public void run() {
            artistDao.insert(artist);
        }
    }

    private static class InsertAllThreadSafe implements Runnable {
        private ArtistDao artistDao;
        private ArrayList<Artist> artists;

        public InsertAllThreadSafe(ArtistDao artistDao, ArrayList<Artist> artists) {
            this.artistDao = artistDao;
            this.artists = artists;
        }

        @Override
        public void run() {
            artistDao.insertAll(artists);
        }
    }

    private static class DeleteThreadSafe implements Runnable {
        private ArtistDao artistDao;
        private Artist artist;

        public DeleteThreadSafe(ArtistDao artistDao, Artist artist) {
            this.artistDao = artistDao;
            this.artist = artist;
        }

        @Override
        public void run() {
            artistDao.delete(artist);
        }
    }
}
