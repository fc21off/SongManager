package de.st197974.swiftmanager.ui;

import de.st197974.swiftmanager.model.Song;
import de.st197974.swiftmanager.service.DiscographyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class SwiftManagerWindow extends JFrame {

    private static final Logger logger = LogManager.getLogger(SwiftManagerWindow.class);

    private final DiscographyService service;

    private final DefaultListModel<String> artistModel = new DefaultListModel<>();
    private final DefaultListModel<Song> songModel = new DefaultListModel<>();

    private final JLabel statusBar = new JLabel();

    private JList<String> artistList;
    private JList<Song> songList;
    private JTextField songSearchField;

    public SwiftManagerWindow(DiscographyService service) {
        this.service = service;

        setTitle("SongManager 2.0");
        setSize(750, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        buildTop();
        buildCenter();
        buildBottom();

        loadArtists();

        setVisible(true);
    }

    private void buildTop() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();

        searchPanel.add(new JLabel(" Search Artist: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterArtists(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterArtists(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterArtists(searchField.getText()); }
        });

        add(searchPanel, BorderLayout.NORTH);
    }

    private void buildCenter() {
        artistList = new JList<>(artistModel);
        songList = new JList<>(songModel);

        artistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        artistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String artist = artistList.getSelectedValue();
                if (artist != null) loadSongs(artist);
            }
        });

        songSearchField = new JTextField();
        songSearchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {  }
            public void removeUpdate(DocumentEvent e) {  }
            public void changedUpdate(DocumentEvent e) {  }
        });

        JPanel rightPanel = new JPanel(new BorderLayout());

        JPanel songSearchPanel = new JPanel(new BorderLayout(5, 5));
        songSearchPanel.add(new JLabel("Search Song:"), BorderLayout.WEST);
        songSearchPanel.add(songSearchField, BorderLayout.CENTER);
        songSearchPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JButton sortByAlbumButton = new JButton("Sort by Album");
        JButton sortByDurationButton = new JButton("Sort by Duration");

        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        buttonPanel.add(sortByAlbumButton);
        buttonPanel.add(sortByDurationButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        rightPanel.add(songSearchPanel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(songList), BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(artistList),
                rightPanel
        );

        splitPane.setDividerLocation(220);
        add(splitPane, BorderLayout.CENTER);
    }

    private void buildBottom() {

        JButton addButton = new JButton("Add Song");
        JButton deleteButton = new JButton("Delete Song");
        JButton infoButton = new JButton("Song Info");

        addButton.addActionListener(e -> triggerAddSongDialog());
        deleteButton.addActionListener(e -> deleteSelectedSong());
        infoButton.addActionListener(e -> showSongInfo());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(infoButton);
        buttonPanel.add(deleteButton);

        statusBar.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(statusBar);
        bottom.add(buttonPanel);

        add(bottom, BorderLayout.SOUTH);
    }

    private void loadArtists() {
        artistModel.clear();

        service.getAllArtists()
                .forEach(artistModel::addElement);

        if (!artistModel.isEmpty()) {
            artistList.setSelectedIndex(0);
        }
    }

    private void loadSongs(String artist) {
        songModel.clear();

        List<Song> songs = service.getSongsByArtist(artist);
        songs.forEach(songModel::addElement);

        updateStatusBar(songs, artist);
    }

    private void filterArtists(String query) {
        artistModel.clear();

        service.getAllArtists().stream()
                .filter(a -> a.toLowerCase().contains(query.toLowerCase()))
                .forEach(artistModel::addElement);
    }

    private void filterSongs(String query) {
        songModel.clear();

        service.getSongsByArtist(query);

    }

    private void deleteSelectedSong() {
        Song song = songList.getSelectedValue();
        if (song == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete '" + song.getTitle() + "'?",
                "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            service.deleteSong(song.getId());
            loadSongs(song.getArtist());
            logger.info("Deleted song {}", song.getTitle());
        }
    }

    private void showSongInfo() {
        Song s = songList.getSelectedValue();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Select a song first!");
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Title: " + s.getTitle() +
                        "\nArtist: " + s.getArtist() +
                        "\nAlbum: " + s.getAlbum() +
                        "\nDuration: " + s.formatTime(s.getDurationInSeconds()) + " (" + s.getDurationInSeconds() + "s)",
                "Song Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void addSong(JTextField titleField, JTextField albumField, JTextField artistField, JTextField durationField) {

        try {
            String title = titleField.getText();
            String album = albumField.getText();
            String artist = artistField.getText();
            int duration = Integer.parseInt(durationField.getText());

            Song newSong = new Song(title, album, artist, duration);
            service.addSongSafely(newSong);

            titleField.setText("");
            albumField.setText("");
            durationField.setText("");

            loadSongs(artist);
            logger.info("Added new song: {}", title);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid number for duration!");
        }
    }

    public void triggerAddSongDialog() {
        JTextField titleField = new JTextField(15);
        JTextField albumField = new JTextField(15);
        JTextField artistField = new JTextField(15);
        JTextField durationField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel(" Title: "));
        panel.add(titleField);
        panel.add(new JLabel(" Album: "));
        panel.add(albumField);
        panel.add(new JLabel(" Artist: "));
        panel.add(artistField);
        panel.add(new JLabel(" Duration (in s): "));
        panel.add(durationField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Add new Song!",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            addSong(titleField, albumField, artistField, durationField);
            loadArtists();
        }
    }

    private void updateStatusBar(List<Song> songs, String artist) {

        int totalSeconds = songs.stream().mapToInt(Song::getDurationInSeconds).sum();

        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;

        statusBar.setText("Artist: " + artist + " | Songs: " + songs.size() + " | Time: " + min + ":" + String.format("%02d", sec));
    }
}
