package com.cappielloantonio.play.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.cappielloantonio.play.database.AppDatabase;
import com.cappielloantonio.play.database.dao.AlbumDao;
import com.cappielloantonio.play.model.Album;

import java.util.ArrayList;
import java.util.List;

public class AlbumRepository {
    private AlbumDao albumDao;
    private LiveData<List<Album>> listLiveAlbums;

    public AlbumRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        albumDao = database.albumDao();
        listLiveAlbums = albumDao.getAll();
    }

    public LiveData<List<Album>> getListLiveAlbums() {
        return listLiveAlbums;
    }

    public boolean exist(Album album) {
        boolean exist = false;

        ExistThreadSafe existThread = new ExistThreadSafe(albumDao, album);
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

    public void insert(Album album) {
        InsertThreadSafe insert = new InsertThreadSafe(albumDao, album);
        Thread thread = new Thread(insert);
        thread.start();
    }

    public void insertAll(ArrayList<Album> albums) {
        InsertAllThreadSafe insertAll = new InsertAllThreadSafe(albumDao, albums);
        Thread thread = new Thread(insertAll);
        thread.start();
    }

    public void delete(Album album) {
        DeleteThreadSafe delete = new DeleteThreadSafe(albumDao, album);
        Thread thread = new Thread(delete);
        thread.start();
    }

    private static class ExistThreadSafe implements Runnable {
        private AlbumDao albumDao;
        private Album album;
        private boolean exist = false;

        public ExistThreadSafe(AlbumDao albumDao, Album album) {
            this.albumDao = albumDao;
            this.album = album;
        }

        @Override
        public void run() {
            exist = albumDao.exist(album.getId());
        }

        public boolean exist() {
            return exist;
        }
    }

    private static class InsertThreadSafe implements Runnable {
        private AlbumDao albumDao;
        private Album album;

        public InsertThreadSafe(AlbumDao albumDao, Album album) {
            this.albumDao = albumDao;
            this.album = album;
        }

        @Override
        public void run() {
            albumDao.insert(album);
        }
    }

    private static class InsertAllThreadSafe implements Runnable {
        private AlbumDao albumDao;
        private ArrayList<Album> albums;

        public InsertAllThreadSafe(AlbumDao albumDao, ArrayList<Album> albums) {
            this.albumDao = albumDao;
            this.albums = albums;
        }

        @Override
        public void run() {
            albumDao.insertAll(albums);
        }
    }

    private static class DeleteThreadSafe implements Runnable {
        private AlbumDao albumDao;
        private Album album;

        public DeleteThreadSafe(AlbumDao albumDao, Album album) {
            this.albumDao = albumDao;
            this.album = album;
        }

        @Override
        public void run() {
            albumDao.delete(album);
        }
    }
}
