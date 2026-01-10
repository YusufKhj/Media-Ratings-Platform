package server;

import com.sun.net.httpserver.HttpExchange;
import controllers.*;
import utils.JsonResponse;
import utils.TokenManager;

import java.io.IOException;

public class Router {

    private final UserRegisterController userRegisterController = new UserRegisterController();
    private final UserLoginController userLoginController = new UserLoginController();
    private final MediaCreateController mediaCreateController = new MediaCreateController();
    private final MediaUpdateController mediaUpdateController = new MediaUpdateController();
    private final MediaDeleteController mediaDeleteController = new MediaDeleteController();
    private final MediaListController mediaListController = new MediaListController();
    private final MediaAddFavoriteController mediaAddFavoriteController = new MediaAddFavoriteController();
    private final MediaGetFavoritesController mediaGetFavoritesController = new MediaGetFavoritesController();
    private final MediaRemoveFavoriteController mediaRemoveFavoriteController = new MediaRemoveFavoriteController();
    private final RatingCreateController ratingCreateController = new RatingCreateController();
    private final RatingUpdateController ratingUpdateController = new RatingUpdateController();
    private final RatingConfirmController ratingConfirmController = new RatingConfirmController();
    private final RatingListController ratingListController = new RatingListController();
    private final RatingLikeController ratingLikeController = new RatingLikeController();
    private final RatingDeleteController ratingDeleteController = new RatingDeleteController();
    private final UserRatingHistoryController userRatingHistoryController = new UserRatingHistoryController();
    private final RecommendationController recommendationController = new RecommendationController();

    public void route(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath().trim();
        String method = exchange.getRequestMethod();

        System.out.println("=====================================");
        System.out.println("===> ROUTER HAT EINE ANFRAGE ERHALTEN! <===");
        System.out.println("Method: " + method);
        System.out.println("Path: " + path);
        System.out.println("=====================================");

        // Public endpoints
        if (path.equals("/api/users/register") && method.equals("POST")) {
            userRegisterController.handle(exchange);
            return;
        }

        if (path.equals("/api/users/login") && method.equals("POST")) {
            userLoginController.handle(exchange);
            return;
        }

        // Auth prüfen
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            JsonResponse.send(exchange, 401, new Object() {
                public final String error = "Unauthorized - Missing token";
            });
            return;
        }

        String token = authHeader.substring(7);
        if (!TokenManager.isValid(token)) {
            JsonResponse.send(exchange, 401, new Object() {
                public final String error = "Unauthorized - Invalid token";
            });
            return;
        }

        // Add Favorite
        if (path.matches("/api/media/\\d+/favorite") && method.equals("POST")) {
            mediaAddFavoriteController.handle(exchange);
            return;
        }

        // Update Media
        if (path.matches("/api/media/\\d+") && method.equals("PUT")) {
            mediaUpdateController.handle(exchange);
            return;
        }

        // Delete Media
        if (path.matches("/api/media/\\d+") && method.equals("DELETE")) {
            mediaDeleteController.handle(exchange);
            return;
        }

        // List Media
        if (path.equals("/api/media") && method.equals("GET")) {
            mediaListController.handle(exchange);
            return;
        }

        // Media Create
        if (path.equals("/api/media") && method.equals("POST")) {
            mediaCreateController.handle(exchange);
            return;
        }

        // List Favorites
        if (path.equals("/api/media/favorites") && method.equals("GET")) {
            mediaGetFavoritesController.handle(exchange);
            return;
        }

        if (path.matches("/api/media/\\d+/favorite") && method.equals("DELETE")) {
            mediaRemoveFavoriteController.handle(exchange);
            return;
        }

        // Create Rating
        if (path.matches("/api/media/\\d+/ratings") && method.equals("POST")) {
            ratingCreateController.handle(exchange);
            return;
        }

        // Update Rating
        if (path.matches("/api/ratings/\\d+") && method.equals("PUT")) {
            ratingUpdateController.handle(exchange);
            return;
        }

        if (path.matches("/api/ratings/\\d+/confirm") && method.equals("POST")) {
            ratingConfirmController.handle(exchange);
            return;
        }

        // Like Rating
        if (path.matches("/api/ratings/\\d+/like") && method.equals("POST")) {
            ratingLikeController.handle(exchange);
            return;
        }
        // Delete Rating
        if (path.matches("/api/ratings/\\d+") && method.equals("DELETE")) {
            ratingDeleteController.handle(exchange);
            return;
        }

        if (path.equals("/api/users/ratings") && method.equals("GET")) {
            userRatingHistoryController.handle(exchange);
            return;
        }

        if (path.matches("/api/media/\\d+/ratings") && method.equals("GET")) {
            ratingListController.handle(exchange);
            return;
        }

        // Recommendations
        if (path.equals("/api/recommendations") && method.equals("GET")) {
            recommendationController.handle(exchange);
            return;
        }
        // 404 fallback
        JsonResponse.sendNotFound(exchange);
    }
}