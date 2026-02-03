package de.st197974.songmanager.repository;

import de.st197974.songmanager.model.Playlist;
import de.st197974.songmanager.model.Song;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLitePlaylistRepository implements PlaylistRepository{

    private static final Logger logger = LogManager.getLogger(SQLitePlaylistRepository.class);
    private static final String URL = "jdbc:sqlite:songs.db";

    public SQLitePlaylistRepository() {

        try (Connection conn = DriverManager.getConnection(URL)) {
            logger.info("SQLitePlaylistRepository connected to database!");
        } catch (SQLException e) {
            logger.error("Error connecting to SQLite DB", e);
        }

    }

    @Override
    public void createPlaylist(Playlist playlist) {

        String sql = "INSERT INTO playlist(id, name) VALUES (?,?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playlist.id());
            pstmt.setString(2, playlist.name());
            pstmt.executeUpdate();

            logger.info("Created playlist '{}'", playlist.name());

        } catch (SQLException e) {
            logger.error("Error creating playlist '{}'", playlist.name(), e);
        }

    }

    @Override
    public List<Playlist> findAll() {

        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT * FROM playlist";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                playlists.add(new Playlist(
                        rs.getString("id"),
                        rs.getString("name")
                ));
            }

        } catch (SQLException e) {
            logger.error("Error loading playlists", e);
        }

        return playlists;

    }

    @Override
    public void deletePlaylist(String id) {
        String sqlPlaylist = "DELETE FROM playlist WHERE id = ?";
        String sqlPlaylistSongs = "DELETE FROM playlist_song WHERE playlist_id = ?";

        try (Connection conn = DriverManager.getConnection(URL)) {

            try (PreparedStatement pstmt = conn.prepareStatement(sqlPlaylistSongs)) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlPlaylist)) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
            }

            logger.info("Deleted playlist {}", id);

        } catch (SQLException e) {
            logger.error("Error deleting playlist {}", id, e);
        }
    }

    @Override
    public void addSongToPlaylist(String playlistId, String songId) {
        String sql = "INSERT OR IGNORE INTO playlist_song(playlist_id, song_id) VALUES(?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playlistId);
            pstmt.setString(2, songId);
            pstmt.executeUpdate();

            logger.info("Added song {} to playlist {}", songId, playlistId);

        } catch (SQLException e) {
            logger.error("Error adding song {} to playlist {}", songId, playlistId, e);
        }
    }

    @Override
    public void removeSongFromPlaylist(String playlistId, String songId) {
        String sql = "DELETE FROM playlist_song WHERE playlist_id = ? AND song_id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playlistId);
            pstmt.setString(2, songId);
            pstmt.executeUpdate();

            logger.info("Removed song {} from playlist {}", songId, playlistId);

        } catch (SQLException e) {
            logger.error("Error removing song {} from playlist {}", songId, playlistId, e);
        }
    }

    @Override
    public List<Song> getSongsOfPlaylist(String playlistId) {
        List<Song> songs = new ArrayList<>();
        String sql = """
                SELECT s.id, s.title, s.album, s.artist, s.duration
                FROM songs s
                JOIN playlist_song ps ON s.id = ps.song_id
                WHERE ps.playlist_id = ?
                """;

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playlistId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                songs.add(new Song(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("album"),
                        rs.getString("artist"),
                        rs.getInt("duration")
                ));
            }

        } catch (SQLException e) {
            logger.error("Error loading songs for playlist {}", playlistId, e);
        }

        return songs;
    }
}
