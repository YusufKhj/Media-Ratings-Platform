package utils;

import java.util.HashMap;
import java.util.Map;

public class UserManager {

    // Simuliert eine Zuordnung Token -> UserId
    private static final Map<String, Integer> tokenToUserId = new HashMap<>();

    // Token beim Login speichern
    public static void addToken(String token, int userId) {
        tokenToUserId.put(token, userId);
    }

    // UserId aus Token abrufen
    public static int getUserIdFromToken(String token) {
        return tokenToUserId.getOrDefault(token, -1);
    }
}