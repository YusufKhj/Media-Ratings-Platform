package services;

import models.MediaEntry;
import utils.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MediaService {

    // Media erstellen
    public MediaEntry createMedia(MediaEntry media) {
        try (Connection conn = DbUtil.getConnection()) {
            String sql = "INSERT INTO media_entries(title, description, media_type, release_year, genres, age_restriction, creator_id) VALUES(?,?,?,?,?,?,?) RETURNING id";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, media.getTitle());
            ps.setString(2, media.getDescription());
            ps.setString(3, media.getMediaType().name()); // Enum → String
            ps.setInt(4, media.getReleaseYear());
            ps.setArray(5, conn.createArrayOf("text", media.getGenres().toArray()));
            ps.setInt(6, media.getAgeRestriction());
            ps.setInt(7, media.getCreatorId());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                media.setId(rs.getInt("id"));
            }
            return media;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Alle Media abrufen
    public List<MediaEntry> getAllMedia() {
        List<MediaEntry> list = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection()) {
            String sql = "SELECT * FROM media_entries";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MediaEntry media = mapResultSetToMedia(rs);
                list.add(media);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Media nach ID abrufen
    public MediaEntry getMedia(int id) {
        try (Connection conn = DbUtil.getConnection()) {
            String sql = "SELECT * FROM media_entries WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToMedia(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Media aktualisieren
    public MediaEntry updateMedia(int id, MediaEntry media) {
        try (Connection conn = DbUtil.getConnection()) {
            String sql = "UPDATE media_entries SET title=?, description=?, media_type=?, release_year=?, genres=?, age_restriction=? WHERE id=? RETURNING id";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, media.getTitle());
            ps.setString(2, media.getDescription());
            ps.setString(3, media.getMediaType().name()); // Enum → String
            ps.setInt(4, media.getReleaseYear());
            ps.setArray(5, conn.createArrayOf("text", media.getGenres().toArray()));
            ps.setInt(6, media.getAgeRestriction());
            ps.setInt(7, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                media.setId(rs.getInt("id"));
                return media;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Media löschen
    public boolean deleteMedia(int id) {
        try (Connection conn = DbUtil.getConnection()) {
            String sql = "DELETE FROM media_entries WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ----- Hilfsmethode: ResultSet → MediaEntry -----
    private MediaEntry mapResultSetToMedia(ResultSet rs) throws SQLException {
        MediaEntry media = new MediaEntry();
        media.setId(rs.getInt("id"));
        media.setTitle(rs.getString("title"));
        media.setDescription(rs.getString("description"));
        media.setMediaType(MediaEntry.MediaType.valueOf(rs.getString("media_type"))); // String → Enum
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
