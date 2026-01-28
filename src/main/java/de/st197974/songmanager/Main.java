package de.st197974.songmanager;

import de.st197974.songmanager.repository.SQLiteSongRepository;
import de.st197974.songmanager.repository.SongRepository;
import de.st197974.songmanager.service.DiscographyService;
import de.st197974.songmanager.ui.SongManagerUI;

import javax.swing.*;

public class Main {

    static void main() {

        SongRepository repository = new SQLiteSongRepository();
        DiscographyService service = new DiscographyService(repository);

        SwingUtilities.invokeLater(() -> new SongManagerUI(service));

    }

}
