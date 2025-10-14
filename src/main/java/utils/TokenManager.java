package utils;

import java.util.HashMap;
import java.util.Map;

public class TokenManager {
    private static final Map<String, String> tokenUserMap = new HashMap<>();

    public static String generateToken(String username) {
        String token = username + "-mrpToken";
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
