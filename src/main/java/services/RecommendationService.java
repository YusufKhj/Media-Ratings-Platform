package services;

import models.MediaEntry;
import utils.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecommendationService {

    public List<MediaEntry> getRecommendationsForUser(int userId) {
        List<MediaEntry> recommendations = new ArrayList<>();

        // DEBUG: Prüfe zuerst, ob User hochbewertete Medien hat
        String debugSql1 = """
            SELECT m.title, m.genres, r.stars
            FROM ratings r
            JOIN media_entries m ON r.media_id = m.uuid
            WHERE r.user_id = ? AND r.stars >= 4
        """;

        System.out.println("=== DEBUG: Hochbewertete Medien für User " + userId + " ===");
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(debugSql1)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Array genresArray = rs.getArray("genres");
                String[] genres = genresArray != null ? (String[]) genresArray.getArray() : new String[0];
                System.out.println("  - " + rs.getString("title") +
                        " (Sterne: " + rs.getInt("stars") +
                        ", Genres: " + String.join(", ", genres) + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // DEBUG: Prüfe welche Medien noch nicht bewertet wurden
        String debugSql2 = """
            SELECT COUNT(*) as count
            FROM media_entries m
            WHERE m.uuid NOT IN (
                SELECT media_id FROM ratings WHERE user_id = ?
            )
        """;

        System.out.println("=== DEBUG: Anzahl unbewerteter Medien ===");
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(debugSql2)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("  Unbewertet: " + rs.getInt("count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sql = """
            SELECT DISTINCT m.*
            FROM media_entries m
            WHERE m.uuid NOT IN (
                SELECT media_id FROM ratings WHERE user_id = ?
            )
            AND EXISTS (
                SELECT 1
                FROM ratings ra
                JOIN media_entries me ON ra.media_id = me.uuid
                WHERE ra.user_id = ? 
                AND ra.stars >= 4 
                AND m.genres && me.genres
            )
            AND m.media_type IN (
                SELECT DISTINCT me.media_type
                FROM ratings ra
                JOIN media_entries me ON ra.media_id = me.uuid
                WHERE ra.user_id = ?
                AND ra.stars >= 4
            )
            AND m.age_restriction <= (
                SELECT COALESCE(MAX(me.age_restriction), 18)
                FROM ratings ra
                JOIN media_entries me ON ra.media_id = me.uuid
                WHERE ra.user_id = ?
                AND ra.stars >= 4
            )
            ORDER BY m.release_year DESC
            LIMIT 10
        """;

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);

            System.out.println("=== DEBUG: Führe Recommendation Query aus ===");
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                recommendations.add(mapResultSetToMedia(rs));
                count++;
            }
            System.out.println("  Gefundene Recommendations: " + count);

        } catch (Exception e) {
            System.err.println("=== ERROR in Recommendation Query ===");
            e.printStackTrace();
        }

        return recommendations;
    }

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