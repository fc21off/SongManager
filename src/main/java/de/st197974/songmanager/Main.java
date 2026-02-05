package de.st197974.songmanager;

import de.st197974.songmanager.repository.*;
import de.st197974.songmanager.service.DiscographyService;
import de.st197974.songmanager.service.FavoritesService;
import de.st197974.songmanager.service.PlaylistService;
import de.st197974.songmanager.service.StatsService;
import de.st197974.songmanager.ui.SongManagerUI;

import javax.swing.*;

public class Main {

    static void main() {

        SongRepository songRepository = new SQLiteSongRepository();
        PlaylistRepository playlistRepository = new SQLitePlaylistRepository();
        FavoritesRepository favoritesRepository = new SQLiteFavoritesRepository();

        DiscographyService discographyService = new DiscographyService(songRepository);
        PlaylistService playlistService = new PlaylistService(playlistRepository);
        FavoritesService favoritesService = new FavoritesService(favoritesRepository, discographyService);
        StatsService statsService = new StatsService(discographyService, favoritesService);

        SwingUtilities.invokeLater(() -> new SongManagerUI(discographyService, playlistService, favoritesService, statsService));

    }

}
