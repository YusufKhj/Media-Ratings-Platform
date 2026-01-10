package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.MediaService;
import utils.JsonResponse;
import utils.UserManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MediaGetFavoritesController {
    private final MediaService mediaService = new MediaService();

    // GET /api/media/favorites
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                JsonResponse.send(exchange, 405, "Method Not Allowed");
                return;
            }

            // Token aus Header
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                JsonResponse.send(exchange, 401, "Unauthorized - Missing token");
                return;
            }

            String token = authHeader.substring(7);
            int userId = UserManager.getUserIdFromToken(token);

            if (userId == -1) {
                JsonResponse.send(exchange, 401, "Unauthorized - Invalid token");
                return;
            }

            System.out.println("Fetching favorites for userId=" + userId);

            List<Map<String, Object>> favorites = mediaService.getFavorites(userId);
            JsonResponse.send(exchange, 200, favorites);

        } catch (Exception e) {
            System.err.println("ERROR in MediaGetFavoritesController:");
            e.printStackTrace();
            JsonResponse.send(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}
