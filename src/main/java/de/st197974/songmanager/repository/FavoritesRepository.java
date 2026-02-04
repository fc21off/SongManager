package de.st197974.songmanager.repository;

import java.util.List;

public interface FavoritesRepository {

    void addFavorite(String songId);

    void removeFavorite(String songId);

    boolean isFavorite(String songId);

    List<String> getAllFavoriteIds();

}
