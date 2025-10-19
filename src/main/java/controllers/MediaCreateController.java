package controllers;

import com.sun.net.httpserver.HttpExchange;
import models.MediaEntry;
import services.MediaService;
import utils.JsonResponse;
import utils.JsonUtil;
import java.io.IOException;

public class MediaCreateController {
    private final MediaService mediaService = new MediaService();

    public void handle(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        MediaEntry media = JsonUtil.fromJson(requestBody, MediaEntry.class); // <-- Jetzt aus dem String lesen

        MediaEntry created = mediaService.createMedia(media);
        JsonResponse.send(exchange, 201, created);
    }
}
