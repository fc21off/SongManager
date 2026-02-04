package de.st197974.songmanager.ui.panels;

import de.st197974.songmanager.model.Playlist;
import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.repository.SongRepository;
import de.st197974.songmanager.service.PlaylistService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class PlaylistPanel extends JPanel {

    private final PlaylistService playlistService;
    private final SongRepository songRepository;

    private final DefaultListModel<Playlist> playlistModel = new DefaultListModel<>();
    private final DefaultListModel<Song> songModel = new DefaultListModel<>();

    private JList<Playlist> playlistList;
    private JList<Song> songList;

    private final Color ACCENT_COLOR = new Color(97, 182, 255);
    private final Color BG_COLOR = new Color(245, 245, 247);
    private final Color SIDEBAR_COLOR = new Color(255, 255, 255);

    public PlaylistPanel(SongRepository songRepository, PlaylistService playlistService) {
        this.songRepository = songRepository;
        this.playlistService = playlistService;

        setBackground(BG_COLOR);
        setLayout(new BorderLayout());
        buildUI();
        loadPlaylists();
    }

    private void buildUI() {
        playlistList = new JList<>(playlistModel);
        playlistList.setFixedCellHeight(30);
        playlistList.setBackground(SIDEBAR_COLOR);
        playlistList.setCellRenderer(createPlaylistRenderer());

        JPanel sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setBackground(SIDEBAR_COLOR);
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JPanel sidebarActions = new JPanel(new GridLayout(1, 3, 5, 5));
        sidebarActions.setOpaque(false);
        sidebarActions.setBorder(new EmptyBorder(5, 5, 10, 5));
        sidebarActions.add(createPrimaryButton("+ New", _ -> createPlaylist()));
        sidebarActions.add(createSecondaryButton("Edit", _ -> editPlaylist()));
        sidebarActions.add(createSecondaryButton("Delete", _ -> deleteSelectedPlaylist()));

        JLabel sidebarTitle = new JLabel(" My Playlists");
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

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel mainHeader = new JPanel(new BorderLayout());
        mainHeader.setBackground(Color.WHITE);
        mainHeader.setBorder(new EmptyBorder(15, 20, 10, 20));

        JLabel mainTitle = new JLabel("Playlist Content");
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
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                        BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
                if (value instanceof Playlist p) {
                    label.setText(p.name());
                }
                label.setBackground(isSelected ? new Color(199, 221, 253) : Color.WHITE);
                label.setForeground(isSelected ? Color.BLACK : Color.DARK_GRAY);
                return label;
            }
        };
    }

    private DefaultListCellRenderer createSongRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel p = new JPanel(new BorderLayout());
                p.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                        new EmptyBorder(5, 20, 5, 20)));
                p.setBackground(isSelected ? new Color(199, 221, 253) : Color.WHITE);

                if (value instanceof Song s) {
                    JLabel title = new JLabel(s.title());
                    title.setFont(new Font("SansSerif", Font.BOLD, 13));
                    JLabel info = new JLabel(s.artist() + " • " + s.formatTime(s.durationInSeconds()));
                    info.setForeground(Color.GRAY);

                    p.add(title, BorderLayout.WEST);
                    p.add(info, BorderLayout.EAST);
                }
                return p;
            }
        };
    }

    private JButton createPrimaryButton(String text, ActionListener listener) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT_COLOR);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.addActionListener(listener);
        return b;
    }

    private JButton createSecondaryButton(String text, ActionListener listener) {
        JButton b = new JButton(text);
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.addActionListener(listener);
        return b;
    }

    private JButton createMiniButton(String text, ActionListener listener) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 10));
        b.setMargin(new Insets(2, 2, 2, 2));
        b.addActionListener(listener);
        return b;
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

        if (selectedPlaylist != null && selectedSong != null) {

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