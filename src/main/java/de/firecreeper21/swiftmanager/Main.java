package de.firecreeper21.swiftmanager;

import de.firecreeper21.swiftmanager.repository.SQLiteSongRepository;
import de.firecreeper21.swiftmanager.repository.SongRepository;
import de.firecreeper21.swiftmanager.service.DiscographyService;
import de.firecreeper21.swiftmanager.ui.SwiftManagerWindow;

import javax.swing.*;
import java.util.List;

public class Main {

    static void main() {

        SongRepository repository = new SQLiteSongRepository();
        DiscographyService service = new DiscographyService(repository);

        SwingUtilities.invokeLater(() -> new SwiftManagerWindow(service));

    }

}
