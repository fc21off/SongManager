package de.st197974.songmanager.service;

import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.repository.SongRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;

public record DiscographyService(SongRepository repository) {

    private static final Logger logger = LogManager.getLogger(DiscographyService.class);

    public List<Song> getSongsByAlbum(String albumName) {
        return repository.findAll().stream()
                .filter(song -> song.album().equalsIgnoreCase(albumName))
                .toList();
    }

    public List<Song> getAll() {
        return repository.findAll();
    }

    public int getTotalDurationOfAlbum(String albumName) {
        return repository.findAll().stream()
                .filter(song -> song.album().equalsIgnoreCase(albumName))
                .mapToInt(Song::durationInSeconds)
                .sum();
    }

    public void addSongSafely(Song song) {
        if (song.title() == null || song.title().isEmpty()) {
            logger.warn("Attempted to add song without title!");
            return;
        }

        repository.save(song);

        logger.info("Song {} added successfully!", song.title());
    }

    public String getSongTitleById(String id) {
        return repository.findAll().stream()
                .filter(s -> s.id().equals(id))
                .map(Song::title)
                .findFirst()
                .orElse("Unknown Song");
    }

    public void deleteSong(String id) {

        boolean exists = repository.findAll().stream()
                .anyMatch(song -> song.id().equals(id));

        if (!exists) return;
        logger.info("Song {} was deleted!", getSongTitleById(id));
        repository.deleteByID(id);

    }

    public List<Song> getAllSortedByAlbum() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Song::album, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Song::title, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Song> getAllSortedByDuration() {

        return repository.findAll().stream()
                .sorted(Comparator.comparing(Song::durationInSeconds))
                .toList();

    }

    public List<String> getAllArtists() {
        return repository.findAll().stream()
                .map(Song::artist)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<Song> getSongsByArtist(String artist) {
        return repository.findByArtist(artist);
    }

    public List<Song> getSongsByArtistSortedByAlbum(String artist) {
        return repository.findByArtist(artist).stream()
                .sorted(Comparator.comparing(Song::album, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Song::title, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Song> getSongsByArtistSortedByDuration(String artist) {
        return repository.findByArtist(artist).stream()
                .sorted(Comparator.comparingInt(Song::durationInSeconds))
                .toList();
    }

    public List<Song> getSongsAlphabetically(String artist) {

        return repository.findByArtist(artist).stream()
                .sorted(Comparator.comparing(Song::title, String.CASE_INSENSITIVE_ORDER))
                .toList();

    }

    public void updateSongSafely(Song updatedSong) {

        if (updatedSong.id() == null || updatedSong.title().isEmpty()) {
            logger.warn("Update Failed: Song ID or Title Missing!");
            return;
        }

        repository.save(updatedSong);
        logger.info("Song Updated: {} (ID: {}", updatedSong.title(), updatedSong.id());

    }

}
