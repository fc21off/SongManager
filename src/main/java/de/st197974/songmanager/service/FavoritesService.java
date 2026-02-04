package de.st197974.songmanager.service;

import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.repository.FavoritesRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
                .filter(Objects::nonNull) // Falls ID im Repository existiert, aber Song gelöscht wurde
                .collect(Collectors.toList());
    }

    public void toggleFavorite(String songId) {
        if (isFavorite(songId)) {
            removeFavorite(songId);
        } else {
            addFavorite(songId);
        }
    }

}
