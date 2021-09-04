package com.cappielloantonio.play.subsonic.models;

import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.Xml;

import java.util.ArrayList;
import java.util.List;

@Xml
public class ArtistInfo2 extends ArtistInfoBase {
    @Element(name = "similarArtist")
    protected List<SimilarArtistID3> similarArtists;

    public List<SimilarArtistID3> getSimilarArtists() {
        if (similarArtists == null) {
            similarArtists = new ArrayList<>();
        }
        return this.similarArtists;
    }

    public void setSimilarArtists(List<SimilarArtistID3> similarArtists) {
        this.similarArtists = similarArtists;
    }
}
