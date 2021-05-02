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
    private LiveData<List<Artist>> listLiveSampleArtist;
    private LiveData<List<Artist>> searchListLiveArtist;


    public ArtistRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        artistDao = database.artistDao();
    }

    public LiveData<List<Artist>> getListLiveArtists() {
        listLiveArtists = artistDao.getAll();
        return listLiveArtists;
    }

    public LiveData<List<Artist>> getListLiveSampleArtist() {
        listLiveSampleArtist = artistDao.getSample(10);
        return listLiveSampleArtist;
    }

    public LiveData<List<Artist>> searchListLiveArtist(String name, int limit) {
        searchListLiveArtist = artistDao.searchArtist(name, limit);
        return searchListLiveArtist;
    }

    public List<String> getSearchSuggestion(String query) {
        List<String> suggestions = new ArrayList<>();

        SearchSuggestionsThreadSafe suggestionsThread = new SearchSuggestionsThreadSafe(artistDao, query, 5);
        Thread thread = new Thread(suggestionsThread);
        thread.start();

        try {
            thread.join();
            suggestions = suggestionsThread.getSuggestions();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return suggestions;
    }

    public void insertAll(ArrayList<Artist> artists) {
        try {
            final Thread deleteAll = new Thread(new DeleteAllThreadSafe(artistDao));
            final Thread insertAll = new Thread(new InsertAllThreadSafe(artistDao, artists));

            deleteAll.start();
            deleteAll.join();
            insertAll.start();
            insertAll.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deleteAll() {
        DeleteAllThreadSafe delete = new DeleteAllThreadSafe(artistDao);
        Thread thread = new Thread(delete);
        thread.start();
    }

    public Artist getArtistByID(String id) {
        Artist artist = null;

        GetArtistByIDThreadSafe getArtist = new GetArtistByIDThreadSafe(artistDao, id);
        Thread thread = new Thread(getArtist);
        thread.start();

        try {
            thread.join();
            artist = getArtist.getArtist();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return artist;
    }

    private static class SearchSuggestionsThreadSafe implements Runnable {
        private ArtistDao artistDao;
        private String query;
        private int number;
        private List<String> suggestions = new ArrayList<>();

        public SearchSuggestionsThreadSafe(ArtistDao artistDao, String query, int number) {
            this.artistDao = artistDao;
            this.query = query;
            this.number = number;
        }

        @Override
        public void run() {
            suggestions = artistDao.searchSuggestions(query, number);
        }

        public List<String> getSuggestions() {
            return suggestions;
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

    private static class DeleteAllThreadSafe implements Runnable {
        private ArtistDao artistDao;

        public DeleteAllThreadSafe(ArtistDao artistDao) {
            this.artistDao = artistDao;
        }

        @Override
        public void run() {
            artistDao.deleteAll();
        }
    }

    private static class GetArtistByIDThreadSafe implements Runnable {
        private Artist artist;
        private ArtistDao artistDao;
        private String id;

        public GetArtistByIDThreadSafe(ArtistDao artistDao, String id) {
            this.artistDao = artistDao;
            this.id = id;
        }

        @Override
        public void run() {
            artist = artistDao.getArtistByID(id);
        }

        public Artist getArtist() {
            return artist;
        }
    }
}
