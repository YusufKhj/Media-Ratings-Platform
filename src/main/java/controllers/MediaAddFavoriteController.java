package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.MediaService;
import utils.JsonResponse;
import utils.UserManager;

import java.io.IOException;
import java.util.Map;

// POST /api/media/{id}/favorite
public class MediaAddFavoriteController {
    private final MediaService mediaService = new MediaService();

    public void handle(HttpExchange exchange) throws IOException {
        try {
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

            // WICHTIG: Prüfen ob userId gültig ist!
            if (userId == -1) {
                System.err.println("ERROR: Invalid token or user not found! Token: " + token);
                JsonResponse.send(exchange, 401, "Unauthorized - Invalid token");
                return;
            }

            System.out.println("Adding favorite: mediaId=" + mediaId + ", userId=" + userId);

            // Favorite hinzufügen
            boolean success = mediaService.addFavorite(mediaId, userId);
            if (success) {

               var json = Map.of("message",  "Added to favorites", "mediaIdResponse", mediaId);
                JsonResponse.send(exchange, 200, json);
            }


            JsonResponse.sendBadRequest(exchange, "Could not add to favorites");


        } catch (NumberFormatException e) {
            JsonResponse.sendBadRequest(exchange, "Invalid media ID");
        } catch (Exception e) {
            System.err.println("ERROR in MediaAddFavoriteController:");
            e.printStackTrace();
            JsonResponse.send(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}