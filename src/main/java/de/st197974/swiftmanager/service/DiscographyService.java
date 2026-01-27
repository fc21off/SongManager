package de.st197974.swiftmanager.service;

import de.st197974.swiftmanager.model.Song;
import de.st197974.swiftmanager.repository.SongRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DiscographyService {

    private static final Logger logger = LogManager.getLogger(DiscographyService.class);

    private final SongRepository repository;

    public DiscographyService(SongRepository repository) {
        this.repository = repository;
    }

    public List<Song> getSongsByAlbum(String albumName) {
        return repository.findAll().stream()
                .filter(song -> song.getAlbum().equalsIgnoreCase(albumName))
                .collect(Collectors.toList());
    }

    public List<Song> getAll() {
        return repository.findAll();
    }

    public int getTotalDurationOfAlbum(String albumName){
        return repository.findAll().stream()
                .filter(song -> song.getAlbum().equalsIgnoreCase(albumName))
                .mapToInt(Song::getDurationInSeconds)
                .sum();
    }

    public void addSongSafely(Song song){
        if(song.getTitle() == null || song.getTitle().isEmpty()) {
            logger.warn("Attempted to add song without title!");
            return;
        }

        repository.save(song);

        logger.info("Song {} added successfully!", song.getTitle());
    }

    public String getSongTitleById(String id) {
        return repository.findAll().stream()
                .filter(s -> s.getId().equals(id))
                .map(Song::getTitle)
                .findFirst()
                .orElse("Unknown Song");
    }

    public void deleteSong(String id) {

        boolean exists = repository.findAll().stream()
                        .anyMatch(song -> song.getId().equals(id));

        if(!exists) return;
        logger.info("Song {} was deleted!", getSongTitleById(id));
        repository.deleteByID(id);

    }

    public List<Song> getAllSortedByAlbum() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Song::getAlbum, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<Song> getAllSortedByDuration() {

        return repository.findAll().stream()
                .sorted(Comparator.comparing(Song::getDurationInSeconds))
                .collect(Collectors.toList());

    }

}
