package de.st197974.songmanager.ui;

import de.st197974.songmanager.model.Playlist;
import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.service.*;
import de.st197974.songmanager.ui.panels.MultiEditPanel;
import de.st197974.songmanager.ui.panels.FavoritesPanel;
import de.st197974.songmanager.ui.panels.PlaylistPanel;
import de.st197974.songmanager.ui.panels.StatsPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The SongManagerUI class provides a user interface for managing songs, playlists, favorites, artists, and playback statistics.
 * It extends JFrame and incorporates various panels, components, and services to enable seamless interaction
 * with the application's core functionalities.
 */
public class SongManagerUI extends JFrame {
    private static final Logger logger = LogManager.getLogger(SongManagerUI.class);

    private final Font MAIN_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private final DiscographyService discographyService;
    private final PlaylistService playlistService;
    private final FavoritesService favoritesService;
    private final StatsService statsService;
    private final ArtistService artistService;

    private final DefaultListModel<String> artistModel = new DefaultListModel<>();
    private final DefaultListModel<Song> songModel = new DefaultListModel<>();

    private JTabbedPane tabbedPane;
    private PlaylistPanel playlistPanel;
    private FavoritesPanel favoritesPanel;
    private MultiEditPanel multiEditPanel;
    private StatsPanel statsPanel;
    private JPanel sidebar;
    private JPanel songHeader;
    private JPanel footer;
    private JPanel mainContent;
    private JSplitPane librarySplit;

    private final List<JButton> primaryButtons = new ArrayList<>();
    private final List<JTextField> styledTextFields = new ArrayList<>();

    private final Song EMPTY_SONG_PLACEHOLDER = new Song("null", "No Songs found...", "", "", 0);
    private final JLabel statusBar = new JLabel("Ready...");
    private JList<String> artistList;
    private JList<Song> songList;
    private JTextField songSearchField;
    private JTextField artistSearchField;
    private JToggleButton darkModeToggle;

    public SongManagerUI(DiscographyService discographyService, PlaylistService playlistService, FavoritesService favoritesService, StatsService statsService, ArtistService artistService) {

        AppTheme.applyLightTheme();

        this.discographyService = discographyService;
        this.playlistService = playlistService;
        this.favoritesService = favoritesService;
        this.statsService = statsService;
        this.artistService = artistService;

        setTitle("SongManager 3.0");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        buildCenter();
        buildBottom();

        updateUIColors();

        loadArtists(null);
        setVisible(true);
    }

