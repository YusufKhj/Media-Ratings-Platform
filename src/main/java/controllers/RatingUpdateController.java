package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.RatingService;
import utils.JsonResponse;
import utils.JsonUtil;
import utils.UserManager;

import java.io.IOException;
import java.util.Map;

public class RatingUpdateController {

    private final RatingService ratingService = new RatingService();

    public void handle(HttpExchange exchange) throws IOException {

        if (!"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.send(exchange, 405, "Method Not Allowed");
            return;
        }

        // Extract rating ID from URL: /api/ratings/{id}
        String[] parts = exchange.getRequestURI().getPath().split("/");
        int ratingId = Integer.parseInt(parts[3]);

        // Get user ID from token
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        int userId = UserManager.getUserIdFromToken(auth.substring(7));

        // Check if user owns this rating
        if (!ratingService.userOwnsRating(ratingId, userId)) {
            JsonResponse.send(exchange, 403, new Object() {
                public final String error = "Forbidden - You can only edit your own ratings";
            });
            return;
        }

        // Parse request body
        Map<String, Object> body =
                JsonUtil.fromJsonToMap(new String(exchange.getRequestBody().readAllBytes()));

        int stars = ((Number) body.get("stars")).intValue();
        String comment = body.get("comment") != null ? body.get("comment").toString() : null;

        // Validate stars
        if (stars < 1 || stars > 5) {
            JsonResponse.sendBadRequest(exchange, "Stars must be between 1 and 5");
            return;
        }

        // Update rating
        boolean success = ratingService.updateRating(ratingId, userId, stars, comment);

        if (!success) {
            JsonResponse.send(exchange, 500, "Could not update rating");
            return;
        }

        JsonResponse.send(exchange, 200, new Object() {
            public final String message = "Rating updated successfully";
            public final int id = ratingId;
        });
    }
}