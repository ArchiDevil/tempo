package com.cappielloantonio.play.util;

import android.text.Html;

import com.cappielloantonio.play.App;
import com.cappielloantonio.play.R;
import com.cappielloantonio.play.glide.CustomGlideRequest;
import com.cappielloantonio.play.model.Song;
import com.google.android.exoplayer2.MediaItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicUtil {
    private static final String TAG = "MusicUtil";

    public static String getSongFileUri(Song song) {
        String url = App.getSubsonicClientInstance(App.getInstance(), false).getUrl();

        Map<String, String> params = App.getSubsonicClientInstance(App.getInstance(), false).getParams();

        return url + "stream" +
                "?u=" + params.get("u") +
                "&s=" + params.get("s") +
                "&t=" + params.get("t") +
                "&v=" + params.get("v") +
                "&c=" + params.get("c") +
                "&id=" + song.getId();
    }

    public static String getReadableDurationString(long duration, boolean millis) {
        long minutes = 0;
        long seconds = 0;

        if (millis) {
            minutes = (duration / 1000) / 60;
            seconds = (duration / 1000) % 60;
        } else {
            minutes = duration / 60;
            seconds = duration % 60;
        }

        if (minutes < 60) {
            return String.format(Locale.getDefault(), "%01d:%02d", minutes, seconds);
        } else {
            long hours = minutes / 60;
            minutes = minutes % 60;
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        }
    }

    public static String getReadableString(String string) {
        if (string != null) {
            return Html.fromHtml(string, Html.FROM_HTML_MODE_COMPACT).toString();
        }

        return "";
    }

    public static String forceReadableString(String string) {
        if (string != null) {
            return getReadableString(string)
                    .replaceAll("&#34;", "\"")
                    .replaceAll("&#39;", "'")
                    .replaceAll("&amp;", "'")
                    .replaceAll("<a[\\s]+([^>]+)>((?:.(?!</a>))*.)</a>", "");
        }

        return "";
    }

    public static String normalizedArtistName(String string) {
        if (string != null) {
            if(string.toLowerCase().contains(" feat.")) return Pattern.compile(" feat.", Pattern.CASE_INSENSITIVE).split(string)[0].trim();
            else if(string.toLowerCase().contains(" featuring")) return Pattern.compile(" featuring", Pattern.CASE_INSENSITIVE).split(string)[0].trim();
            else return string;
        }

        return "";
    }

    public static List<String> getReadableStrings(List<String> strings) {
        List<String> readableStrings = new ArrayList<>();

        if (strings.size() > 0) {
            for (String string : strings) {
                if (string != null) {
                    readableStrings.add(Html.fromHtml(string, Html.FROM_HTML_MODE_COMPACT).toString());
                }
            }
        }

        return readableStrings;
    }

    public static int getDefaultPicPerCategory(String category) {
        switch (category) {
            case CustomGlideRequest.SONG_PIC:
                return R.drawable.default_album_art;
            case CustomGlideRequest.ALBUM_PIC:
                return R.drawable.default_album_art;
            case CustomGlideRequest.ARTIST_PIC:
                return R.drawable.default_album_art;
            case CustomGlideRequest.PLAYLIST_PIC:
                return R.drawable.default_album_art;
            default:
                return R.drawable.default_album_art;
        }
    }

    public static MediaItem getMediaItemFromSong(Song song) {
        String uri = MusicUtil.getSongFileUri(song);
        return MediaItem.fromUri(uri);
    }
}
