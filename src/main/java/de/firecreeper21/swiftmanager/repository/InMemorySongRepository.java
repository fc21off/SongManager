package de.firecreeper21.swiftmanager.repository;

import de.firecreeper21.swiftmanager.model.Song;

import java.util.ArrayList;
import java.util.List;

public class InMemorySongRepository implements SongRepository{

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
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void deleteByID(String id) {
        songList.removeIf(s -> s.getId().equals(id));
    }
}
