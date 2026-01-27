package de.st197974.swiftmanager.model;

import java.util.UUID;

public class Song {

    private String id;
    private String title;
    private String album;
    private String artist;
    private int durationInSeconds;

    public Song(String title, String album, String artist, int durationInSeconds) {
        this(UUID.randomUUID().toString(), title, album, artist, durationInSeconds);
    }

    public Song(String id, String title, String album, String artist, int durationInSeconds) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.durationInSeconds = durationInSeconds;
        this.artist = artist;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAlbum() { return album; }
    public String getArtist() { return artist; }
    public int getDurationInSeconds() { return durationInSeconds; }

    @Override
    public String toString() {
        return toDisplayString();
    }

    public String toDisplayString() {
        return "<html><table width='460'>" +
                "<tr>" +
                "<td align='left'>" +
                title +
                " <b>– " + artist + "</b>" +
                " <font color='gray'> (" + album + ")</font>" +
                "</td>" +
                "<td align='right'><b>" + formatTime(durationInSeconds) + "</b></td>" +
                "</tr>" +
                "</table></html>";
    }

    public String formatTime(int duration){
        int minutes = duration / 60;
        int seconds = duration % 60;

        return String.format("%d:%02d", minutes, seconds);
    }
}
