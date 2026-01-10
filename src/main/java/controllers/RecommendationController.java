package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.RecommendationService;
import utils.JsonResponse;

import java.io.IOException;
import java.util.List;

public class RecommendationController {

    private final RecommendationService recommendationService = new RecommendationService();

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            int userId = Integer.parseInt(query.split("=")[1]);

            List<?> recommendations =
                    recommendationService.getRecommendationsForUser(userId);

            JsonResponse.send(exchange, 200, recommendations);

        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.send(exchange, 400, "Invalid request");
        }
    }
}