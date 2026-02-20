package de.st197974.songmanager.model;

import java.util.UUID;

/**
 * Represents a song with details such as title, album, artist, and duration.
 * Provides methods to format and display the song's information.
 */
public record Song(String id, String title, String album, String artist, int durationInSeconds) {

    public Song(String title, String album, String artist, int durationInSeconds) {
        this(UUID.randomUUID().toString(), title, album, artist, durationInSeconds);
    }

    @Override
    public String toString() {
        return toDisplayString();
    }

    public String toDisplayString() {

        if ("null".equals(this.id)) {
            return "<html><div style='text-align: center; width: 380px; padding: 5px; color: #888888;'>" + "<i>" + title + "</i>" + "</div></html>";
        }

        String albumInfo = (album == null || album.trim().isEmpty()) ? "" : " <font color='gray'> (" + album + ")</font>";

        return "<html><table width='480'>" + "<tr>" + "<td align='left'>" + title + " <b>– " + artist + "</b>" + albumInfo + "</td>" + "<td align='right' width='60'><b>" + formatTime(durationInSeconds) + "</b></td>" + "</tr>" + "</table></html>";
    }

    public String formatTime(int duration) {
        int minutes = duration / 60;
        int seconds = duration % 60;

        return String.format("%d:%02d", minutes, seconds);
    }
}
