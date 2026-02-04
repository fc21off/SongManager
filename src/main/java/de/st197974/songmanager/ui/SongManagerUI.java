package de.st197974.songmanager.ui;

import de.st197974.songmanager.model.Playlist;
import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.service.DiscographyService;
import de.st197974.songmanager.service.FavoritesService;
import de.st197974.songmanager.service.PlaylistService;
import de.st197974.songmanager.service.StatsService;
import de.st197974.songmanager.ui.panels.FavoritesPanel;
import de.st197974.songmanager.ui.panels.PlaylistPanel;
import de.st197974.songmanager.ui.panels.StatsPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Comparator;
import java.util.List;

public class SongManagerUI extends JFrame {
    private static final Logger logger = LogManager.getLogger(SongManagerUI.class);

    private final Color ACCENT_COLOR = new Color(97, 182, 255);
    private final Color BG_COLOR = new Color(245, 245, 247);
    private final Color SIDEBAR_COLOR = new Color(255, 255, 255);
    private final Font MAIN_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private final DiscographyService discographyService;
    private final PlaylistService playlistService;
    private final FavoritesService favoritesService;
    private final StatsService statsService;

    private final DefaultListModel<String> artistModel = new DefaultListModel<>();
    private final DefaultListModel<Song> songModel = new DefaultListModel<>();

    private JTabbedPane tabbedPane;
    private PlaylistPanel playlistPanel;
    private FavoritesPanel favoritesPanel;
    private StatsPanel statsPanel;

    private final Song EMPTY_SONG_PLACEHOLDER = new Song("null", "No Songs found...", "", "", 0);
    private final JLabel statusBar = new JLabel("Ready");
    private JList<String> artistList;
    private JList<Song> songList;
    private JTextField songSearchField;
    private JTextField artistSearchField;

    public SongManagerUI(DiscographyService discographyService, PlaylistService playlistService, FavoritesService favoritesService, StatsService statsService) {
        this.discographyService = discographyService;
        this.playlistService = playlistService;
        this.favoritesService = favoritesService;
        this.statsService = statsService;

        setTitle("SongManager 3.0");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout());

        buildCenter();
        buildBottom();

