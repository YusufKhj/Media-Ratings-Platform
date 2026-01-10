package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.MediaService;
import utils.JsonResponse;
import utils.UserManager;

import java.io.IOException;

public class MediaRemoveFavoriteController {
    private final MediaService mediaService = new MediaService();

    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                JsonResponse.send(exchange, 405, "Method Not Allowed");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            if (segments.length < 5) {
                JsonResponse.sendBadRequest(exchange, "Invalid media ID");
                return;
            }

            int mediaId = Integer.parseInt(segments[segments.length - 2]); // vor "favorite"

            // User aus Token
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

            boolean success = mediaService.removeFavorite(mediaId, userId);
            if (success) {
                JsonResponse.send(exchange, 200, 
                    new Object() { public final String message = "Removed from favorites"; }
                );
            } else {
                JsonResponse.sendBadRequest(exchange, "Favorite does not exist or was not created by you");
}

        } catch (NumberFormatException e) {
            JsonResponse.sendBadRequest(exchange, "Invalid media ID");
        } catch (Exception e) {
            System.err.println("ERROR in MediaRemoveFavoriteController:");
            e.printStackTrace();
            JsonResponse.send(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}