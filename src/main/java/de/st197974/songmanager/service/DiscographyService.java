package de.st197974.songmanager.service;

import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.repository.SongRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;

public class DiscographyService {

    private static final Logger logger = LogManager.getLogger(DiscographyService.class);

    private final SongRepository repository;

    public DiscographyService(SongRepository repository) {
        this.repository = repository;
    }

    public List<Song> getSongsByAlbum(String albumName) {
        return repository.findAll().stream()
                .filter(song -> song.getAlbum().equalsIgnoreCase(albumName))
                .toList();
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
                .toList();
    }

    public List<Song> getAllSortedByDuration() {

        return repository.findAll().stream()
                .sorted(Comparator.comparing(Song::getDurationInSeconds))
                .toList();

    }

    public List<String> getAllArtists() {
        return repository.findAll().stream()
                .map(Song::getArtist)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<Song> getSongsByArtist(String artist) {
        return repository.findByArtist(artist);
    }

    public List<Song> getSongsByArtistSortedByAlbum(String artist) {
        return repository.findByArtist(artist).stream()
                .sorted(Comparator.comparing(Song::getAlbum, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Song> getSongsByArtistSortedByDuration(String artist) {
        return repository.findByArtist(artist).stream()
                .sorted(Comparator.comparingInt(Song::getDurationInSeconds))
                .toList();
    }

    public List<Song> getSongsAlphabetically(String artist) {

        return repository.findByArtist(artist).stream()
                .sorted(Comparator.comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();

    }

}
