package de.st197974.songmanager.repository;

import de.st197974.songmanager.model.Song;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteSongRepository implements SongRepository {

    private static final Logger logger = LogManager.getLogger(SQLiteSongRepository.class);

    private static final String URL = "jdbc:sqlite:songs.db";

    public SQLiteSongRepository() {


        String sql = """
                CREATE TABLE IF NOT EXISTS songs (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    artist TEXT NOT NULL,
                    album TEXT,
                    duration INTEGER
                );
                """;

        try (Connection conn = DriverManager.getConnection(URL);
             Statement statement = conn.createStatement()) {
            statement.execute(sql);
            logger.info("SQLite Repository initialized and table checked!");
        } catch (SQLException e) {
            logger.error("Error while starting SQLite Database", e);
        }

    }

    @Override
    public void save(Song song) {
        String sql = "INSERT OR REPLACE INTO songs(id, title, artist, album, duration) VALUES (?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(URL); PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, song.id());
            preparedStatement.setString(2, song.title());
            preparedStatement.setString(3, song.artist());
            preparedStatement.setString(4, song.album());
            preparedStatement.setInt(5, song.durationInSeconds());

            preparedStatement.executeUpdate();
            logger.info("Song saved into Database: {} ({})", song.title(), song.id());

        } catch (SQLException e) {
            logger.error("Error while saving into SQLite", e);
        }

    }

    @Override
    public List<Song> findAll() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Song song = new Song(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("album"),
                        rs.getString("artist"),
                        rs.getInt("duration")
                );
                songs.add(song);
            }
        } catch (SQLException e) {
            logger.error("Error while Loading from Database", e);
        }
        return songs;
    }

    @Override
    public Song findByID(String id) {
        String sql = "SELECT * FROM songs WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Song(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("album"),
                        rs.getString("artist"),
                        rs.getInt("duration")
                );
            }
        } catch (SQLException e) {
            logger.error("Error while searching for song with ID: {}", id, e);
        }

        return null;
    }

    @Override
    public List<Song> findByArtist(String artist) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs WHERE LOWER(artist) LIKE LOWER(?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + artist + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Song song = new Song(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("album"),
                        rs.getString("artist"),
                        rs.getInt("duration")
                );
                songs.add(song);
            }

        } catch (SQLException e) {
            logger.error("Error while searching songs by artist: {}", artist, e);
        }

        return songs;
    }


    @Override
    public void deleteByID(String id) {
        String sql = "DELETE FROM songs WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            int deletedRows = pstmt.executeUpdate();

            if (deletedRows > 0) {
                logger.info("Song with ID {} deleted successfully!.", id);
            }
        } catch (SQLException e) {
            logger.error("Error while deleting in SQLite", e);
        }
    }
}
