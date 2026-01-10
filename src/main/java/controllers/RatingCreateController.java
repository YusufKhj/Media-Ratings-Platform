package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.RatingService;
import utils.JsonResponse;
import utils.JsonUtil;
import utils.UserManager;

import java.io.IOException;
import java.util.Map;

public class RatingCreateController {

    private final RatingService ratingService = new RatingService();

    public void handle(HttpExchange exchange) throws IOException {

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.send(exchange, 405, "Method Not Allowed");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int mediaId = Integer.parseInt(parts[3]);

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        int userId = UserManager.getUserIdFromToken(auth.substring(7));

        Map<String, Object> body =
                JsonUtil.fromJsonToMap(new String(exchange.getRequestBody().readAllBytes()));

        int stars = ((Number) body.get("stars")).intValue();
        String comment = body.get("comment") != null ? body.get("comment").toString() : null;

        if (stars < 1 || stars > 5) {
            JsonResponse.sendBadRequest(exchange, "Stars must be between 1 and 5");
            return;
        }

        int ratingId = ratingService.createRating(mediaId, userId, stars, comment);

        if (ratingId == -1) {
            JsonResponse.sendBadRequest(exchange, "You already rated this media");
            return;
        }

        if (ratingId < 0) {
            JsonResponse.send(exchange, 500, "Could not create rating");
            return;
        }

        JsonResponse.send(exchange, 201, new Object() {
            public final String message = "Rating created";
            public final int id = ratingId;
        });
    }
}