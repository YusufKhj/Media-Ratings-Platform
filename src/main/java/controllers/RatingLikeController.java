package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.RatingService;
import utils.JsonResponse;
import utils.UserManager;

import java.io.IOException;

public class RatingLikeController {

    private final RatingService ratingService = new RatingService();

    public void handle(HttpExchange exchange) throws IOException {

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.send(exchange, 405, "Method Not Allowed");
            return;
        }

        // Extract rating ID from URL: /api/ratings/{id}/like
        String[] parts = exchange.getRequestURI().getPath().split("/");
        int ratingId = Integer.parseInt(parts[3]);

        // Get user ID from token
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        int userId = UserManager.getUserIdFromToken(auth.substring(7));

        // Like the rating
        boolean success = ratingService.likeRating(ratingId, userId);

        if (!success) {
            JsonResponse.sendBadRequest(exchange, "You already liked this rating");
            return;
        }

        // Get updated like count
        int likeCount = ratingService.getLikeCount(ratingId);

        JsonResponse.send(exchange, 200, new Object() {
            public final String message = "Rating liked successfully";
            public final int id = ratingId;
            public final int likes = likeCount;
        });
    }
}