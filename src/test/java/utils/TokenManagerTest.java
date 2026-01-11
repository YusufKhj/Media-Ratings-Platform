package utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenManagerTest {
    @Test
    @DisplayName("Sollte Token generieren und validieren können")
    void testGenerateAndValidateToken() {
        // Arrange
        String username = "testuser";

        // Act
        String token = TokenManager.generateToken(username);

        // Assert
        assertNotNull(token);
        assertTrue(TokenManager.isValid(token), "Generierter Token sollte valide sein");
        assertEquals(username, TokenManager.getUsername(token));
    }

    @Test
    @DisplayName("Sollte verschiedene Tokens für gleichen User generieren")
    void testGenerateToken_Unique() {
        // Arrange
        String username = "sameuser";

        // Act
        String token1 = TokenManager.generateToken(username);
        String token2 = TokenManager.generateToken(username);

        // Assert
        assertNotEquals(token1, token2, "Tokens sollten unique sein");
        assertTrue(TokenManager.isValid(token1));
        assertTrue(TokenManager.isValid(token2));
    }

    @Test
    @DisplayName("Sollte invaliden Token erkennen")
    void testIsValid_InvalidToken() {
        // Act & Assert
        assertFalse(TokenManager.isValid("invalid-token-xyz"));
    }

    @Test
    @DisplayName("Sollte Username aus Token abrufen können")
    void testGetUsername() {
        // Arrange
        String username = "alice";
        String token = TokenManager.generateToken(username);

        // Act
        String retrievedUsername = TokenManager.getUsername(token);

        // Assert
        assertEquals(username, retrievedUsername);
    }
}