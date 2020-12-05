package com.cappielloantonio.play.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.cappielloantonio.play.database.AppDatabase;
import com.cappielloantonio.play.database.dao.QueueDao;
import com.cappielloantonio.play.model.Queue;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.util.QueueUtil;

import java.util.ArrayList;
import java.util.List;

public class QueueRepository {
    private static final String TAG = "QueueRepository";

    private QueueDao queueDao;
    private LiveData<List<Song>> listLiveQueue;

    public QueueRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        queueDao = database.queueDao();
    }

    public LiveData<List<Song>> getLiveQueue() {
        listLiveQueue = queueDao.getAll();
        return listLiveQueue;
    }

    public List<Song> getSongs() {
        List<Song> songs = new ArrayList<>();

        GetSongsThreadSafe getSongs = new GetSongsThreadSafe(queueDao);
        Thread thread = new Thread(getSongs);
        thread.start();

        try {
            thread.join();
            songs = getSongs.getSongs();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return songs;
    }

    public void insert(Song song) {
        InsertThreadSafe insert = new InsertThreadSafe(queueDao, song);
        Thread thread = new Thread(insert);
        thread.start();
    }

    public void insertAll(List<Song> songs) {
        InsertAllThreadSafe insertAll = new InsertAllThreadSafe(queueDao, songs);
        Thread thread = new Thread(insertAll);
        thread.start();
    }

    public void insertAllAndStartNew(List<Song> songs) {
        try {
            final Thread delete = new Thread(new DeleteAllThreadSafe(queueDao));
            final Thread insertAll = new Thread(new InsertAllThreadSafe(queueDao, songs));

            delete.start();
            delete.join();
            insertAll.start();
            insertAll.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void delete(Queue queueElement) {
        DeleteThreadSafe delete = new DeleteThreadSafe(queueDao, queueElement);
        Thread thread = new Thread(delete);
        thread.start();
    }

    public void deleteAll() {
        DeleteAllThreadSafe delete = new DeleteAllThreadSafe(queueDao);
        Thread thread = new Thread(delete);
        thread.start();
    }

    public int count() {
        int count = 0;

        CountThreadSafe countThread = new CountThreadSafe(queueDao);
        Thread thread = new Thread(countThread);
        thread.start();

        try {
            thread.join();
            count = countThread.getCount();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return count;
    }

    private static class InsertThreadSafe implements Runnable {
        private QueueDao queueDao;
        private Song song;

        public InsertThreadSafe(QueueDao queueDao, Song song) {
            this.queueDao = queueDao;
            this.song = song;
        }

        @Override
        public void run() {
            queueDao.insert(QueueUtil.getQueueElementFromSong(song));
        }
    }

    private static class InsertAllThreadSafe implements Runnable {
        private QueueDao queueDao;
        private List<Song> songs;

        public InsertAllThreadSafe(QueueDao queueDao, List<Song> songs) {
            this.queueDao = queueDao;
            this.songs = songs;
        }

        @Override
        public void run() {
            queueDao.insertAll(QueueUtil.getQueueElementsFromSongs(songs));
        }
    }

    private static class DeleteThreadSafe implements Runnable {
        private QueueDao queueDao;
        private Queue queueElement;

        public DeleteThreadSafe(QueueDao queueDao, Queue queueElement) {
            this.queueDao = queueDao;
            this.queueElement = queueElement;
        }

        @Override
        public void run() {
            queueDao.delete(queueElement);
        }
    }

    private static class DeleteAllThreadSafe implements Runnable {
        private QueueDao queueDao;

        public DeleteAllThreadSafe(QueueDao queueDao) {
            this.queueDao = queueDao;
        }

        @Override
        public void run() {
            queueDao.deleteAll();
        }
    }

    private static class CountThreadSafe implements Runnable {
        private QueueDao queueDao;
        private int count = 0;

        public CountThreadSafe(QueueDao queueDao) {
            this.queueDao = queueDao;
        }

        @Override
        public void run() {
            count = queueDao.count();
        }

        public int getCount() {
            return count;
        }
    }

    private static class GetSongsThreadSafe implements Runnable {
        private QueueDao queueDao;
        private List<Song> songs;

        public GetSongsThreadSafe(QueueDao queueDao) {
            this.queueDao = queueDao;
        }

        @Override
        public void run() {
            songs = queueDao.getAllSimple();
        }

        public List<Song> getSongs() {
            return songs;
        }
    }
}
