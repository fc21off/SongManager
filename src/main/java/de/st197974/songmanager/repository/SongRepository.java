package de.st197974.songmanager.repository;

import de.st197974.songmanager.model.Song;

import java.util.List;

public interface SongRepository {

    void save(Song song);

    List<Song> findAll();

    Song findByID(String id);

    List<Song> findByArtist(String artist);

    void deleteByID(String id);

}
