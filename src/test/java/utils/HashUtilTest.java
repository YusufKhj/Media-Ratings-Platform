package utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HashUtilTest {

    @Test
    @DisplayName("Sollte identische Hashes für gleiches Passwort generieren")
    void testHashPassword_Consistent() {
        // Arrange
        String password = "mySecurePassword123";

        // Act
        String hash1 = HashUtil.hashPassword(password);
        String hash2 = HashUtil.hashPassword(password);

        // Assert
        assertEquals(hash1, hash2, "Gleiche Passwörter sollten gleichen Hash haben");
    }

    @Test
    @DisplayName("Sollte unterschiedliche Hashes für verschiedene Passwörter generieren")
    void testHashPassword_Different() {
        // Arrange
        String password1 = "password123";
        String password2 = "password456";

        // Act
        String hash1 = HashUtil.hashPassword(password1);
        String hash2 = HashUtil.hashPassword(password2);

        // Assert
        assertNotEquals(hash1, hash2, "Verschiedene Passwörter sollten verschiedene Hashes haben");
    }

    @Test
    @DisplayName("Sollte leeres Passwort hashen können")
    void testHashPassword_EmptyString() {
        // Act
        String hash = HashUtil.hashPassword("");

        // Assert
        assertNotNull(hash);
    }
}