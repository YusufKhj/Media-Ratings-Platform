package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.RatingService;
import utils.JsonResponse;
import utils.UserManager;

import java.io.IOException;

public class RatingConfirmController {

    private final RatingService ratingService = new RatingService();

    public void handle(HttpExchange exchange) throws IOException {

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.send(exchange, 405, "Method Not Allowed");
            return;
        }

        // Extract rating ID from URL: /api/ratings/{id}/confirm
        String[] parts = exchange.getRequestURI().getPath().split("/");
        int ratingId = Integer.parseInt(parts[3]);

        // Get user ID from token
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        int userId = UserManager.getUserIdFromToken(auth.substring(7));

        // Check if user owns this rating
        if (!ratingService.userOwnsRating(ratingId, userId)) {
            JsonResponse.send(exchange, 403, new Object() {
                public final String error = "Forbidden - You can only confirm your own ratings";
            });
            return;
        }

        // Confirm the comment
        boolean success = ratingService.confirmComment(ratingId, userId);

        if (!success) {
            JsonResponse.send(exchange, 500, "Could not confirm comment");
            return;
        }

        JsonResponse.send(exchange, 200, new Object() {
            public final String message = "Comment confirmed successfully";
            public final int id = ratingId;
        });
    }
}