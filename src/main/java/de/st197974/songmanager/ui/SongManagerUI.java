package de.st197974.songmanager.ui;

import de.st197974.songmanager.model.Playlist;
import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.service.DiscographyService;
import de.st197974.songmanager.service.PlaylistService;
import de.st197974.songmanager.ui.panels.FavoritesPanel;
import de.st197974.songmanager.ui.panels.PlaylistPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;

public class SongManagerUI extends JFrame {

    private static final Logger logger = LogManager.getLogger(SongManagerUI.class);

    private final DiscographyService discographyService;
    private final PlaylistService playlistService;

    private final DefaultListModel<String> artistModel = new DefaultListModel<>();
    private final DefaultListModel<Song> songModel = new DefaultListModel<>();

    private JTabbedPane tabbedPane;

    private PlaylistPanel playlistPanel;
    private FavoritesPanel favoritesPanel;

    private final Song EMPTY_SONG_PLACEHOLDER = new Song("null", "No Songs found...", "", "", 0);

    private final JLabel statusBar = new JLabel();

    private JList<String> artistList;
    private JList<Song> songList;
    private JTextField songSearchField;

    public SongManagerUI(DiscographyService discographyService, PlaylistService playlistService) {
        this.discographyService = discographyService;
        this.playlistService = playlistService;

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
        silenceBackspace(searchField);

        searchPanel.add(new JLabel(" Search Artist: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        addSearchListener(searchField, () -> filterArtists(searchField.getText().trim()));

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
        silenceBackspace(songSearchField);
        addSearchListener(songSearchField, () -> filterSongs(songSearchField.getText().trim()));

        JPanel rightPanel = new JPanel(new BorderLayout());

        JPanel songSearchPanel = new JPanel(new BorderLayout(5, 5));
        songSearchPanel.add(new JLabel("Search Song:"), BorderLayout.WEST);
        songSearchPanel.add(songSearchField, BorderLayout.CENTER);
        songSearchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton sortByAlbumButton = new JButton("Sort by Album");
        sortByAlbumButton.addActionListener(_ -> sortSongsByAlbum());
        JButton sortByDurationButton = new JButton("Sort by Duration");
        sortByDurationButton.addActionListener(_ -> sortSongsByDuration());
        JButton sortAlphabeticallyButton = new JButton("Sort Alphabetically");
        sortAlphabeticallyButton.addActionListener(_ -> sortSongsAlphabetically((artistList.getSelectedValue() != null) ? artistList.getSelectedValue() : null));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        buttonPanel.add(sortByAlbumButton);
        buttonPanel.add(sortByDurationButton);
        buttonPanel.add(sortAlphabeticallyButton);

        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusBar.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel mainControlPanel = new JPanel(new BorderLayout(5, 5));
        mainControlPanel.add(buttonPanel, BorderLayout.CENTER);
        mainControlPanel.add(statusBar, BorderLayout.SOUTH);
        mainControlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        rightPanel.add(songSearchPanel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(songList), BorderLayout.CENTER);
        rightPanel.add(mainControlPanel, BorderLayout.SOUTH);

        JSplitPane librarySplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(artistList), rightPanel);
        librarySplitPane.setDividerLocation(220);

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Library", librarySplitPane);

        playlistPanel = new PlaylistPanel(discographyService.repository(), playlistService);
        tabbedPane.addTab("Playlists", playlistPanel);

        tabbedPane.addChangeListener(e -> {

            if (tabbedPane.getSelectedIndex() == 1) {

                playlistPanel.loadPlaylists();
                logger.info("Reloaded Playlist Data");
            }
        });


        add(tabbedPane, BorderLayout.CENTER);
    }

    private void buildBottom() {

        Border line = BorderFactory.createLineBorder(new Color(120, 120, 120), 1);
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);

        JButton addButton = new JButton("Add Song");
        JButton deleteButton = new JButton("Delete Song");
        JButton infoButton = new JButton("Song Info");
        JButton editButton = new JButton("Edit Song");

        addButton.addActionListener(_ -> showSongForm(null));
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

        setupSongListContextMenu();
    }


    private void loadArtists(String artistToSelect) {
        String current = (artistToSelect != null) ? artistToSelect : artistList.getSelectedValue();

        artistModel.clear();

        discographyService.getAllArtists().forEach(artistModel::addElement);

        if (current != null && artistModel.contains(current)) {
            artistList.setSelectedValue(current, true);
        } else if (!artistModel.isEmpty()) {
            artistList.setSelectedIndex(0);
        }
    }

