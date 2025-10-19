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