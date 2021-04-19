package com.cappielloantonio.play.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.cappielloantonio.play.database.AppDatabase;
import com.cappielloantonio.play.database.dao.SongDao;
import com.cappielloantonio.play.database.dao.SongGenreCrossDao;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.model.SongGenreCross;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongRepository {
    private static final String TAG = "SongRepository";

    private SongDao songDao;
    private SongGenreCrossDao songGenreCrossDao;
    private LiveData<List<Song>> searchListLiveSongs;
    private LiveData<Song> liveDataSong;
    private LiveData<List<Song>> listLiveSampleRecentlyAddedSongs;
    private LiveData<List<Song>> listLiveSampleRecentlyPlayedSongs;
    private LiveData<List<Song>> listLiveSampleMostPlayedSongs;
    private LiveData<List<Song>> listLiveSampleArtistTopSongs;
    private LiveData<List<Song>> listLiveAlbumSongs;
    private LiveData<List<Song>> listLivePlaylistSongs;
    private LiveData<List<Song>> listLiveSongByGenre;
    private LiveData<List<Song>> listLiveFilteredSongs;
    private LiveData<List<Song>> listLiveSongByYear;
    private LiveData<List<Song>> listLiveSampleFavoritesSong;
    private LiveData<List<Song>> listLiveFavoritesSong;


    public SongRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        songDao = database.songDao();
        songGenreCrossDao = database.songGenreCrossDao();
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

    public List<Song> getArtistListLiveRandomSong(String artistID) {
        List<Song> songs = new ArrayList<>();

        GetRandomSongsByArtistIDThreadSafe randomArtistSongThread = new GetRandomSongsByArtistIDThreadSafe(songDao, artistID, 100);
        Thread thread = new Thread(randomArtistSongThread);
        thread.start();

        try {
            thread.join();
            songs = randomArtistSongThread.getSongs();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return songs;
    }

    public LiveData<List<Song>> getAlbumListLiveSong(String albumID) {
        listLiveAlbumSongs = songDao.getLiveAlbumSong(albumID);
        return listLiveAlbumSongs;
    }

    public LiveData<List<Song>> getPlaylistLiveSong(String playlistID) {
        listLivePlaylistSongs = songDao.getLivePlaylistSong(playlistID);
        return listLivePlaylistSongs;
    }

    public List<Song> getAlbumListSong(String albumID, boolean randomOrder) {
        List<Song> songs = new ArrayList<>();

        GetSongsByAlbumIDThreadSafe suggestionsThread = new GetSongsByAlbumIDThreadSafe(songDao, albumID);
        Thread thread = new Thread(suggestionsThread);
        thread.start();

        try {
            thread.join();
            songs = suggestionsThread.getSongs();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(randomOrder) {
            Collections.shuffle(songs);
        }

        return songs;
    }

    public LiveData<List<Song>> getFilteredListLiveSong(ArrayList<String> filters) {
        listLiveFilteredSongs = songDao.getFilteredSong(filters);
        return listLiveFilteredSongs;
    }

    public List<String> getSearchSuggestion(String query) {
        List<String> suggestions = new ArrayList<>();

        SearchSuggestionsThreadSafe suggestionsThread = new SearchSuggestionsThreadSafe(songDao, query, 5);
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

    /*
     * Funzione che ritorna l'intero set di canzoni.
     * Utilizzato per l'aggiornamento del catalogo.
     */
    public List<Song> getCatalogue() {
        List<Song> catalogue = new ArrayList<>();

        GetCatalogueThreadSafe getCatalogueThread = new GetCatalogueThreadSafe(songDao);
        Thread thread = new Thread(getCatalogueThread);
        thread.start();

        try {
            thread.join();
            catalogue = getCatalogueThread.getCatalogue();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return catalogue;
    }

    public List<Integer> getYearList() {
        List<Integer> years = new ArrayList<>();

        GetYearListThreadSafe getYearListThreadSafe = new GetYearListThreadSafe(songDao);
        Thread thread = new Thread(getYearListThreadSafe);
        thread.start();

        try {
            thread.join();
            years = getYearListThreadSafe.getYearList();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return years;
    }

    public LiveData<List<Song>> getSongByYearListLive(int year) {
        listLiveSongByYear = songDao.getSongsByYear(year);
        return listLiveSongByYear;
    }

    public LiveData<List<Song>> getListLiveFavoritesSampleSong(int number) {
        listLiveSampleFavoritesSong = songDao.getFavoriteSongSample(number);
        return listLiveSampleFavoritesSong;
    }

    public LiveData<List<Song>> getListLiveFavoritesSong() {
        listLiveFavoritesSong = songDao.getFavoriteSong();
        return listLiveFavoritesSong;
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
        InsertAllThreadSafe insertAll = new InsertAllThreadSafe(songDao, songGenreCrossDao, songs);
        Thread thread = new Thread(insertAll);
        thread.start();
    }

    public void delete(Song song) {
        DeleteThreadSafe delete = new DeleteThreadSafe(songDao, song);
        Thread thread = new Thread(delete);
        thread.start();
    }

    public void increasePlayCount(Song song) {
        boolean isIncreased = song.nowPlaying();

        if(isIncreased) {
            // UpdatePlayCountThreadSafe update = new UpdatePlayCountThreadSafe(songDao, song);
            UpdateThreadSafe update = new UpdateThreadSafe(songDao, song);
            Thread thread = new Thread(update);
            thread.start();
        }
    }

    public void setFavoriteStatus(Song song) {
        // UpdateFavoriteThreadSafe update = new UpdateFavoriteThreadSafe(songDao, song);
        UpdateThreadSafe update = new UpdateThreadSafe(songDao, song);
        Thread thread = new Thread(update);
        thread.start();
    }

    public void getAll() {
        GetCatalogueThreadSafe catalogue = new GetCatalogueThreadSafe(songDao);
        Thread thread = new Thread(catalogue);
        thread.start();
    }

    public void insertSongPerGenre(ArrayList<SongGenreCross> songGenreCrosses) {
        InsertPerGenreThreadSafe insertPerGenre = new InsertPerGenreThreadSafe(songGenreCrossDao, songGenreCrosses);
        Thread thread = new Thread(insertPerGenre);
        thread.start();
    }

    public void deleteAllSong() {
        DeleteAllSongThreadSafe delete = new DeleteAllSongThreadSafe(songDao);
        Thread thread = new Thread(delete);
        thread.start();
    }

    public void deleteAllSongGenreCross() {
        DeleteAllSongGenreCrossThreadSafe delete = new DeleteAllSongGenreCrossThreadSafe(songGenreCrossDao);
        Thread thread = new Thread(delete);
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

    private static class GetSongsByAlbumIDThreadSafe implements Runnable {
        private SongDao songDao;
        private String albumID;
        private List<Song> songs = new ArrayList<>();

        public GetSongsByAlbumIDThreadSafe(SongDao songDao, String albumID) {
            this.songDao = songDao;
            this.albumID = albumID;
        }

        @Override
        public void run() {
            songs = songDao.getAlbumSong(albumID);
        }

        public List<Song> getSongs() {
            return songs;
        }
    }

    private static class GetRandomSongsByArtistIDThreadSafe implements Runnable {
        private SongDao songDao;
        private String artistID;
        private int limit;
        private List<Song> songs = new ArrayList<>();

        public GetRandomSongsByArtistIDThreadSafe(SongDao songDao, String artistID, int limit) {
            this.songDao = songDao;
            this.artistID = artistID;
            this.limit = limit;
        }

        @Override
        public void run() {
            songs = songDao.getArtistRandomSongs(artistID, limit);
        }

        public List<Song> getSongs() {
            return songs;
        }
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
        private SongGenreCrossDao songGenreCrossDao;
        private ArrayList<Song> songs;

        public InsertAllThreadSafe(SongDao songDao, SongGenreCrossDao songGenreCrossDao, ArrayList<Song> songs) {
            this.songDao = songDao;
            this.songGenreCrossDao = songGenreCrossDao;
            this.songs = songs;
        }

        @Override
        public void run() {
            songDao.deleteAll();
            songGenreCrossDao.deleteAll();
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

    private static class UpdatePlayCountThreadSafe implements Runnable {
        private SongDao songDao;
        private Song song;

        public UpdatePlayCountThreadSafe(SongDao songDao, Song song) {
            this.songDao = songDao;
            this.song = song;
        }

        @Override
        public void run() {
            songDao.updatePlayCount(song.getId(), song.getPlayCount(), song.getLastPlay());
        }
    }

    private static class UpdateFavoriteThreadSafe implements Runnable {
        private SongDao songDao;
        private Song song;

        public UpdateFavoriteThreadSafe(SongDao songDao, Song song) {
            this.songDao = songDao;
            this.song = song;
        }

        @Override
        public void run() {
            songDao.updateFavorite(song.getId(), song.isFavorite());
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

    private static class SearchSuggestionsThreadSafe implements Runnable {
        private SongDao songDao;
        private String query;
        private int number;
        private List<String> suggestions = new ArrayList<>();

        public SearchSuggestionsThreadSafe(SongDao songDao, String query, int number) {
            this.songDao = songDao;
            this.query = query;
            this.number = number;
        }

        @Override
        public void run() {
            suggestions = songDao.searchSuggestions(query, number);
        }

        public List<String> getSuggestions() {
            return suggestions;
        }
    }

    private static class GetCatalogueThreadSafe implements Runnable {
        private SongDao songDao;
        private List<Song> catalogue = new ArrayList<>();

        public GetCatalogueThreadSafe(SongDao songDao) {
            this.songDao = songDao;
        }

        @Override
        public void run() {
            catalogue = songDao.getAllList();
        }

        public List<Song> getCatalogue() {
            return catalogue;
        }
    }

    private static class InsertPerGenreThreadSafe implements Runnable {
        private SongGenreCrossDao songGenreCrossDao;
        private ArrayList<SongGenreCross> cross;

        public InsertPerGenreThreadSafe(SongGenreCrossDao songGenreCrossDao, ArrayList<SongGenreCross> cross) {
            this.songGenreCrossDao = songGenreCrossDao;
            this.cross = cross;
        }

        @Override
        public void run() {
            songGenreCrossDao.insertAll(cross);
        }
    }

    private static class DeleteAllSongThreadSafe implements Runnable {
        private SongDao songDao;

        public DeleteAllSongThreadSafe(SongDao songDao) {
            this.songDao = songDao;
        }

        @Override
        public void run() {
            songDao.deleteAll();
        }
    }

    private static class DeleteAllSongGenreCrossThreadSafe implements Runnable {
        private SongGenreCrossDao songGenreCrossDao;

        public DeleteAllSongGenreCrossThreadSafe(SongGenreCrossDao songGenreCrossDao) {
            this.songGenreCrossDao = songGenreCrossDao;
        }

        @Override
        public void run() {
            songGenreCrossDao.deleteAll();
        }
    }

    private static class GetYearListThreadSafe implements Runnable {
        private SongDao songDao;
        private List<Integer> years = new ArrayList<>();
        private List<Integer> decades = new ArrayList<>();

        public GetYearListThreadSafe(SongDao songDao) {
            this.songDao = songDao;
        }

        @Override
        public void run() {
            years = songDao.getYearList();

            for(int year : years) {
                if(!decades.contains(year - year % 10)) {
                    decades.add(year);
                }
            }
        }

        public List<Integer> getYearList() {
            return years;
        }

        public List<Integer> getDecadeList() {
            return decades;
        }
    }
}
