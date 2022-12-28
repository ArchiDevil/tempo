package com.cappielloantonio.play.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chronology")
public class Chronology {
    @PrimaryKey(autoGenerate = true)
    private int uuid;

    @ColumnInfo(name = "id")
    private String trackId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "albumId")
    private String albumId;

    @ColumnInfo(name = "albumName")
    private String albumName;

    @ColumnInfo(name = "artistId")
    private String artistId;

    @ColumnInfo(name = "artistName")
    private String artistName;

    @ColumnInfo(name = "cover_art_id")
    private String coverArtId;

    @ColumnInfo(name = "duration")
    private long duration;

    @ColumnInfo(name = "container")
    private String container;

    @ColumnInfo(name = "bitrate")
    private int bitrate;

    @ColumnInfo(name = "extension")
    private String extension;

    @ColumnInfo(name = "timestamp")
    private Long timestamp;

    public Chronology(String trackId, String title, String albumId, String albumName, String artistId, String artistName, String coverArtId, long duration, String container, int bitrate, String extension) {
        this.trackId = trackId;
        this.title = title;
        this.albumId = albumId;
        this.albumName = albumName;
        this.artistId = artistId;
        this.artistName = artistName;
        this.coverArtId = coverArtId;
        this.duration = duration;
        this.container = container;
        this.bitrate = bitrate;
        this.extension = extension;
        this.timestamp = System.currentTimeMillis();
    }

    public int getUuid() {
        return uuid;
    }

    public void setUuid(int uuid) {
        this.uuid = uuid;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getCoverArtId() {
        return coverArtId;
    }

    public void setCoverArtId(String coverArtId) {
        this.coverArtId = coverArtId;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
