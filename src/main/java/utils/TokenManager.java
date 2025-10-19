package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TokenManager {
    private static final Map<String, String> tokenUserMap = new HashMap<>();

    public static String generateToken(String username) {
        String token = username + "-" + UUID.randomUUID();
        tokenUserMap.put(token, username);
        return token;
    }

    public static String getUsername(String token) {
        return tokenUserMap.get(token);
    }

    public static boolean isValid(String token) {
        return tokenUserMap.containsKey(token);
    }
}
