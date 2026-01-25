package de.firecreeper21.swiftmanager.model;

import java.util.UUID;

public class Song {

    private String id;
    private String title;
    private String album;
    private int durationInSeconds;

    // Konstruktor 1: Für neue Songs (ID wird generiert)
    public Song(String title, String album, int durationInSeconds) {
        this(UUID.randomUUID().toString(), title, album, durationInSeconds);
    }

    // Konstruktor 2: Falls man die ID schon hat (z.B. aus DB)
    public Song(String id, String title, String album, int durationInSeconds) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.durationInSeconds = durationInSeconds;
    }

    // Getter
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAlbum() { return album; }
    public int getDurationInSeconds() { return durationInSeconds; }

    @Override
    public String toString() {
        return "Song: " + title + " (Album: " + album + ", ID: " + id + ", Duration (in Seconds): " + durationInSeconds + ")";
    }
}
