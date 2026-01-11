package services;

import models.MediaEntry;
import utils.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class MediaService {

    // Media erstellen
    public MediaEntry createMedia(MediaEntry media) {
        String sql = "INSERT INTO media_entries(title, description, media_type, release_year, genres, age_restriction, creator_id) VALUES(?,?,?,?,?,?,?) RETURNING uuid";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, media.getTitle());
            ps.setString(2, media.getDescription());
            ps.setString(3, media.getMediaType().name());
            ps.setInt(4, media.getReleaseYear());
            ps.setArray(5, conn.createArrayOf("text", media.getGenres().toArray()));
            ps.setInt(6, media.getAgeRestriction());
            ps.setInt(7, media.getCreatorId());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                media.setId(rs.getInt("uuid"));
            }
            return media;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public MediaEntry updateMedia(MediaEntry updateData, int userId) {
        String selectSql = "SELECT * FROM media_entries WHERE uuid=?";
        String updateSql = "UPDATE media_entries SET title=?, description=?, media_type=?, release_year=?, genres=?, age_restriction=? WHERE uuid=? RETURNING *";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement selectPs = conn.prepareStatement(selectSql)) {

            // Prüfen, ob Media existiert
            selectPs.setInt(1, updateData.getId());
            ResultSet rs = selectPs.executeQuery();
            if (!rs.next()) {
                System.out.println("Media not found");
                return null;
            }

            MediaEntry existing = mapResultSetToMedia(rs);

            // Prüfen, ob der User der Creator ist
            if (existing.getCreatorId() != userId) {
                System.out.println("Forbidden: User is not the creator");
                return null;
            }

            // Update
            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                updatePs.setString(1, updateData.getTitle() != null ? updateData.getTitle() : existing.getTitle());
                updatePs.setString(2, updateData.getDescription() != null ? updateData.getDescription() : existing.getDescription());
                updatePs.setString(3, updateData.getMediaType() != null ? updateData.getMediaType().name() : existing.getMediaType().name());
                updatePs.setInt(4, updateData.getReleaseYear() > 0 ? updateData.getReleaseYear() : existing.getReleaseYear());
                updatePs.setArray(5, updateData.getGenres() != null ? conn.createArrayOf("text", updateData.getGenres().toArray()) : conn.createArrayOf("text", existing.getGenres().toArray()));
                updatePs.setInt(6, updateData.getAgeRestriction() > 0 ? updateData.getAgeRestriction() : existing.getAgeRestriction());
                updatePs.setInt(7, updateData.getId());

                ResultSet updatedRs = updatePs.executeQuery();
                if (updatedRs.next()) {
                    return mapResultSetToMedia(updatedRs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Media löschen
    public boolean deleteMedia(int mediaId, int userId) {
        String selectSql = "SELECT * FROM media_entries WHERE uuid=?";
        String deleteSql = "DELETE FROM media_entries WHERE uuid=?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement selectPs = conn.prepareStatement(selectSql)) {

            // Prüfen, ob Media existiert
            selectPs.setInt(1, mediaId);
            ResultSet rs = selectPs.executeQuery();
            if (!rs.next()) {
                System.out.println("Media not found");
                return false;
            }

            MediaEntry existing = mapResultSetToMedia(rs);

            // Prüfen, ob der User der Creator ist
            if (existing.getCreatorId() != userId) {
                System.out.println("Forbidden: User is not the creator");
                return false;
            }

            // Delete ausführen
            try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                deletePs.setInt(1, mediaId);
                int affected = deletePs.executeUpdate();
                return affected > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<Map<String, Object>> getAllMedia(Map<String, String> filters) {
        List<Map<String, Object>> mediaList = new ArrayList<>();

        // SQL dynamisch aufbauen
        StringBuilder sql = new StringBuilder("""
        SELECT 
            m.uuid,
            m.title,
            m.description,
            m.media_type,
            m.release_year,
            m.genres,
            m.age_restriction,
            m.creator_id,
            COALESCE(AVG(r.stars), 0) as average_score,
            COUNT(r.uuid) as rating_count
        FROM media_entries m
        LEFT JOIN ratings r ON m.uuid = r.media_id
        WHERE 1=1
    """);

        List<Object> params = new ArrayList<>();

        // Title Filter (Teilstring-Suche, case-insensitive)
        if (filters.containsKey("title")) {
            sql.append(" AND LOWER(m.title) LIKE LOWER(?)");
            params.add("%" + filters.get("title") + "%");
        }

        // Genre Filter
        if (filters.containsKey("genre")) {
            sql.append(" AND ? = ANY(m.genres)");
            params.add(filters.get("genre"));
        }

        // Media Type Filter
        if (filters.containsKey("mediaType")) {
            sql.append(" AND m.media_type = ?");
            params.add(filters.get("mediaType"));
        }

        // Release Year Filter
        if (filters.containsKey("releaseYear")) {
            sql.append(" AND m.release_year = ?");
            params.add(Integer.parseInt(filters.get("releaseYear")));
        }

        // Age Restriction Filter
        if (filters.containsKey("ageRestriction")) {
            sql.append(" AND m.age_restriction <= ?");
            params.add(Integer.parseInt(filters.get("ageRestriction")));
        }

        // GROUP BY für Aggregation
        sql.append(" GROUP BY m.uuid, m.title, m.description, m.media_type, m.release_year, m.genres, m.age_restriction, m.creator_id");

        // Minimum Rating Filter
        if (filters.containsKey("minRating")) {
            sql.append(" HAVING AVG(r.stars) >= ?");
            params.add(Double.parseDouble(filters.get("minRating")));
        }

        // Sortierung
        String sortBy = filters.getOrDefault("sortBy", "title");
        String sortOrder = filters.getOrDefault("sortOrder", "asc").toUpperCase();

        switch (sortBy) {
            case "title":
                sql.append(" ORDER BY m.title ").append(sortOrder);
                break;
            case "releaseYear":
                sql.append(" ORDER BY m.release_year ").append(sortOrder);
                break;
            case "averageScore":
                sql.append(" ORDER BY average_score ").append(sortOrder);
                break;
            default:
                sql.append(" ORDER BY m.title ASC");
        }

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // Parameter setzen
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof Integer) {
                    ps.setInt(i + 1, (Integer) param);
                } else if (param instanceof Double) {
                    ps.setDouble(i + 1, (Double) param);
                } else {
                    ps.setString(i + 1, param.toString());
                }
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> media = new LinkedHashMap<>();  // LinkedHashMap statt HashMap!

                media.put("id", rs.getInt("uuid"));
                media.put("title", rs.getString("title"));
                media.put("description", rs.getString("description"));
                media.put("mediaType", rs.getString("media_type"));
                media.put("releaseYear", rs.getInt("release_year"));

                Array genresArray = rs.getArray("genres");
                if (genresArray != null) {
                    media.put("genres", List.of((String[]) genresArray.getArray()));
                } else {
                    media.put("genres", new ArrayList<>());
                }

                media.put("ageRestriction", rs.getInt("age_restriction"));
                media.put("creatorId", rs.getInt("creator_id"));

                int ratingCount = rs.getInt("rating_count");
                if (ratingCount > 0) {
                    double avgScore = rs.getDouble("average_score");
                    media.put("averageScore", Math.round(avgScore * 10.0) / 10.0);
                } else {
                    media.put("averageScore", 0.0);
                }
                media.put("totalRatings", ratingCount);

                mediaList.add(media);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mediaList;
    }

    public boolean addFavorite(int mediaId, int userId) {
        // Erst prüfen ob Media existiert
        String checkMediaSql = "SELECT uuid FROM media_entries WHERE uuid = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkMediaSql)) {
            checkPs.setInt(1, mediaId);
            ResultSet rs = checkPs.executeQuery();
            if (!rs.next()) {
                System.err.println("ERROR: Media with ID " + mediaId + " does not exist!");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("ERROR checking if media exists:");
            e.printStackTrace();
            return false;
        }

        // Dann prüfen ob User existiert
        String checkUserSql = "SELECT uuid FROM users WHERE uuid = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkUserSql)) {
            checkPs.setInt(1, userId);
            ResultSet rs = checkPs.executeQuery();
            if (!rs.next()) {
                System.err.println("ERROR: User with ID " + userId + " does not exist!");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("ERROR checking if user exists:");
            e.printStackTrace();
            return false;
        }

        // Jetzt Favorite hinzufügen
        String sql = "INSERT INTO user_favorites (user_id, media_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, mediaId);
            int affected = ps.executeUpdate();

            if (affected == 0) {
                System.out.println("INFO: Favorite already exists for userId=" + userId + ", mediaId=" + mediaId);
                return true;
            }

            System.out.println("SUCCESS: Added favorite - userId=" + userId + ", mediaId=" + mediaId);
            return true;
        } catch (SQLException e) {
            System.err.println("ERROR inserting favorite:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> getFavorites(int userId) {
        List<Map<String, Object>> favorites = new ArrayList<>();
        String sql = "SELECT m.uuid, m.title, m.description, uf.created_at " +
                     "FROM media_entries m " +
                     "JOIN user_favorites uf ON m.uuid = uf.media_id " +
                     "WHERE uf.user_id = ? " +
                     "ORDER BY uf.created_at DESC"; // optional: neueste zuerst

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                favorites.add(Map.of(
                    "id", rs.getInt("uuid"),
                    "title", rs.getString("title"),
                    "description", rs.getString("description"),
                    "addedAt", rs.getTimestamp("created_at").toString()
                ));
            }

        } catch (Exception e) {
            System.err.println("ERROR fetching favorites:");
            e.printStackTrace();
        }

        return favorites;
    }

    public boolean removeFavorite(int mediaId, int userId) {
        String sql = "DELETE FROM user_favorites WHERE user_id = ? AND media_id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, mediaId);
            int affected = ps.executeUpdate();

            if (affected == 0) {
                System.out.println("INFO: Favorite did not exist for userId=" + userId + ", mediaId=" + mediaId);
                return false; // Favorite existierte nicht
            }

            System.out.println("SUCCESS: Removed favorite for userId=" + userId + ", mediaId=" + mediaId);
            return true;

        } catch (Exception e) {
            System.err.println("ERROR removing favorite:");
            e.printStackTrace();
            return false;
        }
    }

    // Hilfsmethode
    private MediaEntry mapResultSetToMedia(ResultSet rs) throws SQLException {
        MediaEntry media = new MediaEntry();
        media.setId(rs.getInt("uuid"));
        media.setTitle(rs.getString("title"));
        media.setDescription(rs.getString("description"));
        media.setMediaType(MediaEntry.MediaType.valueOf(rs.getString("media_type")));
        media.setReleaseYear(rs.getInt("release_year"));
        Array genresArray = rs.getArray("genres");
        if (genresArray != null) {
            media.setGenres(List.of((String[]) genresArray.getArray()));
        }
        media.setAgeRestriction(rs.getInt("age_restriction"));
        media.setCreatorId(rs.getInt("creator_id"));
        return media;
    }
}