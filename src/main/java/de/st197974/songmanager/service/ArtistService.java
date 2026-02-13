package de.st197974.songmanager.service;

import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.repository.SongRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public record ArtistService(SongRepository songRepository) {

    private static final Logger logger = LogManager.getLogger(ArtistService.class);

    public void deleteArtist(String artist) {
        List<Song> songs = songRepository.findByArtist(artist);

        for (Song s : songs) {
            songRepository.deleteByID(s.id());
        }

        logger.info("Deleted artist {} with {} songs",
                artist, songs.size());
    }

    public void renameArtist(String oldName, String newName) {
        if (newName == null || newName.isBlank()) return;

        List<Song> songs = songRepository.findByArtist(oldName);

        for (Song s : songs) {
            Song updated = new Song(
                    s.id(),
                    s.title(),
                    s.album(),
                    newName,
                    s.durationInSeconds()
            );

            songRepository.save(updated);
        }

        logger.info("Renamed artist {} to {} ({} songs)",
                oldName, newName, songs.size());
    }

}