        loadArtists(null);
        setVisible(true);
    }

    private void buildCenter() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 12));

        artistList = new JList<>(artistModel);
        artistList.setBackground(SIDEBAR_COLOR);
        artistList.setFixedCellHeight(30);
        artistList.setBorder(new EmptyBorder(5, 5, 5, 5));

        songList = new JList<>(songModel);
        songList.setFixedCellHeight(50);
        songList.setCellRenderer(createModernSongRenderer());

        playlistPanel = new PlaylistPanel(discographyService.repository(), playlistService);
        favoritesPanel = new FavoritesPanel(favoritesService);
        statsPanel = new StatsPanel(statsService);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_COLOR);
        artistSearchField = createStyledTextField(" Search Artist...");

        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setOpaque(false);
        searchWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
        searchWrapper.add(artistSearchField, BorderLayout.CENTER);

        sidebar.add(searchWrapper, BorderLayout.NORTH);
        sidebar.add(new JScrollPane(artistList), BorderLayout.CENTER);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(Color.WHITE);

        JPanel songHeader = new JPanel(new BorderLayout(10, 10));
        songHeader.setBackground(Color.WHITE);
        songHeader.setBorder(new EmptyBorder(10, 10, 10, 10));

        songSearchField = createStyledTextField(" Songtitle or Album...");
        songHeader.add(songSearchField, BorderLayout.CENTER);

        JPanel sortActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sortActions.setOpaque(false);
        sortActions.add(createIconButton("Sort A-Z", _ -> sortSongsAlphabetically(artistList.getSelectedValue())));
        sortActions.add(createIconButton("Album", _ -> sortSongsByAlbum()));
        sortActions.add(createIconButton("Duration", _ -> sortSongsByDuration()));

        mainContent.add(songHeader, BorderLayout.NORTH);
        mainContent.add(new JScrollPane(songList), BorderLayout.CENTER);
        mainContent.add(sortActions, BorderLayout.SOUTH);

        JSplitPane librarySplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, mainContent);
        librarySplit.setDividerLocation(250);
        librarySplit.setDividerSize(1);
        librarySplit.setBorder(null);

        addStyledTab(" Library", librarySplit);
        addStyledTab("  Playlists  ", playlistPanel);
        addStyledTab("  Favorites  ", favoritesPanel);
        addStyledTab("  Statistics  ", statsPanel);

        tabbedPane.addChangeListener(e -> refreshTabData());

        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFocusable(false);
        tabbedPane.setBorder(new EmptyBorder(10, 10, 0, 10));

        artistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String artist = artistList.getSelectedValue();
                if (artist != null) loadSongs(artist);
            }
        });
        addSearchListener(artistSearchField, () -> filterArtists(artistSearchField.getText().trim()));
        addSearchListener(songSearchField, () -> filterSongs(songSearchField.getText().trim()));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void buildBottom() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        actionButtons.setOpaque(false);

        actionButtons.add(createPrimaryButton("Add Song", _ -> showSongForm(null)));
        actionButtons.add(createSecondaryButton("Song Info", _ -> showSongInfo()));
        actionButtons.add(createSecondaryButton("Edit Song", _ -> editSelectedSong()));
        actionButtons.add(createSecondaryButton("Delete Song", _ -> deleteSelectedSong()));

        statusBar.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusBar.setBorder(new EmptyBorder(0, 20, 0, 40));

        footer.add(actionButtons, BorderLayout.WEST);
        footer.add(statusBar, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        setupSongListContextMenu();
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(100, 40));
        field.setBackground(new Color(238, 238, 240));
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));

        Border normalBorder = BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 12, 5, 12)
        );

        Border focusBorder = BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT_COLOR, 2),
                new EmptyBorder(4, 11, 4, 11)
        );

        field.setBorder(normalBorder);

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(focusBorder);
                field.setBackground(Color.WHITE);
            }

            public void focusLost(FocusEvent e) {
                field.setBorder(normalBorder);
                field.setBackground(new Color(238, 238, 240));
            }
        });

        return field;
    }

    private JButton createPrimaryButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(listener);
        return btn;
    }

    private JButton createSecondaryButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.addActionListener(listener);
        return btn;
    }

    private JButton createIconButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setMargin(new Insets(2, 8, 2, 8));
        btn.addActionListener(listener);
        return btn;
    }

    private DefaultListCellRenderer createModernSongRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel itemPanel = new JPanel(new BorderLayout(15, 0));
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                        BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
                itemPanel.setBackground(isSelected ? new Color(199, 221, 253) : Color.WHITE);

                if (value instanceof Song song) {
                    JLabel titleLabel = new JLabel("<html><b>" + song.title() + "</b><br><font color='gray'>" + song.artist() + (song.album().isEmpty() ? "" : " • " + song.album()) + "</font></html>");
                    titleLabel.setFont(MAIN_FONT);

                    boolean isFav = favoritesService.isFavorite(song.id());
                    JLabel rightLabel = new JLabel((isFav ? "★ " : "") + song.formatTime(song.durationInSeconds()));
                    rightLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
                    rightLabel.setForeground(isFav ? new Color(255, 174, 0) : Color.DARK_GRAY);

                    itemPanel.add(titleLabel, BorderLayout.CENTER);
                    itemPanel.add(rightLabel, BorderLayout.EAST);
                }
                return itemPanel;
            }
        };
    }

    private void refreshTabData() {
        int idx = tabbedPane.getSelectedIndex();

        statusBar.setVisible(idx == 0);

        switch (idx) {
            case 1 -> playlistPanel.loadPlaylists();
            case 2 -> favoritesPanel.loadFavorites();
            case 3 -> statsPanel.loadStatistics();
        }
        logger.info("Tab {} reloaded", idx);
    }

    private void addStyledTab(String title, JComponent panel) {
        tabbedPane.addTab(null, panel);
        int index = tabbedPane.getTabCount() - 1;

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setBorder(new EmptyBorder(8, 15, 8, 15));

        tabbedPane.setTabComponentAt(index, lbl);
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

        if (tabbedPane != null && tabbedPane.getSelectedIndex() == 1) {
            s = playlistPanel.getSelectedSong();
        } else if (tabbedPane != null && tabbedPane.getSelectedIndex() == 2) {
            s = favoritesPanel.getSelectedFavorite();
        } else {
            s = songList.getSelectedValue();
        }

        if (s == null || s == EMPTY_SONG_PLACEHOLDER) {
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

        if (tabbedPane != null && tabbedPane.getSelectedIndex() >= 1) {
            JOptionPane.showMessageDialog(this,
                    "Unable to EDIT here!\n" +
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

        if (tabbedPane != null && tabbedPane.getSelectedIndex() >= 1) {
            JOptionPane.showMessageDialog(this,
                    "Unable to DELETE here!\n" +
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

        popupMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                popupMenu.removeAll();

                Song selectedSong = songList.getSelectedValue();
                if (selectedSong == null || selectedSong == EMPTY_SONG_PLACEHOLDER) return;

                JMenuItem addToPlaylist = new JMenuItem("Add to Playlist...");
                addToPlaylist.addActionListener(_ -> showPlaylistSelectionDialog(selectedSong));
                popupMenu.add(addToPlaylist);

                popupMenu.addSeparator();

                if (favoritesService.isFavorite(selectedSong.id())) {
                    JMenuItem removeFromFavorites = new JMenuItem("Remove from Favorites");
                    removeFromFavorites.addActionListener(_ -> removeSongFromFavorites(selectedSong));
                    popupMenu.add(removeFromFavorites);
                } else {
                    JMenuItem addToFavorites = new JMenuItem("Add to Favorites");
                    addToFavorites.addActionListener(_ -> addSongToFavorites(selectedSong));
                    popupMenu.add(addToFavorites);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
            }
        });

        songList.setComponentPopupMenu(popupMenu);

        songList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = songList.locationToIndex(e.getPoint());
                    if (row != -1) {
                        songList.setSelectedIndex(row);
                    }
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

                if (added) {
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

    private void addSongToFavorites(Song selectedSong) {

        boolean added = favoritesService.addFavorite(selectedSong.id());

        if (added) {
            statusBar.setForeground(new Color(0, 150, 0));
            statusBar.setText("Added '" + selectedSong.title() + "' to Favorites!");

            if (tabbedPane != null && favoritesPanel != null) {
                favoritesPanel.loadFavorites();
            }

        } else {
            statusBar.setForeground(new Color(150, 0, 0));
            statusBar.setText("Song '" + selectedSong.title() + "' is already in Favorites!");
        }

    }

    private void removeSongFromFavorites(Song selectedSong) {

        statusBar.setForeground(new Color(150, 100, 0));
        statusBar.setText("Song '" + selectedSong.title() + "' was removed from Favorites");
        favoritesService.removeFavorite(selectedSong.id());

        if (tabbedPane != null && favoritesPanel != null) {
            favoritesPanel.loadFavorites();
        }

    }

}
