package com.cappielloantonio.play.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.dto.GenreDto;
import org.jellyfin.apiclient.model.entities.ImageType;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "artist")
public class Artist implements Parcelable {
    @Ignore
    public List<Genre> genres;
    @Ignore
    public List<Album> albums;
    @Ignore
    public List<Song> songs;

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "primary")
    public String primary;

    @ColumnInfo(name = "blurHash")
    public String blurHash;

    public Artist(@NonNull String id, String name, String primary, String blurHash) {
        this.id = id;
        this.name = name;
        this.primary = primary;
        this.blurHash = blurHash;
    }

    @Ignore
    public Artist(BaseItemDto itemDto) {
        this.id = itemDto.getId();
        this.name = itemDto.getName();

        this.primary = itemDto.getImageTags().containsKey(ImageType.Primary) ? id : null;
        if (itemDto.getImageBlurHashes() != null && itemDto.getImageBlurHashes().get(ImageType.Primary) != null) {
            this.blurHash = (String) itemDto.getImageBlurHashes().get(ImageType.Primary).values().toArray()[0];
        }

        this.genres = new ArrayList<>();
        this.albums = new ArrayList<>();
        this.songs = new ArrayList<>();

        if (itemDto.getGenreItems() != null) {
            for (GenreDto genre : itemDto.getGenreItems()) {
                genres.add(new Genre(genre));
            }
        }
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public String getBlurHash() {
        return blurHash;
    }

    public void setBlurHash(String blurHash) {
        this.blurHash = blurHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artist artist = (Artist) o;
        return id.equals(artist.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(primary);
        dest.writeString(blurHash);
    }

    protected Artist(Parcel in) {
        this.genres = new ArrayList<>();
        this.albums = new ArrayList<>();
        this.songs = new ArrayList<>();
        this.id = in.readString();
        this.name = in.readString();
        this.primary = in.readString();
        this.blurHash = in.readString();
    }

    public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel source) {
            return new Artist(source);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
}