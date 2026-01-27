package de.st197974.swiftmanager.repository;

import de.st197974.swiftmanager.model.Song;
import java.util.List;

public interface SongRepository {

    void save(Song song);

    List<Song> findAll();

    Song findByID(String id);

    void deleteByID(String id);

}
