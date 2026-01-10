package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.RatingService;
import utils.JsonResponse;
import utils.UserManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UserRatingHistoryController {

    private final RatingService ratingService = new RatingService();

    public void handle(HttpExchange exchange) throws IOException {

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.send(exchange, 405, "Method Not Allowed");
            return;
        }

        // Get user ID from token
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        int userId = UserManager.getUserIdFromToken(auth.substring(7));

        // Get all ratings by this user
        List<Map<String, Object>> ratings = ratingService.getRatingsByUser(userId);

        JsonResponse.send(exchange, 200, new Object() {
            public final List<Map<String, Object>> data = ratings;
            public final int count = ratings.size();
        });
    }
}