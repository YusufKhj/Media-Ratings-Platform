package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.MediaService;
import utils.JsonResponse;
import utils.TokenManager;
import utils.UserManager;

import java.io.IOException;

public class MediaDeleteController {

    private final MediaService mediaService = new MediaService();

    public void handle(HttpExchange exchange) throws IOException {
        try {
            // ID aus Pfad extrahieren: /api/media/{id}
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            int mediaId = Integer.parseInt(segments[segments.length - 1]);

            // User aus Token
            String token = exchange.getRequestHeaders().getFirst("Authorization").substring(7);
            int userId = UserManager.getUserIdFromToken(token);

            // Löschen durchführen
            boolean success = mediaService.deleteMedia(mediaId, userId);
            if (success) {
                JsonResponse.send(exchange, 200, "Media deleted successfully");
            } else {
                JsonResponse.send(exchange, 403, "Forbidden: Not the creator or media not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.send(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}