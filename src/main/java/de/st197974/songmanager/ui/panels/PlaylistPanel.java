package de.st197974.songmanager.ui.panels;

import de.st197974.songmanager.model.Playlist;
import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.repository.SongRepository;
import de.st197974.songmanager.service.PlaylistService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class PlaylistPanel extends JPanel {

    private final PlaylistService playlistService;
    private final SongRepository songRepository;

    private final DefaultListModel<Playlist> playlistModel = new DefaultListModel<>();
    private final DefaultListModel<Song> songModel = new DefaultListModel<>();

    private JList<Playlist> playlistList;
    private JList<Song> songList;

    public PlaylistPanel(SongRepository songRepository, PlaylistService playlistService) {
        this.songRepository = songRepository;
        this.playlistService = playlistService;

        setLayout(new BorderLayout());
        buildUI();
        loadPlaylists();
    }

    private void buildUI() {
        playlistList = new JList<>(playlistModel);
        songList = new JList<>(songModel);

        playlistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Playlist selected = playlistList.getSelectedValue();
                loadSongs(selected);
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(playlistList),
                new JScrollPane(songList));
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);

        JButton addPlaylistBtn = new JButton("New Playlist");
        addPlaylistBtn.addActionListener(e -> createPlaylist());

        JButton editPlaylistBtn = new JButton("Rename Playlist");
        editPlaylistBtn.addActionListener(e -> editPlaylist());

        JButton deletePlaylistBtn = new JButton("Delete Playlist");
        deletePlaylistBtn.addActionListener(e -> deleteSelectedPlaylist());

        JButton addSongBtn = new JButton("Add Song");
        addSongBtn.addActionListener(e -> addSongToPlaylist());

        JButton removeSongBtn = new JButton("Remove Song");
        removeSongBtn.addActionListener(e -> removeSongFromPlaylist());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 5, 5));
        buttonPanel.add(addPlaylistBtn);
        buttonPanel.add(editPlaylistBtn);
        buttonPanel.add(deletePlaylistBtn);
        buttonPanel.add(addSongBtn);
        buttonPanel.add(removeSongBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void loadPlaylists() {
        playlistModel.clear();
        playlistService.getAllPlaylists().forEach(playlistModel::addElement);

        if (!playlistModel.isEmpty()) {
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
        String name = JOptionPane.showInputDialog(this, "Playlist Name:");
        if (name != null && !name.trim().isEmpty()) {
            Playlist p = new Playlist(name.trim());
            playlistService.addPlaylistSafely(p);
            loadPlaylists();
            playlistList.setSelectedValue(p, true);
        }
    }

    private void editPlaylist() {
        Playlist selected = playlistList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a playlist to rename!");
            return;
        }

        String newName = (String) JOptionPane.showInputDialog(this,
                "New Name for '" + selected.name() + "':",
                "Rename Playlist",
                JOptionPane.QUESTION_MESSAGE,
                null, null, selected.name());

        if (newName != null && !newName.trim().isEmpty() && !newName.equals(selected.name())) {
            Playlist updated = new Playlist(selected.id(), newName.trim());
            playlistService.updatePlaylistSafely(updated);
            loadPlaylists();
            playlistList.setSelectedValue(updated, true);
        }
    }

    private void deleteSelectedPlaylist() {
        Playlist selected = playlistList.getSelectedValue();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete '" + selected.name() + "'?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                playlistService.deletePlaylist(selected.id());
                loadPlaylists();
            }
        }
    }

    private void addSongToPlaylist() {
        Playlist selectedPlaylist = playlistList.getSelectedValue();
        if (selectedPlaylist == null) {
            JOptionPane.showMessageDialog(this, "Bitte wähle zuerst eine Playlist aus!");
            return;
        }

        List<Song> allSongs = songRepository.findAll();
        if (allSongs == null || allSongs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keine Songs in der Datenbank gefunden.");
            return;
        }

        JTextField searchField = new JTextField(20);
        DefaultListModel<Song> pickerModel = new DefaultListModel<>();
        allSongs.forEach(pickerModel::addElement);
        JList<Song> pickerList = new JList<>(pickerModel);
        pickerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                String query = searchField.getText().toLowerCase().trim();
                pickerModel.clear();
                allSongs.stream()
                        .filter(s -> s.title().toLowerCase().contains(query) ||
                                s.artist().toLowerCase().contains(query))
                        .forEach(pickerModel::addElement);
            }
        });

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel searchArea = new JPanel(new BorderLayout(5, 5));
        searchArea.add(new JLabel("Suche:"), BorderLayout.WEST);
        searchArea.add(searchField, BorderLayout.CENTER);

        panel.add(searchArea, BorderLayout.NORTH);
        panel.add(new JScrollPane(pickerList), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(300, 400));

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Add Song to Playlist", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Song chosenSong = pickerList.getSelectedValue();
            if (chosenSong != null) {

                boolean added = playlistService.addSongToPlaylist(selectedPlaylist.id(), chosenSong.id());

                if (added) {
                    loadSongs(selectedPlaylist);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Song '" + chosenSong.title() + "' is already in this playlist!",
                            "Duplicate Entry",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    private void removeSongFromPlaylist() {
        Playlist selectedPlaylist = playlistList.getSelectedValue();
        Song selectedSong = songList.getSelectedValue();
        if (selectedPlaylist != null && selectedSong != null) {
            playlistService.removeSongFromPlaylist(selectedPlaylist.id(), selectedSong.id());
            loadSongs(selectedPlaylist);
        }
    }

    public Song getSelectedSong() {
        return songList.getSelectedValue();
    }
}