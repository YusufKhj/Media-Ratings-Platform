package services;

import models.MediaEntry;
import utils.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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