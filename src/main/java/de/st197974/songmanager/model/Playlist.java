package de.st197974.songmanager.model;

import java.util.UUID;

/**
 * Represents a playlist with a unique identifier and a name.
 */
public record Playlist(String id, String name) {

    public Playlist(String name) {
        this(UUID.randomUUID().toString(), name);
    }

    @Override
    public String toString() {
        return name;
    }

}
