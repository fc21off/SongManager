package de.st197974.songmanager;

import de.st197974.songmanager.repository.PlaylistRepository;
import de.st197974.songmanager.repository.SQLitePlaylistRepository;
import de.st197974.songmanager.repository.SQLiteSongRepository;
import de.st197974.songmanager.repository.SongRepository;
import de.st197974.songmanager.service.DiscographyService;
import de.st197974.songmanager.service.PlaylistService;
import de.st197974.songmanager.ui.SongManagerUI;

import javax.swing.*;

public class Main {

    static void main() {

        SongRepository songRepository = new SQLiteSongRepository();
        PlaylistRepository playlistRepository = new SQLitePlaylistRepository();
        DiscographyService discographyService = new DiscographyService(songRepository);
        PlaylistService playlistService = new PlaylistService(playlistRepository);


        SwingUtilities.invokeLater(() -> new SongManagerUI(discographyService, playlistService));

    }

}