    private void loadSongs(String artist) {
        songModel.clear();
        List<Song> songs = discographyService.getSongsAlphabetically(artist);
        songs.forEach(songModel::addElement);

        updateStatusBar(songs, artist);
    }

    private void filterArtists(String query) {
        String lowerQuery = query.toLowerCase().trim();

        List<String> filtered = discographyService.getAllArtists().stream().filter(a -> a.toLowerCase().contains(lowerQuery)).sorted(String.CASE_INSENSITIVE_ORDER).toList();

        artistModel.clear();

        if (filtered.isEmpty() && !lowerQuery.isEmpty()) {
            artistModel.addElement(" No Result for '" + query + "'!");
        } else {
            filtered.forEach(artistModel::addElement);
            if (!artistModel.isEmpty()) {
                artistList.setSelectedIndex(0);
            }
        }

    }

    private void filterSongs(String query) {
        String lowerQuery = query.toLowerCase();
        List<Song> results;

        if (lowerQuery.isEmpty()) {
            String artist = artistList.getSelectedValue();
            if (artist == null) {
                songModel.clear();
                return;
            }
            results = discographyService.getSongsByArtist(artist);
        } else {
            results = discographyService.getAll().stream().filter(s -> s.title().toLowerCase().contains(lowerQuery) || s.album().toLowerCase().contains(lowerQuery) || s.artist().toLowerCase().contains(lowerQuery)).sorted(Comparator.comparing(Song::title, String.CASE_INSENSITIVE_ORDER)).toList();
        }

        refreshSongList(results);

        String statusInfo = lowerQuery.isEmpty() ? artistList.getSelectedValue() : "Search: '" + query + "'";
        updateStatusBar(results, statusInfo);

    }

    private void showSongInfo() {
        Song s;

        if(tabbedPane != null && tabbedPane.getSelectedIndex() == 1) {
            s = playlistPanel.getSelectedSong();
        } else {
            s = songList.getSelectedValue();
        }

        if(s == null || s == EMPTY_SONG_PLACEHOLDER) {
            JOptionPane.showMessageDialog(this, "Select a song first!");
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Title: " + s.title() +
                        "\nArtist: " + s.artist() +
                        "\nAlbum: " + s.album() +
                        "\nDuration: " + s.formatTime(s.durationInSeconds()) + " (" + s.durationInSeconds() + "s)",
                "Song Info",
                JOptionPane.INFORMATION_MESSAGE);

    }

    private void updateStatusBar(List<Song> songs, String artist) {

        int totalSeconds = songs.stream().mapToInt(Song::durationInSeconds).sum();

        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;

        statusBar.setForeground(Color.BLACK);
        statusBar.setText("Artist: " + artist + " | Songs: " + songs.size() + " | Time: " + min + ":" + String.format("%02d", sec));
    }

    private void refreshSongList(List<Song> songs) {
        songModel.clear();
        if (songs.isEmpty()) {
            songModel.addElement(EMPTY_SONG_PLACEHOLDER);
        } else {
            songs.forEach(songModel::addElement);
        }
    }

    private void sortSongsByAlbum() {
        String artist = artistList.getSelectedValue();
        if (artist != null) {
            List<Song> sortedSongs = discographyService.getSongsByArtistSortedByAlbum(artist);
            refreshSongList(sortedSongs);
        }
    }

    private void sortSongsByDuration() {
        String artist = artistList.getSelectedValue();
        if (artist != null) {
            List<Song> sortedSongs = discographyService.getSongsByArtistSortedByDuration(artist);
            refreshSongList(sortedSongs);
        }
    }

    private void sortSongsAlphabetically(String artist) {

        if (artist != null) {
            List<Song> sortedSongs = discographyService.getSongsAlphabetically(artist);
            refreshSongList(sortedSongs);
        }

    }

