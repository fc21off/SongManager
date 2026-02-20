package de.st197974.songmanager.service;

import de.st197974.songmanager.model.Playlist;
import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.repository.PlaylistRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Provides services for managing playlists, including operations to create, update, delete,
 * and retrieve playlists, as well as functionality to manage songs within playlists.
 */
public record PlaylistService(PlaylistRepository repository) {

    private static final Logger logger = LogManager.getLogger(PlaylistService.class);

    public List<Playlist> getAllPlaylists() {
        return repository.findAll();
    }

    public void addPlaylistSafely(Playlist playlist) {
        if (playlist.name() == null || playlist.name().trim().isEmpty()) {
            logger.warn("Aborted Attempt of creating playlist without name!");
            return;
        }

        repository.createPlaylist(playlist);
        logger.info("Playlist '{}' successfully created.", playlist.name());
    }

    public void deletePlaylist(String id) {

        boolean exists = repository.findAll().stream().anyMatch(playlist -> playlist.id().equals(id));

        if (!exists) {
            logger.warn("Deletion unsuccessful: Playlist with ID {} does not exist!", id);
            return;
        }

        logger.info("Playlist with ID {} gets deleted.", id);
        repository.deletePlaylist(id);
    }

    public void updatePlaylistSafely(Playlist updatedPlaylist) {
        if (updatedPlaylist.id() == null || updatedPlaylist.name().trim().isEmpty()) {
            logger.error("Update unsuccessful: Playlist ID or Name missing!");
            return;
        }

        repository.updatePlaylist(updatedPlaylist);
        logger.info("Playlist ID {} updated: New Name '{}'", updatedPlaylist.id(), updatedPlaylist.name());
    }

    public List<Song> getSongsOfPlaylist(String playlistId) {
        if (playlistId == null) return List.of();
        return repository.getSongsOfPlaylist(playlistId);
    }

    public boolean addSongToPlaylist(String playlistId, String songId) {
        if (playlistId == null || songId == null) {
            logger.warn("Error while adding Song, no ID was available!");
            return false;
        }

        if (repository.isSongInPlaylist(playlistId, songId)) {
            logger.info("Song {} is already in Playlist {}!", songId, playlistId);
            return false;
        }

        repository.addSongToPlaylist(playlistId, songId);
        return true;
    }

    public void removeSongFromPlaylist(String playlistId, String songId) {
        if (playlistId == null || songId == null) return;

        repository.removeSongFromPlaylist(playlistId, songId);
        logger.info("Song {} removed from Playlist {}.", songId, playlistId);
    }
}