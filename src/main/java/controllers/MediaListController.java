package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.MediaService;
import utils.JsonResponse;

import java.io.IOException;
import java.util.List;

public class MediaListController {

    private final MediaService mediaService = new MediaService();

    public void handle(HttpExchange exchange) throws IOException {
        try {
            // Liste aller Media-Einträge abrufen
            List<?> mediaList = mediaService.getAllMedia();
            JsonResponse.send(exchange, 200, mediaList);
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.send(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}