    private void editSelectedSong() {

        if (tabbedPane != null && tabbedPane.getSelectedIndex() == 1) {
            JOptionPane.showMessageDialog(this,
                    "Unable to EDIT while viewing playlists.\n" +
                            "Please switch to 'Library' to manage the main library.",
                    "Action Not Allowed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Song s = songList.getSelectedValue();
        if (s == null || s == EMPTY_SONG_PLACEHOLDER) {
            JOptionPane.showMessageDialog(this, "Select a song first!");
            return;
        }
        showSongForm(s);
    }

    private void deleteSelectedSong() {

        if (tabbedPane != null && tabbedPane.getSelectedIndex() == 1) {
            JOptionPane.showMessageDialog(this,
                    "Unable to DELETE while viewing playlists.\n" +
                            "Please switch to 'Library' to manage the main library.",
                    "Action Not Allowed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Song s = songList.getSelectedValue();
        if (s == null || s == EMPTY_SONG_PLACEHOLDER) {
            JOptionPane.showMessageDialog(this, "Select a song first!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete '" + s.title() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {

            String currentArtist = s.artist();

            discographyService.deleteSong(s.id());
            loadSongs(s.artist());
            loadArtists(null);

            if (artistModel.contains(currentArtist)) {
                loadSongs(currentArtist);
            }

            logger.info("Deleted song {}", s.title());
        }
    }

    private void showSongForm(Song songToEdit) {
        boolean isEdit = (songToEdit != null);

        JTextField titleField = new JTextField(isEdit ? songToEdit.title() : "", 15);
        silenceBackspace(titleField);
        JTextField albumField = new JTextField(isEdit ? songToEdit.album() : "", 15);
        silenceBackspace(albumField);
        JTextField artistField = new JTextField(isEdit ? songToEdit.artist() : "", 15);
        silenceBackspace(artistField);
        JTextField durationField = new JTextField(isEdit ? String.valueOf(songToEdit.durationInSeconds()) : "", 15);
        silenceBackspace(durationField);

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel panel = new JPanel(new GridLayout(0, 1, 2, 2));
        panel.add(new JLabel(" Title: "));
        panel.add(titleField);
        panel.add(new JLabel(" Artist: "));
        panel.add(artistField);
        panel.add(new JLabel(" Album: "));
        panel.add(albumField);
        panel.add(new JLabel(" Duration (in s): "));
        panel.add(durationField);
        panel.add(errorLabel);

        boolean valid = false;
        while (!valid) {
            int result = JOptionPane.showConfirmDialog(this, panel, isEdit ? "Edit Song" : "Add New Song", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    String title = titleField.getText().trim();
                    String album = albumField.getText().trim();
                    String artist = artistField.getText().trim();
                    int duration = Integer.parseInt(durationField.getText().trim());

                    if (isEdit) {
                        Song updatedSong = new Song(songToEdit.id(), title, album, artist, duration);
                        discographyService.updateSongSafely(updatedSong);
                    } else {
                        discographyService.addSongSafely(new Song(title, album, artist, duration));

                        if (tabbedPane != null) {
                            tabbedPane.setSelectedIndex(0);
                        }
                    }

                    loadArtists(artist);
                    loadSongs(artist);

                    logger.info("{} song: {}", isEdit ? "Updated" : "Added", title);
                    valid = true;

                } catch (NumberFormatException e) {
                    errorLabel.setText(" Please enter a valid number for duration!");
                }
            } else {
                valid = true;
            }
        }
    }

    private void silenceBackspace(JTextField textField) {

        Action originalAction = textField.getActionMap().get("delete-previous");

        textField.getActionMap().put("delete-previous", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!textField.getText().isEmpty()) {
                    originalAction.actionPerformed(e);
                }
            }
        });

    }

    private void addSearchListener(JTextField textField, Runnable action) {
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }

    private void setupSongListContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem addToPlaylist = new JMenuItem("Add to Playlist...");

        addToPlaylist.addActionListener(_ -> {
            Song selectedSong = songList.getSelectedValue();
            if (selectedSong != null && selectedSong != EMPTY_SONG_PLACEHOLDER) {
                showPlaylistSelectionDialog(selectedSong);
            }
        });

        popupMenu.add(addToPlaylist);

        songList.setComponentPopupMenu(popupMenu);

        songList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = songList.locationToIndex(e.getPoint());
                    songList.setSelectedIndex(row);
                }
            }
        });
    }

    private void showPlaylistSelectionDialog(Song song) {
        List<Playlist> playlists = playlistService.getAllPlaylists();

        if (playlists.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No playlists found. Create one first!");
            return;
        }

        Playlist[] playlistArray = playlists.toArray(new Playlist[0]);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Add '" + song.title() + "' to:"), BorderLayout.NORTH);

        JComboBox<Playlist> playlistCombo = new JComboBox<>(playlistArray);
        panel.add(playlistCombo, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Select Playlist", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Playlist selected = (Playlist) playlistCombo.getSelectedItem();
            if (selected != null) {
                boolean added = playlistService.addSongToPlaylist(selected.id(), song.id());

                if(added) {
                    if (playlistPanel != null) {
                        playlistPanel.loadPlaylists();
                    }
                    statusBar.setForeground(new Color(0, 150, 0));
                    statusBar.setText("Added '" + song.title() + "' to '" + selected.name() + "'!");
                } else {
                    statusBar.setForeground(new Color(150, 0, 0));
                    statusBar.setText("Song '" + song.title() + "' is already in '" + selected.name() + "'!");
                }

            }
        }
    }

}
