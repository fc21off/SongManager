package de.st197974.swiftmanager;

import de.st197974.swiftmanager.repository.SQLiteSongRepository;
import de.st197974.swiftmanager.repository.SongRepository;
import de.st197974.swiftmanager.service.DiscographyService;
import de.st197974.swiftmanager.ui.SwiftManagerWindow;

import javax.swing.*;

public class Main {

    static void main() {

        SongRepository repository = new SQLiteSongRepository();
        DiscographyService service = new DiscographyService(repository);

        SwingUtilities.invokeLater(() -> new SwiftManagerWindow(service));

    }

}
