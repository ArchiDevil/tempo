package com.cappielloantonio.play.interfaces;


import android.os.Bundle;

public interface ClickCallback {
    default void onMediaClick(Bundle bundle) {}

    default void onMediaLongClick(Bundle bundle) {}

    default void onAlbumClick(Bundle bundle) {}

    default void onAlbumLongClick(Bundle bundle) {}

    default void onArtistClick(Bundle bundle) {}

    default void onArtistLongClick(Bundle bundle) {}

    default void onGenreClick(Bundle bundle) {}

    default void onPlaylistClick(Bundle bundle) {}

    default void onPlaylistLongClick(Bundle bundle) {}

    default void onYearClick(Bundle bundle) {}

    default void onServerClick(Bundle bundle) {}

    default void onServerLongClick(Bundle bundle) {}

    default void onPodcastEpisodeClick(Bundle bundle) {}

    default void onPodcastEpisodeLongClick(Bundle bundle) {}

    default void onPodcastChannelClick(Bundle bundle) {}

    default void onPodcastChannelLongClick(Bundle bundle) {}

    default void onInternetRadioStationClick(Bundle bundle) {}

    default void onInternetRadioStationLongClick(Bundle bundle) {}
}
