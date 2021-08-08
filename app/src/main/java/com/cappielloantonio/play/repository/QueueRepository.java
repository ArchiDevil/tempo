package com.cappielloantonio.play.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.cappielloantonio.play.App;
import com.cappielloantonio.play.database.AppDatabase;
import com.cappielloantonio.play.database.dao.QueueDao;
import com.cappielloantonio.play.model.Queue;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.util.MappingUtil;
import com.cappielloantonio.play.util.PreferenceUtil;
import com.cappielloantonio.play.util.QueueUtil;

import java.util.ArrayList;
import java.util.List;

public class QueueRepository {
    private static final String TAG = "QueueRepository";

    private QueueDao queueDao;
    private LiveData<List<Queue>> listLiveQueue;

    public QueueRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        queueDao = database.queueDao();
    }

    public LiveData<List<Queue>> getLiveQueue() {
        listLiveQueue = queueDao.getAll(PreferenceUtil.getInstance(App.getInstance()).getServerId());
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

    public void deleteByPosition(int position) {
        DeleteByPositionThreadSafe delete = new DeleteByPositionThreadSafe(queueDao, position);
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

    private static class GetSongsThreadSafe implements Runnable {
        private QueueDao queueDao;
        private List<Song> songs;

        public GetSongsThreadSafe(QueueDao queueDao) {
            this.queueDao = queueDao;
        }

        @Override
        public void run() {
            songs = MappingUtil.mapQueue(queueDao.getAllSimple(PreferenceUtil.getInstance(App.getInstance()).getServerId()));
        }

        public List<Song> getSongs() {
            return songs;
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
            queueDao.insertAll(QueueUtil.getQueueElementsFromSongs(songs, PreferenceUtil.getInstance(App.getInstance()).getServerId()));
        }
    }

    private static class DeleteByPositionThreadSafe implements Runnable {
        private QueueDao queueDao;
        private int position;

        public DeleteByPositionThreadSafe(QueueDao queueDao, int position) {
            this.queueDao = queueDao;
            this.position = position;
        }

        @Override
        public void run() {
            queueDao.deleteByPosition(position, PreferenceUtil.getInstance(App.getInstance()).getServerId());
        }
    }

    private static class DeleteAllThreadSafe implements Runnable {
        private QueueDao queueDao;

        public DeleteAllThreadSafe(QueueDao queueDao) {
            this.queueDao = queueDao;
        }

        @Override
        public void run() {
            queueDao.deleteAll(PreferenceUtil.getInstance(App.getInstance()).getServerId());
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
}
