package services;

import utils.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatingService {

    public int createRating(int mediaId, int userId, int stars, String comment) {
        String sql = """
        INSERT INTO ratings (media_id, user_id, stars, comment)
        VALUES (?, ?, ?, ?)
        RETURNING uuid
    """;

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, mediaId);
            ps.setInt(2, userId);
            ps.setInt(3, stars);
            ps.setString(4, comment);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("uuid");
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("unique")) {
                return -1;
            }
            e.printStackTrace();
        }
        return -2;
    }

    public boolean updateRating(int ratingId, int userId, int stars, String comment) {
        String sql = """
        UPDATE ratings
        SET stars = ?, comment = ?, comment_confirmed = FALSE
        WHERE uuid = ? AND user_id = ?
    """;

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stars);
            ps.setString(2, comment);
            ps.setInt(3, ratingId);
            ps.setInt(4, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean confirmComment(int ratingId, int userId) {
        String sql = """
        UPDATE ratings
        SET comment_confirmed = TRUE
        WHERE uuid = ? AND user_id = ?
    """;

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ratingId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean likeRating(int ratingId, int userId) {
        String sql = """
        INSERT INTO rating_likes (rating_id, user_id)
        VALUES (?, ?)
    """;

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ratingId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            return false; // schon geliked
        }
    }

    public int getLikeCount(int ratingId) {
        String sql = "SELECT COUNT(*) FROM rating_likes WHERE rating_id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ratingId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean deleteRating(int ratingId, int userId) {
        String sql =
                "DELETE FROM ratings WHERE uuid = ? AND user_id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ratingId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean userOwnsRating(int ratingId, int userId) {
        String sql = "SELECT COUNT(*) FROM ratings WHERE uuid = ? AND user_id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ratingId);
            ps.setInt(2, userId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<Map<String, Object>> getRatingsByMedia(int mediaId) {
        String sql = """
        SELECT 
            r.uuid, 
            r.user_id, 
            u.username,
            r.stars, 
            r.comment, 
            r.timestamp, 
            r.comment_confirmed,
            (SELECT COUNT(*) FROM rating_likes WHERE rating_id = r.uuid) as like_count
        FROM ratings r
        JOIN users u ON r.user_id = u.uuid
        WHERE r.media_id = ?
        ORDER BY r.timestamp DESC
    """;

        List<Map<String, Object>> ratings = new ArrayList<>();

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, mediaId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> rating = new HashMap<>();
                rating.put("id", rs.getInt("uuid"));
                rating.put("userId", rs.getInt("user_id"));
                rating.put("username", rs.getString("username"));
                rating.put("stars", rs.getInt("stars"));

                // Nur bestätigte Kommentare anzeigen
                boolean confirmed = rs.getBoolean("comment_confirmed");
                String comment = rs.getString("comment");
                rating.put("comment", (confirmed && comment != null) ? comment : null);
                rating.put("commentConfirmed", confirmed);

                rating.put("timestamp", rs.getTimestamp("timestamp").toString());
                rating.put("likeCount", rs.getInt("like_count"));

                ratings.add(rating);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ratings;
    }

    public List<Map<String, Object>> getRatingsByUser(int userId) {
        String sql = """
        SELECT 
            r.uuid, 
            r.media_id,
            m.title as media_title,
            m.media_type,
            r.stars, 
            r.comment, 
            r.timestamp, 
            r.comment_confirmed,
            (SELECT COUNT(*) FROM rating_likes WHERE rating_id = r.uuid) as like_count
        FROM ratings r
        JOIN media_entries m ON r.media_id = m.uuid
        WHERE r.user_id = ?
        ORDER BY r.timestamp DESC
    """;

        List<Map<String, Object>> ratings = new ArrayList<>();

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> rating = new HashMap<>();
                rating.put("id", rs.getInt("uuid"));
                rating.put("mediaId", rs.getInt("media_id"));
                rating.put("mediaTitle", rs.getString("media_title"));
                rating.put("mediaType", rs.getString("media_type"));
                rating.put("stars", rs.getInt("stars"));
                rating.put("comment", rs.getString("comment"));
                rating.put("commentConfirmed", rs.getBoolean("comment_confirmed"));
                rating.put("timestamp", rs.getTimestamp("timestamp").toString());
                rating.put("likeCount", rs.getInt("like_count"));

                ratings.add(rating);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ratings;
    }

    public Map<String, Object> getAverageScore(int mediaId) {
        // Erst prüfen ob Media existiert
        String checkMediaSql = "SELECT uuid, title FROM media_entries WHERE uuid = ?";
        String mediaTitle = null;

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkMediaSql)) {

            checkPs.setInt(1, mediaId);
            ResultSet rs = checkPs.executeQuery();

            if (!rs.next()) {
                // Media existiert nicht
                return null;
            }

            mediaTitle = rs.getString("title");

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        // Jetzt Average Score berechnen
        String sql = """
        SELECT 
            COUNT(*) as total_ratings,
            AVG(stars) as average_score
        FROM ratings
        WHERE media_id = ?
    """;

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, mediaId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int totalRatings = rs.getInt("total_ratings");

                // Wenn keine Ratings vorhanden sind
                if (totalRatings == 0) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("mediaId", mediaId);
                    result.put("mediaTitle", mediaTitle);
                    result.put("message", "Media does not have ratings");
                    result.put("totalRatings", 0);
                    return result;
                }

                double averageScore = rs.getDouble("average_score");

                Map<String, Object> result = new HashMap<>();
                result.put("mediaId", mediaId);
                result.put("mediaTitle", mediaTitle);
                result.put("averageScore", Math.round(averageScore * 10.0) / 10.0);
                result.put("totalRatings", totalRatings);

                return result;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}