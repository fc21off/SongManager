package de.st197974.songmanager.ui.panels;

import de.st197974.songmanager.model.Playlist;
import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.repository.PlaylistRepository;
import de.st197974.songmanager.repository.SQLitePlaylistRepository;
import de.st197974.songmanager.repository.SongRepository;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PlaylistPanel extends JPanel {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    private final DefaultListModel<Playlist> playlistModel = new DefaultListModel<>();
    private final DefaultListModel<Song> songModel = new DefaultListModel<>();

    private JList<Playlist> playlistList;
    private JList<Song> songList;

    public PlaylistPanel(SongRepository songRepository) {
        this.songRepository = songRepository;
        this.playlistRepository = new SQLitePlaylistRepository();

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

        JButton addPlaylistBtn = new JButton("Neue Playlist");
        addPlaylistBtn.addActionListener(e -> createPlaylist());

        JButton deletePlaylistBtn = new JButton("Playlist löschen");
        deletePlaylistBtn.addActionListener(e -> deleteSelectedPlaylist());

        JButton addSongBtn = new JButton("Song hinzufügen");
        addSongBtn.addActionListener(e -> addSongToPlaylist());

        JButton removeSongBtn = new JButton("Song entfernen");
        removeSongBtn.addActionListener(e -> removeSongFromPlaylist());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        buttonPanel.add(addPlaylistBtn);
        buttonPanel.add(deletePlaylistBtn);
        buttonPanel.add(addSongBtn);
        buttonPanel.add(removeSongBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadPlaylists() {
        playlistModel.clear();
        playlistRepository.findAll().forEach(playlistModel::addElement);

        if (!playlistModel.isEmpty()) {
            playlistList.setSelectedIndex(0);
        }
    }

    private void loadSongs(Playlist playlist) {
        songModel.clear();
        if (playlist != null) {
            playlistRepository.getSongsOfPlaylist(playlist.id()).forEach(songModel::addElement);
        }
    }

    private void createPlaylist() {
        String name = JOptionPane.showInputDialog(this, "Name der neuen Playlist:");
        if (name != null && !name.trim().isEmpty()) {
            Playlist p = new Playlist(name.trim());
            playlistRepository.createPlaylist(p);
            loadPlaylists();
            playlistList.setSelectedValue(p, true);
        }
    }

    private void deleteSelectedPlaylist() {
        Playlist selected = playlistList.getSelectedValue();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Playlist '" + selected.name() + "' löschen?",
                    "Bestätigen", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                playlistRepository.deletePlaylist(selected.id());
                loadPlaylists();
            }
        }
    }

    private void addSongToPlaylist() {
        Playlist selectedPlaylist = playlistList.getSelectedValue();
        if (selectedPlaylist == null) return;

        List<Song> allSongs = songRepository.findAll();
        Song[] songArray = allSongs.toArray(new Song[0]);

        Song chosenSong = (Song) JOptionPane.showInputDialog(this,
                "Song auswählen:", "Song hinzufügen",
                JOptionPane.PLAIN_MESSAGE, null, songArray, null);

        if (chosenSong != null) {
            playlistRepository.addSongToPlaylist(selectedPlaylist.id(), chosenSong.id());
            loadSongs(selectedPlaylist);
        }
    }

    private void removeSongFromPlaylist() {
        Playlist selectedPlaylist = playlistList.getSelectedValue();
        Song selectedSong = songList.getSelectedValue();
        if (selectedPlaylist != null && selectedSong != null) {
            playlistRepository.removeSongFromPlaylist(selectedPlaylist.id(), selectedSong.id());
            loadSongs(selectedPlaylist);
        }
    }

}
