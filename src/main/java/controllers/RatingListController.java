package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.RatingService;
import utils.JsonResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RatingListController {

    private final RatingService ratingService = new RatingService();

    public void handle(HttpExchange exchange) throws IOException {

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.send(exchange, 405, "Method Not Allowed");
            return;
        }

        // Extract media ID from URL: /api/media/{id}/ratings
        String[] parts = exchange.getRequestURI().getPath().split("/");
        int mediaId = Integer.parseInt(parts[3]);

        // Get all ratings for this media
        List<Map<String, Object>> ratings = ratingService.getRatingsByMedia(mediaId);

        JsonResponse.send(exchange, 200, new Object() {
            public final List<Map<String, Object>> data = ratings;
            public final int count = ratings.size();
        });
    }
}