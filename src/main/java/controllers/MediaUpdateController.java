package controllers;

import com.sun.net.httpserver.HttpExchange;
import models.MediaEntry;
import services.MediaService;
import utils.JsonResponse;
import utils.TokenManager;
import utils.UserManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MediaUpdateController {

    private final MediaService mediaService = new MediaService();

    public void handle(HttpExchange exchange) throws IOException {
        try {
            // ID aus Pfad extrahieren: /api/media/{id}
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            int mediaId = Integer.parseInt(segments[segments.length - 1]);

            // Body auslesen
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            MediaEntry updateData = utils.JsonUtil.fromJson(body, MediaEntry.class);
            updateData.setId(mediaId); // ID setzen

            // User aus Token
            String token = exchange.getRequestHeaders().getFirst("Authorization").substring(7);
            int userId = UserManager.getUserIdFromToken(token);

            // Update durchführen
            MediaEntry updatedMedia = mediaService.updateMedia(updateData, userId);
            if (updatedMedia != null) {
                JsonResponse.send(exchange, 200, updatedMedia);
            } else {
                JsonResponse.send(exchange, 403, "Forbidden: Not the creator or media not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.send(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}