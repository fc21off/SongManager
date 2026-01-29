package de.st197974.songmanager.repository;

import de.st197974.songmanager.model.Song;

import java.util.ArrayList;
import java.util.List;

public class InMemorySongRepository implements SongRepository {

    private final List<Song> songList = new ArrayList<>();

    @Override
    public void save(Song song) {
        songList.add(song);
    }

    @Override
    public List<Song> findAll() {
        return new ArrayList<>(songList);
    }

    @Override
    public Song findByID(String id) {
        return songList.stream()
                .filter(s -> s.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Song> findByArtist(String artist) {

        return songList.stream()
                .filter(s -> s.artist().equalsIgnoreCase(artist))
                .toList();

    }

    @Override
    public void deleteByID(String id) {
        songList.removeIf(s -> s.id().equals(id));
    }
}
