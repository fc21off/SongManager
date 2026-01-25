package de.firecreeper21.swiftmanager.repository;

import de.firecreeper21.swiftmanager.model.Song;
import java.util.List;

public interface SongRepository {

    void save(Song song);

    List<Song> findAll();

    Song findByID(String id);

    void deleteByID(String id);

}
