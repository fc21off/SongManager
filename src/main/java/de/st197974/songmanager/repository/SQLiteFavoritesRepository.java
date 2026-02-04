package de.st197974.songmanager.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteFavoritesRepository implements FavoritesRepository {

    private static final Logger logger = LogManager.getLogger(SQLiteFavoritesRepository.class);
    private static final String URL = "jdbc:sqlite:songs.db";

    public SQLiteFavoritesRepository() {

        try (Connection conn = DriverManager.getConnection(URL)) {
            logger.info("SQLiteFavoritesRepository connected to database!");
        } catch (SQLException e) {
            logger.error("Error connecting to SQLite DB", e);
        }

    }

    @Override
    public void addFavorite(String songId) {
        String sql = "INSERT OR IGNORE INTO favorites(song_id) VALUES(?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, songId);
            pstmt.executeUpdate();
            logger.info("Added song {} to favorites", songId);
        } catch (SQLException e) {
            logger.error("Error adding song {} to favorites", songId, e);
        }
    }

    @Override
    public void removeFavorite(String songId) {
        String sql = "DELETE FROM favorites WHERE song_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, songId);
            pstmt.executeUpdate();
            logger.info("Removed song {} from favorites", songId);
        } catch (SQLException e) {
            logger.error("Error removing song {} from favorites", songId, e);
        }
    }

    @Override
    public boolean isFavorite(String songId) {
        String sql = "SELECT 1 FROM favorites WHERE song_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, songId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.error("Error checking if song {} is favorite", songId, e);
            return false;
        }
    }

    @Override
    public List<String> getAllFavoriteIds() {
        List<String> favorites = new ArrayList<>();
        String sql = "SELECT song_id FROM favorites";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                favorites.add(rs.getString("song_id"));
            }

        } catch (SQLException e) {
            logger.error("Error fetching all favorite songs", e);
        }

        return favorites;
    }
}