    private void buildCenter() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 12));

        artistList = new JList<>(artistModel);

        artistList.setFixedCellHeight(30);
        artistList.setBorder(new EmptyBorder(5, 5, 5, 5));
        artistList.setCellRenderer(createArtistRenderer());

        songList = new JList<>(songModel);
        songList.setFixedCellHeight(50);
        songList.setCellRenderer(createModernSongRenderer());

        playlistPanel = new PlaylistPanel(discographyService.repository(), playlistService);
        favoritesPanel = new FavoritesPanel(favoritesService);
        multiEditPanel = new MultiEditPanel(discographyService, favoritesService, this);
        statsPanel = new StatsPanel(statsService);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(5, 5, 0, 10));
        topBar.add(tabbedPane, BorderLayout.CENTER);

        sidebar = new JPanel(new BorderLayout());

        artistSearchField = createStyledTextField();
        artistSearchField.setPreferredSize(new Dimension(200, 30));

        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setOpaque(false);
        searchWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
        searchWrapper.add(artistSearchField, BorderLayout.CENTER);

        sidebar.add(searchWrapper, BorderLayout.NORTH);
        sidebar.add(new JScrollPane(artistList), BorderLayout.CENTER);

        mainContent = new JPanel(new BorderLayout());

        songHeader = new JPanel(new BorderLayout(10, 10));
        songHeader.setBorder(new EmptyBorder(10, 10, 10, 10));

        songSearchField = createStyledTextField();
        songSearchField.setPreferredSize(new Dimension(200, 30));
        songHeader.add(songSearchField, BorderLayout.CENTER);

        JPanel sortActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sortActions.setOpaque(false);
        sortActions.add(createSortButton("Sort A-Z", _ -> sortSongsAlphabetically(artistList.getSelectedValue())));
        sortActions.add(createSortButton("Album", _ -> sortSongsByAlbum()));
        sortActions.add(createSortButton("Duration", _ -> sortSongsByDuration()));

        mainContent.add(songHeader, BorderLayout.NORTH);
        mainContent.add(new JScrollPane(songList), BorderLayout.CENTER);
        mainContent.add(sortActions, BorderLayout.SOUTH);

        librarySplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, mainContent);
        librarySplit.setDividerLocation(250);
        librarySplit.setDividerSize(1);
        librarySplit.setBackground(new Color(220, 220, 220));

        addStyledTab("  Library", librarySplit);
        addStyledTab("  Playlists  ", playlistPanel);
        addStyledTab("  Favorites  ", favoritesPanel);
        addStyledTab("  Multi Edit  ", multiEditPanel);
        addStyledTab("  Statistics  ", statsPanel);

        tabbedPane.addChangeListener(_ -> refreshTabData());

        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFocusable(false);
        tabbedPane.setBorder(new EmptyBorder(10, 10, 0, 10));
        tabbedPane.setSelectedIndex(0);

        artistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String artist = artistList.getSelectedValue();
                if (artist != null) loadSongs(artist);
            }
        });
        addSearchListener(artistSearchField, () -> filterArtists(artistSearchField.getText().trim()));
        addSearchListener(songSearchField, () -> filterSongs(songSearchField.getText().trim()));

        add(topBar, BorderLayout.CENTER);
        refreshTabData();
    }

    private void buildBottom() {
        footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        actionButtons.setOpaque(false);

        actionButtons.add(createPrimaryButton("Add Song", _ -> showSongForm(null)));
        actionButtons.add(createSecondaryButton("Song Info", _ -> showSongInfo()));
        actionButtons.add(createSecondaryButton("Edit Song", _ -> editSelectedSong()));
        actionButtons.add(createSecondaryButton("Delete Song", _ -> deleteSelectedSong()));

        statusBar.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusBar.setBorder(new EmptyBorder(0, 60, 0, 0));

        darkModeToggle = createThemeToggle();
        darkModeToggle.setBorder(new CompoundBorder(new EmptyBorder(5, 0, 5, 10), darkModeToggle.getBorder()));

        footer.add(actionButtons, BorderLayout.WEST);
        footer.add(statusBar, BorderLayout.CENTER);
        footer.add(darkModeToggle, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        setupSongListContextMenu();
        setUpArtistContextMenu();
    }

    private void updateUIColors() {
        SwingUtilities.updateComponentTreeUI(this);

        sidebar.setBackground(AppTheme.sidebar());
        artistList.setBackground(AppTheme.sidebar());

        Color contentBg = AppTheme.isDark() ? UIManager.getColor("Panel.background") : Color.WHITE;
        mainContent.setBackground(contentBg);
        songHeader.setBackground(contentBg);
        footer.setBackground(contentBg);

        statusBar.setForeground(AppTheme.isDark() ? Color.LIGHT_GRAY : Color.BLACK);

        int selectedIndex = tabbedPane.getSelectedIndex();
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component c = tabbedPane.getTabComponentAt(i);

            if (c instanceof JLabel lbl) {
                boolean isSelected = (i == selectedIndex);

                if (isSelected) {

                    lbl.setForeground(AppTheme.isDark() ? new Color(100, 180, 255) : new Color(16, 148, 255));
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                } else {

                    lbl.setForeground(AppTheme.isDark() ? Color.LIGHT_GRAY : new Color(100, 100, 100));
                    lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                }
            }
        }

        for (JButton btn : primaryButtons) {
            btn.setBackground(AppTheme.accent());
        }

        for (JTextField field : styledTextFields) {
            field.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200), 1), new EmptyBorder(5, 12, 5, 12)));

            field.setBackground(AppTheme.isDark() ? new Color(60, 63, 65) : new Color(238, 238, 240));

            field.setForeground(AppTheme.isDark() ? Color.WHITE : Color.BLACK);
            field.setCaretColor(AppTheme.isDark() ? Color.WHITE : Color.BLACK);
        }

        Color toggleBorderColor = Color.GRAY;
        Color toggleTextColor = AppTheme.isDark() ? Color.WHITE : Color.BLACK;

        darkModeToggle.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, 0), BorderFactory.createLineBorder(toggleBorderColor, 1)));
        darkModeToggle.setForeground(toggleTextColor);

        this.repaint();
    }

    private JToggleButton createThemeToggle() {
        JToggleButton toggle = new JToggleButton(AppTheme.isDark() ? "☀" : "\uD83C\uDF19");
        toggle.setSelected(AppTheme.isDark());

        toggle.setPreferredSize(new Dimension(60, 25));

        toggle.putClientProperty("JButton.buttonType", "square");
        toggle.putClientProperty("JButton.arc", 50);
        toggle.setBackground(UIManager.getColor("App.accentColor"));

        toggle.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        toggle.setOpaque(true);
        toggle.setFocusPainted(false);

        toggle.addActionListener(_ -> {
            AppTheme.toggleTheme();
            toggle.setText(AppTheme.isDark() ? "☀" : "\uD83C\uDF19");
            updateUIColors();
        });
        return toggle;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(100, 40));
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));

        styledTextFields.add(field);

        Border normalBorder = BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200), 1), new EmptyBorder(5, 12, 5, 12));

        field.setBorder(normalBorder);

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {

                Border focusBorder = BorderFactory.createCompoundBorder(new LineBorder(AppTheme.accent(), 2), new EmptyBorder(4, 11, 4, 11));
                field.setBorder(focusBorder);

                field.setBackground(AppTheme.isDark() ? new Color(70, 73, 75) : Color.WHITE);
            }

            public void focusLost(FocusEvent e) {
                field.setBorder(normalBorder);
                field.setBackground(AppTheme.isDark() ? new Color(60, 63, 65) : new Color(238, 238, 240));
            }
        });

        return field;
    }

    private JButton createPrimaryButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);

        btn.putClientProperty("JButton.buttonType", "square");
        btn.putClientProperty("JButton.arc", 0);

        btn.setPreferredSize(new Dimension(120, 35));

        btn.setBackground(AppTheme.accent());
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));

        primaryButtons.add(btn);
        return btn;
    }

    private JButton createSecondaryButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);

        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(120, 35));

        btn.putClientProperty("JButton.buttonType", "square");
        btn.putClientProperty("JButton.arc", 0);

        btn.setOpaque(true);
        btn.setFocusPainted(false);

        return btn;
    }

    private JButton createSortButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);
        btn.putClientProperty("JButton.buttonType", "toolBarButton");
        btn.putClientProperty("JButton.showBorder", true);
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        btn.setPreferredSize(new Dimension(60, 25));
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));

        return btn;
    }

    private DefaultListCellRenderer createArtistRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                Color dividerColor = UIManager.getColor("App.dividerColor");

                label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, dividerColor), BorderFactory.createEmptyBorder(5, 15, 5, 15)));

                if (value instanceof String artist) {
                    label.setText(artist);
                }

                if (isSelected) {
                    label.setBackground(AppTheme.selection());
                    label.setForeground(AppTheme.isDark() ? Color.WHITE : Color.BLACK);
                } else {
                    label.setBackground(AppTheme.sidebar());
                    label.setForeground(AppTheme.isDark() ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                }

                label.setFont(new Font("SansSerif", Font.BOLD, 13));
                label.setOpaque(true);

                return label;
            }
        };
    }


    private DefaultListCellRenderer createModernSongRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel itemPanel = new JPanel(new BorderLayout(15, 0));

                Color normalBg = AppTheme.isDark() ? UIManager.getColor("List.background") : Color.WHITE;
                itemPanel.setBackground(isSelected ? AppTheme.selection() : normalBg);

                itemPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("App.dividerColor")), BorderFactory.createEmptyBorder(5, 15, 5, 15)));

                if (value instanceof Song song) {
                    String titleColor = AppTheme.isDark() ? (isSelected ? "white" : "#dddddd") : "black";
                    String subColor = AppTheme.isDark() ? (isSelected ? "#cccccc" : "gray") : "gray";

                    JLabel titleLabel = new JLabel("<html><b><font color='" + titleColor + "'>" + song.title() + "</font></b><br><font color='" + subColor + "'>" + song.artist() + (song.album().isEmpty() ? "" : " • " + song.album()) + "</font></html>");
                    titleLabel.setFont(MAIN_FONT);

                    boolean isPlaceholder = song == EMPTY_SONG_PLACEHOLDER;

                    boolean isFav = favoritesService.isFavorite(song.id());
                    JLabel rightLabel = new JLabel(isPlaceholder ? "" : (isFav ? "★ " : "") + song.formatTime(song.durationInSeconds()));
                    rightLabel.setFont(new Font("Monospaced", Font.BOLD, 12));

                    Color timeColor = isFav ? new Color(255, 153, 0) : (isSelected ? (AppTheme.isDark() ? Color.WHITE : Color.BLACK) : (AppTheme.isDark() ? Color.LIGHT_GRAY : Color.DARK_GRAY));

                    rightLabel.setForeground(timeColor);

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
            case 0 -> loadArtists(artistList.getSelectedValue());
            case 1 -> {
                if (playlistPanel != null) playlistPanel.loadPlaylists();
            }
            case 2 -> {
                if (favoritesPanel != null) favoritesPanel.loadFavorites();
            }
            case 3 -> {
                if (multiEditPanel != null) multiEditPanel.loadAllSongs();
            }
            case 4 -> {
                if (statsPanel != null) statsPanel.loadStatistics();
            }
        }
    }

    private void addStyledTab(String title, JComponent panel) {
        tabbedPane.addTab(null, panel);
        int index = tabbedPane.getTabCount() - 1;

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Inter", Font.PLAIN, 13));
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(10, 20, 10, 20));

        lbl.setForeground(new Color(100, 100, 100));

        tabbedPane.addChangeListener(_ -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            Color activeColor = AppTheme.isDark() ? new Color(100, 180, 255) : new Color(16, 148, 255);
            Color inactiveColor = AppTheme.isDark() ? Color.LIGHT_GRAY : new Color(100, 100, 100);

            if (selectedIndex == index) {
                lbl.setForeground(activeColor);
                lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
            } else {
                lbl.setForeground(inactiveColor);
                lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
            }
        });

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
            if (!artistModel.isEmpty()) artistList.setSelectedIndex(0);
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
        if (tabbedPane != null && tabbedPane.getSelectedIndex() == 1) s = playlistPanel.getSelectedSong();
        else if (tabbedPane != null && tabbedPane.getSelectedIndex() == 2) s = favoritesPanel.getSelectedFavorite();
        else s = songList.getSelectedValue();

        if (s == null || s == EMPTY_SONG_PLACEHOLDER) {
            JOptionPane.showMessageDialog(this, "Select a song first!");
            return;
        }
        JOptionPane.showMessageDialog(this, "Title: " + s.title() + "\nArtist: " + s.artist() + "\nAlbum: " + s.album() + "\nDuration: " + s.formatTime(s.durationInSeconds()) + " (" + s.durationInSeconds() + "s)", "Song Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStatusBar(List<Song> songs, String artist) {
        int totalSeconds = songs.stream().mapToInt(Song::durationInSeconds).sum();
        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;

        statusBar.setForeground(AppTheme.isDark() ? Color.LIGHT_GRAY : Color.BLACK);
        statusBar.setText("Artist: " + artist + " | Songs: " + songs.size() + " | Time: " + min + ":" + String.format("%02d", sec));
    }

    private void refreshSongList(List<Song> songs) {
        songModel.clear();
        if (songs.isEmpty()) songModel.addElement(EMPTY_SONG_PLACEHOLDER);
        else songs.forEach(songModel::addElement);
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
        int currentTab = tabbedPane.getSelectedIndex();
        Song s = null;

        if (currentTab == 0) {
            s = songList.getSelectedValue();
        } else if (currentTab == 3) {
            s = multiEditPanel.getSelectedSongFromTable();
        } else {
            JOptionPane.showMessageDialog(this, "Editing is only allowed in 'Library' or 'Multi Edit'.", "Action Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (s == null || s == EMPTY_SONG_PLACEHOLDER) {
            JOptionPane.showMessageDialog(this, "Please select a song first!");
            return;
        }

        showSongForm(s);

        if (currentTab == 3) multiEditPanel.loadAllSongs();
    }

    private void deleteSelectedSong() {
        int currentTab = tabbedPane.getSelectedIndex();
        Song s = null;

        if (currentTab == 0) {
            s = songList.getSelectedValue();
        } else if (currentTab == 3) {
            s = multiEditPanel.getSelectedSongFromTable();
        } else {
            JOptionPane.showMessageDialog(this, "Deleting is only allowed in 'Library' or 'Multi Edit'.", "Action Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (s == null || s == EMPTY_SONG_PLACEHOLDER) {
            JOptionPane.showMessageDialog(this, "Please select a song first!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete '" + s.title() + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String currentArtist = s.artist();
            discographyService.deleteSong(s.id());

            if (currentTab == 0) {
                loadArtists(null);
                if (artistModel.contains(currentArtist)) loadSongs(currentArtist);
            } else {
                multiEditPanel.loadAllSongs();
            }

            logger.info("Deleted song {}", s.title());
        }
    }

    public void showSongForm(Song songToEdit) {
        boolean isEdit = (songToEdit != null);

        JTextField titleField = new JTextField(15);
        JTextField albumField = new JTextField(15);
        JTextField artistField = new JTextField(15);
        JTextField durationField = new JTextField(15);

        setCharacterLimit(titleField, 50);
        setCharacterLimit(albumField, 50);
        setCharacterLimit(artistField, 50);
        setCharacterLimit(durationField, 30);

        silenceBackspace(titleField);
        silenceBackspace(albumField);
        silenceBackspace(artistField);
        silenceBackspace(durationField);

        if (isEdit) {
            titleField.setText(songToEdit.title());
            artistField.setText(songToEdit.artist());
            albumField.setText(songToEdit.album());
            durationField.setText(songToEdit.formatTime(songToEdit.durationInSeconds()));
        }

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
        panel.add(new JLabel(" Duration: "));
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
                    String durationInput = durationField.getText().trim();

                    if (title.isEmpty() || artist.isEmpty() || durationInput.isEmpty()) {
                        errorLabel.setText(" Please fill in all necessary fields! (Title, Artist, Duration) ");
                        continue;
                    }

                    int duration = parseDurationToSeconds(durationInput);

                    Song updatedSong;

                    if (isEdit) {
                        updatedSong = new Song(songToEdit.id(), title, album, artist, duration);
                        discographyService.updateSongSafely(updatedSong);
                    } else {
                        updatedSong = new Song(title, album, artist, duration);
                        discographyService.addSongSafely(updatedSong);
                        if (tabbedPane != null) tabbedPane.setSelectedIndex(0);
                    }

                    loadArtists(artist);
                    loadSongs(artist);
                    songList.setSelectedValue(updatedSong, true);
                    logger.info("{} song: {}", isEdit ? "Updated" : "Added", title);
                    valid = true;
                } catch (IllegalArgumentException e) {
                    errorLabel.setText(" Please enter a valid duration format!");
                }
            } else {
                valid = true;
            }
        }
    }

    public void showSongFormOnArtist(String presetArtist) {
        JTextField titleField = new JTextField(15);
        JTextField albumField = new JTextField(15);
        JTextField artistField = new JTextField(15);
        JTextField durationField = new JTextField(15);

        setCharacterLimit(titleField, 50);
        setCharacterLimit(albumField, 50);
        setCharacterLimit(artistField, 50);
        setCharacterLimit(durationField, 30);

        silenceBackspace(titleField);
        silenceBackspace(albumField);
        silenceBackspace(artistField);
        silenceBackspace(durationField);

        artistField.setText(presetArtist);
        artistField.setEditable(false);

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
        panel.add(new JLabel(" Duration: "));
        panel.add(durationField);
        panel.add(errorLabel);

        boolean valid = false;
        while (!valid) {
            int result = JOptionPane.showConfirmDialog(this, panel, "Add New Song to " + presetArtist, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    String title = titleField.getText().trim();
                    String album = albumField.getText().trim();
                    String artist = artistField.getText().trim();
                    String durationInput = durationField.getText().trim();

                    if (title.isEmpty() || durationInput.isEmpty()) {
                        errorLabel.setText(" Please fill in all necessary fields! (Title, Duration) ");
                        continue;
                    }

                    int duration = parseDurationToSeconds(durationInput);

                    Song newSong = new Song(title, album, artist, duration);
                    discographyService.addSongSafely(newSong);

                    if (tabbedPane != null) tabbedPane.setSelectedIndex(0);
                    loadArtists(artist);
                    loadSongs(artist);
                    songList.setSelectedValue(newSong, true);

                    logger.info("Added new song '{}' to artist '{}'", title, artist);
                    valid = true;
                } catch (IllegalArgumentException e) {
                    errorLabel.setText(" Please enter a valid duration format!");
                }
            } else {
                valid = true;
            }
        }
    }

    private int parseDurationToSeconds(String input) {

        if (input.matches("\\d+")) {
            return Integer.parseInt(input);
        }

        Pattern pattern = Pattern.compile("^(?:(\\d{1,2}):)?(\\d{1,2}):(\\d{1,2})$");
        Matcher matcher = pattern.matcher(input);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration format!");
        }

        int hours = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : 0;
        int minutes = Integer.parseInt(matcher.group(2));
        int seconds = Integer.parseInt(matcher.group(3));

        if (minutes >= 60 || seconds >= 60) {
            throw new IllegalArgumentException("Invalid duration format!");
        }

        return hours * 3600 + minutes * 60 + seconds;
    }

    private void setCharacterLimit(JTextField textField, int limit) {
        String existingText = textField.getText();
        textField.setDocument(new PlainDocument() {

            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (str == null) return;
                if ((getLength() + str.length()) <= limit) {
                    super.insertString(offs, str, a);
                } else {
                    int allowed = limit - getLength();
                    if (allowed > 0) {
                        super.insertString(offs, str.substring(0, allowed), a);
                    }
                }
            }

        });
        textField.setText(existingText);
    }

    private void silenceBackspace(JTextField textField) {
        Action originalAction = textField.getActionMap().get("delete-previous");
        textField.getActionMap().put("delete-previous", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!textField.getText().isEmpty()) originalAction.actionPerformed(e);
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

                if (favoritesService.isFavorite(selectedSong.id())) {
                    JMenuItem removeFromFavorites = new JMenuItem("Remove from Favorites");
                    removeFromFavorites.addActionListener(_ -> removeSongFromFavorites(selectedSong));
                    popupMenu.add(removeFromFavorites);
                } else {
                    JMenuItem addToFavorites = new JMenuItem("Add to Favorites");
                    addToFavorites.addActionListener(_ -> addSongToFavorites(selectedSong));
                    popupMenu.add(addToFavorites);
                }
                popupMenu.addSeparator();

                JMenuItem editSong = new JMenuItem("Edit Song");
                editSong.addActionListener(_ -> editSelectedSong());
                popupMenu.add(editSong);

                JMenuItem deleteSong = new JMenuItem("Delete Song");
                deleteSong.addActionListener(_ -> deleteSelectedSong());
                popupMenu.add(deleteSong);
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
                    if (row != -1) songList.setSelectedIndex(row);
                }
            }
        });
    }

    private void setUpArtistContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        artistList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = artistList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        artistList.setSelectedIndex(index);
                    }
                }
            }
        });

        artistList.setComponentPopupMenu(popupMenu);

        popupMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                popupMenu.removeAll();

                String thisArtist = artistList.getSelectedValue();
                if (thisArtist == null || thisArtist.startsWith(" No Result")) return;

                JMenuItem addSongItem = new JMenuItem("Add Song to '" + thisArtist + "'");
                addSongItem.addActionListener(ae -> showSongFormOnArtist(thisArtist));
                popupMenu.add(addSongItem);
                popupMenu.addSeparator();

                JMenuItem editNameItem = new JMenuItem("Edit Artist Name");
                editNameItem.addActionListener(ae -> {

                    String newName = JOptionPane.showInputDialog(
                            SongManagerUI.this,
                            "Rename artist '" + thisArtist + "' to:",
                            thisArtist
                    );

                    if (newName != null && !newName.isBlank() && !newName.equals(thisArtist)) {
                        artistService.renameArtist(thisArtist, newName);
                        loadArtists(newName);
                    }
                });
                popupMenu.add(editNameItem);

                JMenuItem deleteItem = new JMenuItem("Delete Artist");
                deleteItem.addActionListener(ae -> {
                    int confirm = JOptionPane.showConfirmDialog(
                            SongManagerUI.this,
                            "Delete artist '" + thisArtist + "' and all associated songs?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        artistService.deleteArtist(thisArtist);
                        loadArtists(null);
                    }
                });
                popupMenu.add(deleteItem);
            }

            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
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

        int result = JOptionPane.showConfirmDialog(this, panel, "Select Playlist", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Playlist selected = (Playlist) playlistCombo.getSelectedItem();
            if (selected != null) {
                boolean added = playlistService.addSongToPlaylist(selected.id(), song.id());
                if (added) {
                    if (playlistPanel != null) playlistPanel.loadPlaylists();
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
            if (tabbedPane != null && favoritesPanel != null) favoritesPanel.loadFavorites();
        } else {
            statusBar.setForeground(new Color(150, 0, 0));
            statusBar.setText("Song '" + selectedSong.title() + "' is already in Favorites!");
        }
    }

    private void removeSongFromFavorites(Song selectedSong) {
        statusBar.setForeground(new Color(150, 100, 0));
        statusBar.setText("Song '" + selectedSong.title() + "' was removed from Favorites");
        favoritesService.removeFavorite(selectedSong.id());
        if (tabbedPane != null && favoritesPanel != null) favoritesPanel.loadFavorites();
    }

    public void navigateToSong(Song song) {
        tabbedPane.setSelectedIndex(0);

        artistList.setSelectedValue(song.artist(), true);

        SwingUtilities.invokeLater(() -> {
            songList.setSelectedValue(song, true);
            songList.ensureIndexIsVisible(songList.getSelectedIndex());
        });
    }

}