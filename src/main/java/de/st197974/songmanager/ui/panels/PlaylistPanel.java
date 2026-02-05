package de.st197974.songmanager.ui.panels;

import de.st197974.songmanager.model.Playlist;
import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.repository.SongRepository;
import de.st197974.songmanager.service.PlaylistService;
import de.st197974.songmanager.ui.AppTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class PlaylistPanel extends JPanel {

    private final PlaylistService playlistService;
    private final SongRepository songRepository;

    private final DefaultListModel<Playlist> playlistModel = new DefaultListModel<>();
    private final DefaultListModel<Song> songModel = new DefaultListModel<>();

    private JList<Playlist> playlistList;
    private JList<Song> songList;

    private JPanel sidebarPanel;
    private JPanel mainPanel;
    private JPanel mainHeader;
    private JLabel mainTitle;
    private JLabel sidebarTitle;

    private final List<JButton> primaryButtons = new ArrayList<>();

    public PlaylistPanel(SongRepository songRepository, PlaylistService playlistService) {
        this.songRepository = songRepository;
        this.playlistService = playlistService;

        setLayout(new BorderLayout());
        buildUI();
        loadPlaylists();

        updateThemeColors();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (sidebarPanel != null) {
            updateThemeColors();
        }
    }

    private void updateThemeColors() {
        sidebarPanel.setBackground(AppTheme.sidebar());
        playlistList.setBackground(AppTheme.sidebar());

        Color textColor = AppTheme.isDark() ? Color.WHITE : Color.BLACK;
        Color subTextColor = AppTheme.isDark() ? Color.LIGHT_GRAY : Color.DARK_GRAY;

        sidebarTitle.setForeground(textColor);

        Color contentBg = AppTheme.isDark() ? UIManager.getColor("Panel.background") : Color.WHITE;
        mainPanel.setBackground(contentBg);
        mainHeader.setBackground(contentBg);

        mainTitle.setForeground(textColor);

        for (JButton btn : primaryButtons) {
            btn.setBackground(AppTheme.accent());
        }

        repaint();
    }

    private void buildUI() {
        playlistList = new JList<>(playlistModel);
        playlistList.setFixedCellHeight(30);

        playlistList.setCellRenderer(createPlaylistRenderer());

        sidebarPanel = new JPanel(new BorderLayout());

        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JPanel sidebarActions = new JPanel(new GridLayout(1, 3, 5, 5));
        sidebarActions.setOpaque(false);
        sidebarActions.setBorder(new EmptyBorder(5, 5, 10, 5));

        sidebarActions.add(createPrimaryButton("+ New", _ -> createPlaylist()));
        sidebarActions.add(createSecondaryButton("Edit", _ -> editPlaylist()));
        sidebarActions.add(createSecondaryButton("Delete", _ -> deleteSelectedPlaylist()));

        sidebarTitle = new JLabel(" My Playlists");
        sidebarTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        sidebarTitle.setBorder(new EmptyBorder(0, 10, 0, 0));

        JPanel labelWrapper = new JPanel(new BorderLayout());
        labelWrapper.setOpaque(false);
        labelWrapper.setBorder(new EmptyBorder(20, 10, 20, 10));
        labelWrapper.add(sidebarTitle, BorderLayout.CENTER);

        sidebarPanel.add(labelWrapper, BorderLayout.NORTH);
        sidebarPanel.add(new JScrollPane(playlistList), BorderLayout.CENTER);
        sidebarPanel.add(sidebarActions, BorderLayout.SOUTH);

        songList = new JList<>(songModel);
        songList.setFixedCellHeight(45);
        songList.setCellRenderer(createSongRenderer());

        mainPanel = new JPanel(new BorderLayout());

        mainHeader = new JPanel(new BorderLayout());
        mainHeader.setBorder(new EmptyBorder(15, 20, 10, 20));

        mainTitle = new JLabel("No playlist selected...");
        mainTitle.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel mainActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        mainActions.setOpaque(false);
        mainActions.add(createPrimaryButton("Add Song to Playlist", _ -> addSongToPlaylist()));
        mainActions.add(createSecondaryButton("Remove Song", _ -> removeSongFromPlaylist()));

        mainHeader.add(mainTitle, BorderLayout.WEST);
        mainHeader.add(mainActions, BorderLayout.EAST);

        mainPanel.add(mainHeader, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(songList), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, mainPanel);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        playlistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Playlist selected = playlistList.getSelectedValue();
                if (selected != null) {
                    mainTitle.setText("Playlist: " + selected.name());
                    loadSongs(selected);
                }
            }
        });
    }

    private DefaultListCellRenderer createPlaylistRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                Color dividerColor = UIManager.getColor("App.dividerColor");

                label.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, dividerColor), new EmptyBorder(5, 15, 5, 15)));

                if (value instanceof Playlist p) {
                    label.setText(p.name());
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

    private DefaultListCellRenderer createSongRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel p = new JPanel(new BorderLayout());

                Color dividerColor = UIManager.getColor("App.dividerColor");

                Color normalBg = AppTheme.isDark() ? UIManager.getColor("List.background") : Color.WHITE;
                p.setBackground(isSelected ? AppTheme.selection() : normalBg);

                p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, dividerColor), new EmptyBorder(5, 20, 5, 20)));

                if (value instanceof Song s) {
                    JLabel title = new JLabel(s.title());
                    title.setFont(new Font("SansSerif", Font.BOLD, 13));

                    String titleHex = AppTheme.isDark() ? (isSelected ? "ffffff" : "dddddd") : "000000";
                    title.setForeground(AppTheme.isDark() ? (isSelected ? Color.WHITE : new Color(220, 220, 220)) : Color.BLACK);

                    JLabel info = new JLabel(s.artist() + " • " + s.formatTime(s.durationInSeconds()));

                    Color infoColor;
                    if (isSelected) infoColor = AppTheme.isDark() ? Color.LIGHT_GRAY : Color.DARK_GRAY;
                    else infoColor = Color.GRAY;

                    info.setForeground(infoColor);

                    p.add(title, BorderLayout.WEST);
                    p.add(info, BorderLayout.EAST);
                }
                return p;
            }
        };
    }

    private JButton createPrimaryButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);

        btn.putClientProperty("JButton.buttonType", "square");
        btn.putClientProperty("JButton.arc", 0);

        btn.setBackground(AppTheme.accent());
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));

        btn.setPreferredSize(new Dimension(220, 26));

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);

        primaryButtons.add(btn);

        return btn;
    }

    private JButton createSecondaryButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);

        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(160, 26));

        btn.putClientProperty("JButton.buttonType", "square");
        btn.putClientProperty("JButton.focusWidth", 0);
        btn.putClientProperty("JButton.arc", 0);

        btn.setOpaque(true);
        btn.setFocusPainted(false);

        return btn;
    }

    public void loadPlaylists() {
        playlistModel.clear();
        playlistService.getAllPlaylists().forEach(playlistModel::addElement);
        if (!playlistModel.isEmpty() && playlistList.getSelectedIndex() == -1) {
            playlistList.setSelectedIndex(0);
        }
    }

    private void loadSongs(Playlist playlist) {
        songModel.clear();
        if (playlist != null) {
            playlistService.getSongsOfPlaylist(playlist.id()).forEach(songModel::addElement);
        }
    }

    private void createPlaylist() {
        String name = JOptionPane.showInputDialog(this, "New Playlist Name:");
        if (name != null && !name.trim().isEmpty()) {
            Playlist p = new Playlist(name.trim());
            playlistService.addPlaylistSafely(p);
            loadPlaylists();
        }
    }

    private void editPlaylist() {
        Playlist selected = playlistList.getSelectedValue();
        if (selected == null) return;

        String newName = JOptionPane.showInputDialog(this, "Rename '" + selected.name() + "' to:", selected.name());
        if (newName != null && !newName.trim().isEmpty()) {
            playlistService.updatePlaylistSafely(new Playlist(selected.id(), newName.trim()));
            loadPlaylists();
        }
    }

    private void deleteSelectedPlaylist() {
        Playlist selected = playlistList.getSelectedValue();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete playlist '" + selected.name() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                playlistService.deletePlaylist(selected.id());

                songModel.clear();
                mainTitle.setText("No playlist selected...");

                loadPlaylists();
            }
        }
    }

    private void addSongToPlaylist() {
        Playlist selectedPlaylist = playlistList.getSelectedValue();
        if (selectedPlaylist == null) {
            JOptionPane.showMessageDialog(this, "Please select a playlist first!");
            return;
        }

        List<Song> allSongs = songRepository.findAll();
        JTextField searchField = new JTextField();
        DefaultListModel<Song> pickerModel = new DefaultListModel<>();
        allSongs.forEach(pickerModel::addElement);
        JList<Song> pickerList = new JList<>(pickerModel);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            private void filter() {
                String q = searchField.getText().toLowerCase().trim();
                pickerModel.clear();
                allSongs.stream().filter(s -> s.title().toLowerCase().contains(q) || s.artist().toLowerCase().contains(q)).forEach(pickerModel::addElement);
            }
        });

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(searchField, BorderLayout.NORTH);
        panel.add(new JScrollPane(pickerList), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(300, 350));

        if (JOptionPane.showConfirmDialog(this, panel, "Add Song", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Song chosen = pickerList.getSelectedValue();
            if (chosen != null) {
                if (playlistService.addSongToPlaylist(selectedPlaylist.id(), chosen.id())) {
                    loadSongs(selectedPlaylist);
                } else {
                    JOptionPane.showMessageDialog(this, "Song already in playlist.");
                }
            }
        }
    }

    private void removeSongFromPlaylist() {
        Playlist selectedPlaylist = playlistList.getSelectedValue();
        Song selectedSong = songList.getSelectedValue();

        if (selectedSong == null) {
            JOptionPane.showMessageDialog(this, "Select a song first!");
            return;
        }

        if (selectedPlaylist != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Remove '" + selectedSong.title() + "' from '" + selectedPlaylist.name() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                playlistService.removeSongFromPlaylist(selectedPlaylist.id(), selectedSong.id());
                loadSongs(selectedPlaylist);
            }
        }
    }

    public Song getSelectedSong() {
        return songList.getSelectedValue();
    }
}