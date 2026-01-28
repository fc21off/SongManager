package de.st197974.songmanager.ui;

import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.service.DiscographyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class SongManagerUI extends JFrame {

    private static final Logger logger = LogManager.getLogger(SongManagerUI.class);

    private final DiscographyService service;

    private final DefaultListModel<String> artistModel = new DefaultListModel<>();
    private final DefaultListModel<Song> songModel = new DefaultListModel<>();

    private final JLabel statusBar = new JLabel();

    private JList<String> artistList;
    private JList<Song> songList;
    private JTextField songSearchField;

    public SongManagerUI(DiscographyService service) {
        this.service = service;

        setTitle("SongManager 2.0");
        setSize(750, 860);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        buildTop();
        buildCenter();
        buildBottom();

        loadArtists(null);

        setVisible(true);
    }

    private void buildTop() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();

        searchPanel.add(new JLabel(" Search Artist: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterArtists(searchField.getText().trim()); }
            public void removeUpdate(DocumentEvent e) { filterArtists(searchField.getText().trim()); }
            public void changedUpdate(DocumentEvent e) { filterArtists(searchField.getText().trim()); }
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
            public void insertUpdate(DocumentEvent e) { filterSongs(songSearchField.getText().trim()); }
            public void removeUpdate(DocumentEvent e) { filterSongs(songSearchField.getText().trim()); }
            public void changedUpdate(DocumentEvent e) { filterSongs(songSearchField.getText().trim()); }
        });

        JPanel rightPanel = new JPanel(new BorderLayout());

        JPanel songSearchPanel = new JPanel(new BorderLayout(5, 5));
        songSearchPanel.add(new JLabel("Search Song:"), BorderLayout.WEST);
        songSearchPanel.add(songSearchField, BorderLayout.CENTER);
        songSearchPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JButton sortByAlbumButton = new JButton("Sort by Album");
        sortByAlbumButton.addActionListener(_ -> sortSongsByAlbum());
        JButton sortByDurationButton = new JButton("Sort by Duration");
        sortByDurationButton.addActionListener(_ -> sortSongsByDuration());
        JButton sortAlphabeticallyButton = new JButton("Sort Alphabetically");
        sortAlphabeticallyButton.addActionListener(_ -> sortSongsAlphabetically((artistList.getSelectedValue() != null) ? artistList.getSelectedValue() : null));

        statusBar.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
        statusBar.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        buttonPanel.add(sortByAlbumButton);
        buttonPanel.add(sortByDurationButton);
        buttonPanel.add(sortAlphabeticallyButton);

        JPanel mainControlPanel = new JPanel(new BorderLayout(5, 5));
        mainControlPanel.add(buttonPanel, BorderLayout.CENTER); // Buttons oben/mitte
        mainControlPanel.add(statusBar, BorderLayout.SOUTH);    // Statusbar über die volle Breite unten

        mainControlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        rightPanel.add(songSearchPanel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(songList), BorderLayout.CENTER);
        rightPanel.add(mainControlPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(artistList),
                rightPanel
        );

        splitPane.setDividerLocation(220);
        add(splitPane, BorderLayout.CENTER);
    }

    private void buildBottom() {

        Border line = BorderFactory.createLineBorder(new Color(120, 120, 120), 1);
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);

        JButton addButton = new JButton("Add Song");
        JButton deleteButton = new JButton("Delete Song");
        JButton infoButton = new JButton("Song Info");
        JButton editButton = new JButton("Edit Song");

        addButton.addActionListener(_ -> triggerAddSongDialog());
        deleteButton.addActionListener(_ -> deleteSelectedSong());
        infoButton.addActionListener(_ -> showSongInfo());
        editButton.addActionListener(_ -> editSelectedSong());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(line, padding));

        buttonPanel.add(addButton);
        buttonPanel.add(infoButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);


        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(buttonPanel);

        add(bottom, BorderLayout.SOUTH);
    }


    private void loadArtists(String artistToSelect) {
        String current = (artistToSelect != null) ? artistToSelect : artistList.getSelectedValue();

        artistModel.clear();

        service.getAllArtists()
                .forEach(artistModel::addElement);

        if (current != null && artistModel.contains(current)) {
            artistList.setSelectedValue(current, true);
        } else if (!artistModel.isEmpty()) {
            artistList.setSelectedIndex(0);
        }
    }

    private void loadSongs(String artist) {
        songModel.clear();
        List<Song> songs = service.getSongsAlphabetically(artist);
        songs.forEach(songModel::addElement);


        updateStatusBar(songs, artist);
    }

    private void filterArtists(String query) {
        String lowerQuery = query.toLowerCase().trim();

        List<String> filtered = service.getAllArtists().stream()
                .filter(a -> a.toLowerCase().contains(lowerQuery))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        artistModel.clear();
        filtered.forEach(artistModel::addElement);

        if (!artistModel.isEmpty()) {
            artistList.setSelectedIndex(0);
        }
    }

    private void filterSongs(String query) {

        String artist = artistList.getSelectedValue();
        if (artist == null) return;

        String lowerQuery = query.toLowerCase();

        List<Song> filtered = service.getSongsByArtist(artist).stream()
                .filter(s -> s.getTitle().toLowerCase().contains(lowerQuery) || s.getAlbum().toLowerCase().contains(lowerQuery))
                .sorted(Comparator.comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();

        refreshSongList(filtered);

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
            String enteredArtist = artistField.getText().trim();
            addSong(titleField, albumField, artistField, durationField);
            loadArtists(enteredArtist);
        }
    }

    private void updateStatusBar(List<Song> songs, String artist) {

        int totalSeconds = songs.stream().mapToInt(Song::getDurationInSeconds).sum();

        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;

        statusBar.setText("Artist: " + artist + " | Songs: " + songs.size() + " | Time: " + min + ":" + String.format("%02d", sec));
    }

    private void refreshSongList(List<Song> songs) {
        songModel.clear();
        songs.forEach(songModel::addElement);
    }

    private void sortSongsByAlbum() {
        String artist = artistList.getSelectedValue();
        if (artist != null) {
            List<Song> sortedSongs = service.getSongsByArtistSortedByAlbum(artist);
            refreshSongList(sortedSongs);
        }
    }

    private void sortSongsByDuration() {
        String artist = artistList.getSelectedValue();
        if (artist != null) {
            List<Song> sortedSongs = service.getSongsByArtistSortedByDuration(artist);
            refreshSongList(sortedSongs);
        }
    }

    private void sortSongsAlphabetically(String artist) {

        if(artist != null) {
            List<Song> sortedSongs = service.getSongsAlphabetically(artist);
            refreshSongList(sortedSongs);
        }

    }


    private void editSelectedSong() {
        Song s = songList.getSelectedValue();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Select a song first!");
            return;
        }

        JTextField titleField = new JTextField(15);
        titleField.setText(s.getTitle());
        JTextField albumField = new JTextField(15);
        albumField.setText(s.getAlbum());
        JTextField artistField = new JTextField(15);
        artistField.setText(s.getArtist());
        JTextField durationField = new JTextField(15);
        durationField.setText(String.valueOf(s.getDurationInSeconds()));

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
            String enteredArtist = artistField.getText().trim();
            addSong(titleField, albumField, artistField, durationField);
            service.deleteSong(s.getId());
            loadArtists(enteredArtist);
        }

    }

}
