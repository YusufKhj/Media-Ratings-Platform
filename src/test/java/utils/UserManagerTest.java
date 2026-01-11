package utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    @Test
    @DisplayName("Sollte Token zu UserId mapping speichern")
    void testAddToken() {
        // Arrange
        String token = "test-token-" + System.currentTimeMillis();
        int userId = 42;

        // Act
        UserManager.addToken(token, userId);
        int retrievedUserId = UserManager.getUserIdFromToken(token);

        // Assert
        assertEquals(userId, retrievedUserId);
    }

    @Test
    @DisplayName("Sollte -1 zurückgeben für unbekannten Token")
    void testGetUserIdFromToken_Unknown() {
        // Act
        int userId = UserManager.getUserIdFromToken("unknown-token-xyz");

        // Assert
        assertEquals(-1, userId, "Unbekannter Token sollte -1 zurückgeben");
    }

    @Test
    @DisplayName("Sollte Token überschreiben können")
    void testAddToken_Overwrite() {
        // Arrange
        String token = "reused-token-" + System.currentTimeMillis();

        // Act
        UserManager.addToken(token, 100);
        UserManager.addToken(token, 200);

        int userId = UserManager.getUserIdFromToken(token);

        // Assert
        assertEquals(200, userId, "Neuester UserId sollte gespeichert sein");
    }
}