package de.st197974.songmanager.service;

import de.st197974.songmanager.model.Song;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record StatsService(DiscographyService discographyService, FavoritesService favoritesService) {

    public List<Song> getAllSongs() {
        return discographyService.getAll();
    }

    public Map<String, Long> getSongsPerArtist() {
        return discographyService.getAll().stream()
                .collect(Collectors.groupingBy(Song::artist, Collectors.counting()));
    }

    public int getTotalSongs() {
        return discographyService.getAll().size();
    }

    public int getTotalDuration() {
        return discographyService.getAll().stream()
                .mapToInt(Song::durationInSeconds)
                .sum();
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
