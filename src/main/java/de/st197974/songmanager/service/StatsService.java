package de.st197974.songmanager.service;

import de.st197974.songmanager.model.Song;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides statistical analysis and operations for songs and favorites.
 * <p>
 * The StatsService class aggregates data from two primary services:
 * DiscographyService and FavoritesService. It provides methods for
 * retrieving high-level information, such as the total number of songs,
 * average duration, and total number of favorite songs.
 * <p>
 * Responsibilities include:
 * - Aggregating and summarizing data on songs and favorites.
 * - Calculating statistics, such as the total duration and average song length.
 * - Grouping songs by artist and managing song counts.
 */
public record StatsService(DiscographyService discographyService, FavoritesService favoritesService) {

    public List<Song> getAllSongs() {
        return discographyService.getAll();
    }

    public Map<String, Long> getSongsPerArtist() {
        return discographyService.getAll().stream().collect(Collectors.groupingBy(Song::artist, Collectors.counting()));
    }

    public int getTotalSongs() {
        return discographyService.getAll().size();
    }

    public int getTotalDuration() {
        return discographyService.getAll().stream().mapToInt(Song::durationInSeconds).sum();
    }

    public int getAverageDuration() {
        List<Song> all = discographyService.getAll();
        if (all.isEmpty()) return 0;
        return getTotalDuration() / all.size();
    }

    public int getTotalFavorites() {
        return favoritesService.getAllFavorites().size();
    }


}
