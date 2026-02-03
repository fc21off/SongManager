package de.st197974.songmanager.repository;

import de.st197974.songmanager.model.Playlist;
import de.st197974.songmanager.model.Song;

import java.util.List;

public interface PlaylistRepository {

    void createPlaylist(Playlist playlist);

    List<Playlist> findAll();

    void deletePlaylist(String id);

    void addSongToPlaylist(String playlistId, String songId);

    void removeSongFromPlaylist(String playlistId, String songId);

    List<Song> getSongsOfPlaylist(String playlistId);

    void updatePlaylist(Playlist playlist);

    boolean isSongInPlaylist(String playlistId, String songId);

}
