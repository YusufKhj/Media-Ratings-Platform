package services;

import models.MediaEntry;
import org.junit.jupiter.api.*;
import utils.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MediaServiceTest {

    private MediaService mediaService;
    private int testUserId;
    private int testMediaId;
    private static List<Integer> allTestUserIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        mediaService = new MediaService();

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users(username, password) VALUES(?, ?) RETURNING uuid")) {
            ps.setString(1, "testuser_" + System.currentTimeMillis());
            ps.setString(2, "hashedpassword");
            var rs = ps.executeQuery();
            if (rs.next()) {
                testUserId = rs.getInt("uuid");
                allTestUserIds.add(testUserId);
            }
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection conn = DbUtil.getConnection()) {
            // Cleanup
            if (testUserId > 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM rating_likes WHERE rating_id IN " +
                                "(SELECT uuid FROM ratings WHERE user_id = ?)")) {
                    ps.setInt(1, testUserId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM ratings WHERE user_id = ?")) {
                    ps.setInt(1, testUserId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM user_favorites WHERE user_id = ?")) {
                    ps.setInt(1, testUserId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM media_entries WHERE creator_id = ?")) {
                    ps.setInt(1, testUserId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM users WHERE uuid = ?")) {
                    ps.setInt(1, testUserId);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Cleanup Fehler: " + e.getMessage());
        }
    }

    // TEST 1
    @Test
    @DisplayName("Sollte Update verweigern wenn User nicht Creator ist")
    void testUpdateMedia_Authorization() {
        // Arrange - Media erstellen
        MediaEntry media = createMedia("Original Title");
        testMediaId = media.getId();

        // Act - Fremder User versucht zu updaten
        MediaEntry updateData = new MediaEntry();
        updateData.setId(testMediaId);
        updateData.setTitle("Hacked Title");

        MediaEntry result = mediaService.updateMedia(updateData, 99999);

        // Assert
        assertNull(result, "Fremder User darf nicht updaten");
    }

    // TEST 2
    @Test
    @DisplayName("Sollte Löschen verweigern wenn User nicht Creator ist")
    void testDeleteMedia_Authorization() {
        // Arrange
        MediaEntry media = createMedia("Protected Media");
        testMediaId = media.getId();

        // Act
        boolean deleted = mediaService.deleteMedia(testMediaId, 99999);

        // Assert
        assertFalse(deleted, "Fremder User darf nicht löschen");
    }

    // TEST 3
    @Test
    @DisplayName("Sollte nach Genre filtern")
    void testGetAllMedia_GenreFilter() {
        // Arrange
        createMedia("Action Movie", List.of("Action", "Adventure"));
        createMedia("Drama Movie", List.of("Drama"));
        createMedia("Mixed Movie", List.of("Action", "Drama"));

        // Act
        List<Map<String, Object>> result = mediaService.getAllMedia(
                Map.of("genre", "Action")
        );

        // Assert
        assertTrue(result.size() >= 2, "Sollte mindestens 2 Action-Filme finden");
        result.forEach(media -> {
            @SuppressWarnings("unchecked")
            List<String> genres = (List<String>) media.get("genres");
            assertTrue(genres.contains("Action"),
                    "Jedes Ergebnis sollte 'Action' enthalten");
        });
    }

    // TEST 4
    @Test
    @DisplayName("Sollte nach AgeRestriction filtern")
    void testGetAllMedia_AgeRestrictionFilter() {
        // Arrange
        createMedia("Kids", List.of("Animation"), 0);
        createMedia("Teens", List.of("Action"), 12);
        createMedia("Adults", List.of("Horror"), 18);

        // Act - User ist 12 Jahre alt
        List<Map<String, Object>> result = mediaService.getAllMedia(
                Map.of("ageRestriction", "12")
        );

        // Assert
        assertFalse(result.isEmpty());
        result.forEach(media -> {
            int fsk = (int) media.get("ageRestriction");
            assertTrue(fsk <= 12,
                    "FSK " + fsk + " sollte für 12-jährige erlaubt sein");
        });
    }

    // TEST 5
    @Test
    @DisplayName("Sollte mehrere Filter kombinieren können")
    void testGetAllMedia_MultipleFilters() {
        // Arrange
        createMedia("New Action 2024", List.of("Action"), 12, 2024, MediaEntry.MediaType.MOVIE);
        createMedia("Old Action 1990", List.of("Action"), 12, 1990, MediaEntry.MediaType.MOVIE);
        createMedia("New Drama 2024", List.of("Drama"), 12, 2024, MediaEntry.MediaType.MOVIE);
        createMedia("New Action Series", List.of("Action"), 12, 2024, MediaEntry.MediaType.SERIES);

        // Act
        List<Map<String, Object>> result = mediaService.getAllMedia(Map.of(
                "genre", "Action",
                "releaseYear", "2024",
                "mediaType", "MOVIE"
        ));

        // Assert
        assertTrue(result.size() >= 1);
        result.forEach(media -> {
            @SuppressWarnings("unchecked")
            List<String> genres = (List<String>) media.get("genres");
            assertTrue(genres.contains("Action"));
            assertEquals(2024, media.get("releaseYear"));
            assertEquals("MOVIE", media.get("mediaType"));
        });
    }

    // Helper Methods
    private MediaEntry createMedia(String title) {
        return createMedia(title, List.of("Default"), 12, 2024, MediaEntry.MediaType.MOVIE);
    }

    private MediaEntry createMedia(String title, List<String> genres) {
        return createMedia(title, genres, 12, 2024, MediaEntry.MediaType.MOVIE);
    }

    private MediaEntry createMedia(String title, List<String> genres, int fsk) {
        return createMedia(title, genres, fsk, 2024, MediaEntry.MediaType.MOVIE);
    }

    private MediaEntry createMedia(String title, List<String> genres, int fsk,
                                   int year, MediaEntry.MediaType type) {
        MediaEntry media = new MediaEntry();
        media.setTitle(title);
        media.setDescription("Test");
        media.setMediaType(type);
        media.setReleaseYear(year);
        media.setGenres(genres);
        media.setAgeRestriction(fsk);
        media.setCreatorId(testUserId);

        MediaEntry created = mediaService.createMedia(media);
        if (testMediaId == 0) {
            testMediaId = created.getId();
        }
        return created;
    }
}