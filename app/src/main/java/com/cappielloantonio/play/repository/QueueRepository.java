package com.cappielloantonio.play.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.cappielloantonio.play.database.AppDatabase;
import com.cappielloantonio.play.database.dao.QueueDao;
import com.cappielloantonio.play.model.Queue;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.util.MappingUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class QueueRepository {
    private static final String TAG = "QueueRepository";

    private final QueueDao queueDao;

    public QueueRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        queueDao = database.queueDao();
    }

    public LiveData<List<Queue>> getLiveQueue() {
        return queueDao.getAll();
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

    public void insert(Song song, boolean reset, int afterIndex) {
        try {
            List<Song> songs = new ArrayList<>();

            if (!reset) {
                GetSongsThreadSafe getSongsThreadSafe = new GetSongsThreadSafe(queueDao);
                Thread getSongsThread = new Thread(getSongsThreadSafe);
                getSongsThread.start();
                getSongsThread.join();

                songs = getSongsThreadSafe.getSongs();
            }

            songs.add(afterIndex, song);

            Thread delete = new Thread(new DeleteAllThreadSafe(queueDao));
            delete.start();
            delete.join();

            Thread insertAll = new Thread(new InsertAllThreadSafe(queueDao, songs));
            insertAll.start();
            insertAll.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void insertAll(List<Song> toAdd, boolean reset, int afterIndex) {
        try {
            List<Song> songs = new ArrayList<>();

            if (!reset) {
                GetSongsThreadSafe getSongsThreadSafe = new GetSongsThreadSafe(queueDao);
                Thread getSongsThread = new Thread(getSongsThreadSafe);
                getSongsThread.start();
                getSongsThread.join();

                songs = getSongsThreadSafe.getSongs();
            }

            songs.addAll(afterIndex, toAdd);

            Thread delete = new Thread(new DeleteAllThreadSafe(queueDao));
            delete.start();
            delete.join();

            Thread insertAll = new Thread(new InsertAllThreadSafe(queueDao, songs));
            insertAll.start();
            insertAll.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void delete(int position) {
        DeleteThreadSafe delete = new DeleteThreadSafe(queueDao, position);
        Thread thread = new Thread(delete);
        thread.start();
    }

    public void deleteAll() {
        DeleteAllThreadSafe deleteAll = new DeleteAllThreadSafe(queueDao);
        Thread thread = new Thread(deleteAll);
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

    public void setLastPlayedTimestamp(String id) {
        SetLastPlayedTimestampThreadSafe timestamp = new SetLastPlayedTimestampThreadSafe(queueDao, id);
        Thread thread = new Thread(timestamp);
        thread.start();
    }

    public void setPlayingChangedTimestamp(String id, long ms) {
        SetPlayingChangedTimestampThreadSafe timestamp = new SetPlayingChangedTimestampThreadSafe(queueDao, id, ms);
        Thread thread = new Thread(timestamp);
        thread.start();
    }

    public int getLastPlayedSongIndex() {
        int index = 0;

        GetLastPlayedSongThreadSafe getLastPlayedSongThreadSafe = new GetLastPlayedSongThreadSafe(queueDao);
        Thread thread = new Thread(getLastPlayedSongThreadSafe);
        thread.start();

        try {
            thread.join();
            index = getLastPlayedSongThreadSafe.getIndex();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return index;
    }

    public long getLastPlayedSongTimestamp() {
        long timestamp = 0;

        GetLastPlayedSongTimestampThreadSafe getLastPlayedSongTimestampThreadSafe = new GetLastPlayedSongTimestampThreadSafe(queueDao);
        Thread thread = new Thread(getLastPlayedSongTimestampThreadSafe);
        thread.start();

        try {
            thread.join();
            timestamp = getLastPlayedSongTimestampThreadSafe.getTimestamp();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return timestamp;
    }

    private static class GetSongsThreadSafe implements Runnable {
        private final QueueDao queueDao;
        private List<Song> songs;

        public GetSongsThreadSafe(QueueDao queueDao) {
            this.queueDao = queueDao;
        }

        @Override
        public void run() {
            songs = MappingUtil.mapQueue(queueDao.getAllSimple());
        }

        public List<Song> getSongs() {
            return songs;
        }
    }

    private static class InsertAllThreadSafe implements Runnable {
        private final QueueDao queueDao;
        private final List<Song> songs;

        public InsertAllThreadSafe(QueueDao queueDao, List<Song> songs) {
            this.queueDao = queueDao;
            this.songs = songs;
        }

        @Override
        public void run() {
            queueDao.insertAll(MappingUtil.mapSongsToQueue(songs));
        }
    }

    private static class DeleteThreadSafe implements Runnable {
        private final QueueDao queueDao;
        private final int position;

        public DeleteThreadSafe(QueueDao queueDao, int position) {
            this.queueDao = queueDao;
            this.position = position;
        }

        @Override
        public void run() {
            queueDao.delete(position);
        }
    }

    private static class DeleteAllThreadSafe implements Runnable {
        private final QueueDao queueDao;

        public DeleteAllThreadSafe(QueueDao queueDao) {
            this.queueDao = queueDao;
        }

        @Override
        public void run() {
            queueDao.deleteAll();
        }
    }

    private static class CountThreadSafe implements Runnable {
        private final QueueDao queueDao;
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

    private static class SetLastPlayedTimestampThreadSafe implements Runnable {
        private final QueueDao queueDao;
        private final String songId;

        public SetLastPlayedTimestampThreadSafe(QueueDao queueDao, String songId) {
            this.queueDao = queueDao;
            this.songId = songId;
        }

        @Override
        public void run() {
            queueDao.setLastPlay(songId, Instant.now().toEpochMilli());
        }
    }

    private static class SetPlayingChangedTimestampThreadSafe implements Runnable {
        private final QueueDao queueDao;
        private final String songId;
        private final long ms;

        public SetPlayingChangedTimestampThreadSafe(QueueDao queueDao, String songId, long ms) {
            this.queueDao = queueDao;
            this.songId = songId;
            this.ms = ms;
        }

        @Override
        public void run() {
            queueDao.setPlayingChanged(songId, ms);
        }
    }

    private static class GetLastPlayedSongThreadSafe implements Runnable {
        private final QueueDao queueDao;
        private int index;

        public GetLastPlayedSongThreadSafe(QueueDao queueDao) {
            this.queueDao = queueDao;
        }

        @Override
        public void run() {
            index = queueDao.getLastPlay();
        }

        public int getIndex() {
            return index;
        }
    }

    private static class GetLastPlayedSongTimestampThreadSafe implements Runnable {
        private final QueueDao queueDao;
        private long timestamp;

        public GetLastPlayedSongTimestampThreadSafe(QueueDao queueDao) {
            this.queueDao = queueDao;
        }

        @Override
        public void run() {
            timestamp = queueDao.getLastPlayedTimestamp();
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
