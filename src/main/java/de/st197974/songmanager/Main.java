package de.st197974.songmanager;

import de.st197974.songmanager.repository.*;
import de.st197974.songmanager.service.*;
import de.st197974.songmanager.ui.AppTheme;
import de.st197974.songmanager.ui.SongManagerUI;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;

public class Main {

    void main(String[] args) {
        System.setProperty("flatlaf.uiScale", "1.0");

        AppTheme.applyLightTheme();

        SongRepository songRepository = new SQLiteSongRepository();
        PlaylistRepository playlistRepository = new SQLitePlaylistRepository();
        FavoritesRepository favoritesRepository = new SQLiteFavoritesRepository();

        DiscographyService discographyService = new DiscographyService(songRepository);
        PlaylistService playlistService = new PlaylistService(playlistRepository);
        FavoritesService favoritesService = new FavoritesService(favoritesRepository, discographyService);
        StatsService statsService = new StatsService(discographyService, favoritesService);
        ArtistService artistService = new ArtistService(songRepository);

        discographyService.cleanupInvalidSongs();

        SwingUtilities.invokeLater(() -> {
            new SongManagerUI(discographyService, playlistService, favoritesService, statsService, artistService);
        });
    }
}