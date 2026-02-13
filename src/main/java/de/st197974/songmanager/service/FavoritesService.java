package de.st197974.songmanager.service;

import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.repository.FavoritesRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for managing users' favorite songs. This includes adding, removing,
 * toggling, and retrieving favorites, as well as sorting the favorite songs
 * by various attributes.
 * <p>
 * Responsibilities:
 * - Add a song to the list of favorites.
 * - Remove a song from the list of favorites.
 * - Check if a song is marked as a favorite.
 * - Retrieve all favorite song IDs.
 * - Retrieve all favorite songs with detailed information.
 * - Toggle the favorite status of a song.
 * - Sort favorite songs alphabetically, by artist, or by duration.
 * <p>
 * This service uses a FavoritesRepository to store favorite song IDs
 * and a DiscographyService to retrieve song details.
 */
public record FavoritesService(FavoritesRepository repository, DiscographyService service) {

    private static final Logger logger = LogManager.getLogger(FavoritesService.class);

    public boolean addFavorite(String songId) {

        if (songId == null || songId.isEmpty()) {
            logger.warn("Error while adding Song, ID was empty!");
            return false;
        }

        String title = service.getSongTitleById(songId);

        if (repository.isFavorite(songId)) {
            logger.info("Song {} is already marked as favorite!", title);
            return false;
        }

        repository.addFavorite(songId);
        logger.info("Song {} was added to favorites!", title);
        return true;
    }

    public void removeFavorite(String songId) {

        if (songId == null || songId.isEmpty()) {
            logger.warn("Error while removing Song, ID was empty!");
            return;
        }

        String title = service.getSongTitleById(songId);

        if (!repository.isFavorite(songId)) {
            logger.info("Song {} is not marked as favorite!", title);
            return;
        }

        repository.removeFavorite(songId);
        logger.info("Song {} was removed from Favorites!", title);

    }

    public boolean isFavorite(String songId) {
        return songId != null && repository.isFavorite(songId);
    }

    public List<String> getAllFavoriteIds() {
        return repository.getAllFavoriteIds();
    }

    public List<Song> getAllFavorites() {
        return repository.getAllFavoriteIds().stream()
                .map(service::getSongById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void toggleFavorite(String songId) {
        if (isFavorite(songId)) {
            removeFavorite(songId);
        } else {
            addFavorite(songId);
        }
    }

    public List<Song> getFavoritesSortedAlphabetically() {
        return getAllFavorites().stream()
                .sorted(Comparator.comparing(Song::title, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<Song> getFavoritesSortedByArtist() {
        return getAllFavorites().stream()
                .sorted(Comparator.comparing(Song::artist, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<Song> getFavoritesSortedByDuration() {
        return getAllFavorites().stream()
                .sorted(Comparator.comparingInt(Song::durationInSeconds))
                .collect(Collectors.toList());
    }

}
