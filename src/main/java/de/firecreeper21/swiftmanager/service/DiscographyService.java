package de.firecreeper21.swiftmanager.service;

import de.firecreeper21.swiftmanager.model.Song;
import de.firecreeper21.swiftmanager.repository.SongRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class DiscographyService {

    private static final Logger logger = LogManager.getLogger(DiscographyService.class);

    private final SongRepository repository;

    public DiscographyService(SongRepository repository) {
        this.repository = repository;
    }

    //returns all songs from an album
    public List<Song> getSongsByAlbum(String albumName) {
        return repository.findAll().stream()
                .filter(song -> song.getAlbum().equalsIgnoreCase(albumName))
                .collect(Collectors.toList());
    }

    public List<Song> getAll() {
        return repository.findAll();
    }

    //calculates duration of all songs in an album
    public int getTotalDurationOfAlbum(String albumName){
        return repository.findAll().stream()
                .filter(song -> song.getAlbum().equalsIgnoreCase(albumName))
                .mapToInt(Song::getDurationInSeconds)
                .sum();
    }

    //adds songs with a correct title
    public void addSongSafely(Song song){
        if(song.getTitle() == null || song.getTitle().isEmpty()) {
            logger.warn("Attempted to add song without title!");
            return;
        }

        repository.save(song);

        logger.info("Song {} added successfully!", song.getTitle());
    }

}
