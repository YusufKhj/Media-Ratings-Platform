package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.RatingService;
import utils.JsonResponse;
import utils.UserManager;

public class RatingDeleteController {

    private final RatingService ratingService = new RatingService();

    public void handle(HttpExchange exchange) {
        try {
            // Pfad: /api/ratings/{id}
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            int ratingId = Integer.parseInt(parts[parts.length - 1]);

            // Token auslesen
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            int userId = UserManager.getUserIdFromToken(auth.substring(7));

            // Sicherheitscheck
            if (!ratingService.userOwnsRating(ratingId, userId)) {
                JsonResponse.send(exchange, 403, new Object() {
                    public final String error = "Forbidden: You do not own this rating";
                });
                return;
            }

            // Löschen
            boolean deleted = ratingService.deleteRating(ratingId, userId);

            if (deleted) {
                JsonResponse.send(exchange, 200, new Object() {
                    public final String message = "Rating deleted successfully";
                });
            } else {
                JsonResponse.send(exchange, 404, new Object() {
                    public final String error = "Rating not found";
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                JsonResponse.send(exchange, 500, "Internal Server Error");
            } catch (Exception ignored) {}
        }
    }
}