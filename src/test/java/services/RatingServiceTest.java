package services;

import org.junit.jupiter.api.*;
import models.MediaEntry;
import utils.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RatingServiceTest {

    private RatingService ratingService;
    private MediaService mediaService;
    private int testUserId1;
    private int testUserId2;
    private int testMediaId;

    @BeforeEach
    void setUp() throws Exception {
        ratingService = new RatingService();
        mediaService = new MediaService();

        // 2 Test-User erstellen
        try (Connection conn = DbUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users(username, password) VALUES(?, ?) RETURNING uuid")) {
                ps.setString(1, "user1_" + System.currentTimeMillis());
                ps.setString(2, "hash1");
                var rs = ps.executeQuery();
                if (rs.next()) testUserId1 = rs.getInt("uuid");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users(username, password) VALUES(?, ?) RETURNING uuid")) {
                ps.setString(1, "user2_" + System.currentTimeMillis());
                ps.setString(2, "hash2");
                var rs = ps.executeQuery();
                if (rs.next()) testUserId2 = rs.getInt("uuid");
            }
        }

        // Test-Media erstellen
        MediaEntry media = new MediaEntry();
        media.setTitle("Test Movie");
        media.setDescription("For rating tests");
        media.setMediaType(MediaEntry.MediaType.MOVIE);
        media.setReleaseYear(2024);
        media.setGenres(List.of("Action"));
        media.setAgeRestriction(12);
        media.setCreatorId(testUserId1);

        testMediaId = mediaService.createMedia(media).getId();
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection conn = DbUtil.getConnection()) {
            // Cleanup
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM rating_likes WHERE rating_id IN " +
                            "(SELECT uuid FROM ratings WHERE media_id = ?)")) {
                ps.setInt(1, testMediaId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM ratings WHERE media_id = ?")) {
                ps.setInt(1, testMediaId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM user_favorites WHERE media_id = ?")) {
                ps.setInt(1, testMediaId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM media_entries WHERE uuid = ?")) {
                ps.setInt(1, testMediaId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM users WHERE uuid IN (?, ?)")) {
                ps.setInt(1, testUserId1);
                ps.setInt(2, testUserId2);
                ps.executeUpdate();
            }
        }
    }

    // TEST 1
    @Test
    @DisplayName("Sollte verhindern dass User dasselbe Media zweimal bewertet")
    void testCreateRating_DuplicatePrevention() {
        // Arrange - Erstes Rating
        int firstRating = ratingService.createRating(
                testMediaId, testUserId1, 5, "Great"
        );
        assertTrue(firstRating > 0, "Erstes Rating sollte erfolgreich sein");

        // Act - Zweites Rating vom selben User
        int secondRating = ratingService.createRating(
                testMediaId, testUserId1, 3, "Changed my mind"
        );

        // Assert
        assertEquals(-1, secondRating,
                "Duplicate Rating sollte -1 zurückgeben (UNIQUE Constraint)");
    }

    // TEST 2:
    @Test
    @DisplayName("Sollte Update verweigern wenn User nicht Owner ist")
    void testUpdateRating_Authorization() {
        // Arrange - User1 erstellt Rating
        int ratingId = ratingService.createRating(
                testMediaId, testUserId1, 4, "Good"
        );

        // Act - User2 versucht zu updaten
        boolean updated = ratingService.updateRating(
                ratingId, testUserId2, 1, "Hacked"
        );

        // Assert
        assertFalse(updated, "Fremder User darf Rating nicht updaten");
    }

    // TEST 3
    @Test
    @DisplayName("Sollte nur bestätigte Comments in getRatingsByMedia anzeigen")
    void testGetRatingsByMedia_CommentModeration() {
        // Arrange
        int confirmedRating = ratingService.createRating(
                testMediaId, testUserId1, 5, "Confirmed comment"
        );
        ratingService.confirmComment(confirmedRating, testUserId1);

        int unconfirmedRating = ratingService.createRating(
                testMediaId, testUserId2, 4, "Unconfirmed comment"
        );

        // Act
        List<Map<String, Object>> ratings =
                ratingService.getRatingsByMedia(testMediaId);

        // Assert
        assertEquals(2, ratings.size(), "Beide Ratings sollten in Liste sein");

        for (Map<String, Object> rating : ratings) {
            if (rating.get("id").equals(confirmedRating)) {
                assertEquals("Confirmed comment", rating.get("comment"),
                        "Bestätigter Comment sollte sichtbar sein");
            } else {
                assertNull(rating.get("comment"),
                        "Unbestätigter Comment sollte null sein");
            }
        }
    }

    // TEST 4
    @Test
    @DisplayName("Sollte Average Score korrekt berechnen")
    void testGetAverageScore_Calculation() {
        // Arrange - 3 Ratings: 5, 4, 3 = Durchschnitt 4.0
        ratingService.createRating(testMediaId, testUserId1, 5, null);
        ratingService.createRating(testMediaId, testUserId2, 4, null);

        int userId3 = createAdditionalUser();
        ratingService.createRating(testMediaId, userId3, 3, null);

        // Act
        Map<String, Object> result = ratingService.getAverageScore(testMediaId);

        // Assert
        assertNotNull(result);
        assertEquals(4.0, result.get("averageScore"),
                "Average von 5,4,3 sollte 4.0 sein");
        assertEquals(3, result.get("totalRatings"),
                "Sollte 3 Ratings zählen");

        // Cleanup für userId3
        cleanupUser(userId3);
    }

    // TEST 5
    @Test
    @DisplayName("Sollte verhindern dass User Rating zweimal liked")
    void testLikeRating_DuplicatePrevention() {
        // Arrange
        int ratingId = ratingService.createRating(
                testMediaId, testUserId1, 5, "Great"
        );

        boolean firstLike = ratingService.likeRating(ratingId, testUserId2);
        assertTrue(firstLike, "Erstes Like sollte erfolgreich sein");

        // Act - Zweites Like
        boolean secondLike = ratingService.likeRating(ratingId, testUserId2);

        // Assert
        assertFalse(secondLike,
                "Duplicate Like sollte fehlschlagen (UNIQUE Constraint)");

        int likeCount = ratingService.getLikeCount(ratingId);
        assertEquals(1, likeCount,
                "Like-Count sollte trotz doppeltem Versuch nur 1 sein");
    }

    // Helper Methods
    private int createAdditionalUser() {
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users(username, password) VALUES(?, ?) RETURNING uuid")) {
            ps.setString(1, "extra_user_" + System.currentTimeMillis());
            ps.setString(2, "hash");
            var rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("uuid");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void cleanupUser(int userId) {
        try (Connection conn = DbUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM ratings WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM users WHERE uuid = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}