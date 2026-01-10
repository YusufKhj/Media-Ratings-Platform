package server;

import com.sun.net.httpserver.HttpExchange;
import controllers.*;
import utils.JsonResponse;
import utils.TokenManager;

import java.io.IOException;

public class Router {

    private final UserController userController = new UserController();
    private final MediaController mediaController = new MediaController();
    private final RatingController ratingController = new RatingController();
    private final RecommendationController recommendationController = new RecommendationController();

    public void route(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath().trim();
        String method = exchange.getRequestMethod();

        System.out.println("=====================================");
        System.out.println("===> ROUTER HAT EINE ANFRAGE ERHALTEN! <===");
        System.out.println("Method: " + method);
        System.out.println("Path: " + path);
        System.out.println("=====================================");

        if (path.equals("/api/users/register") && method.equals("POST")) {
            userController.handleRegister(exchange);
            return;
        }

        if (path.equals("/api/users/login") && method.equals("POST")) {
            userController.handleLogin(exchange);
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
            mediaController.handleAddFavorite(exchange);
            return;
        }

        // Update Media
        if (path.matches("/api/media/\\d+") && method.equals("PUT")) {
            mediaController.handleUpdate(exchange);
            return;
        }

        // Delete Media
        if (path.matches("/api/media/\\d+") && method.equals("DELETE")) {
            mediaController.handleDelete(exchange);
            return;
        }

        // List Media
        if (path.equals("/api/media") && method.equals("GET")) {
            mediaController.handleList(exchange);
            return;
        }

        // Media Create
        if (path.equals("/api/media") && method.equals("POST")) {
            mediaController.handleCreate(exchange);
            return;
        }

        // List Favorites
        if (path.equals("/api/media/favorites") && method.equals("GET")) {
            mediaController.handleGetFavorites(exchange);
            return;
        }

        // Remove Favorite
        if (path.matches("/api/media/\\d+/favorite") && method.equals("DELETE")) {
            mediaController.handleRemoveFavorite(exchange);
            return;
        }

        // Create Rating
        if (path.matches("/api/media/\\d+/ratings") && method.equals("POST")) {
            ratingController.handleCreate(exchange);
            return;
        }

        // Update Rating
        if (path.matches("/api/ratings/\\d+") && method.equals("PUT")) {
            ratingController.handleUpdate(exchange);
            return;
        }

        // Confirm Comment
        if (path.matches("/api/ratings/\\d+/confirm") && method.equals("POST")) {
            ratingController.handleConfirm(exchange);
            return;
        }

        // Like Rating
        if (path.matches("/api/ratings/\\d+/like") && method.equals("POST")) {
            ratingController.handleLike(exchange);
            return;
        }
        // Delete Rating
        if (path.matches("/api/ratings/\\d+") && method.equals("DELETE")) {
            ratingController.handleDelete(exchange);
            return;
        }

        // List User Rating History
        if (path.equals("/api/users/ratings") && method.equals("GET")) {
            ratingController.handleUserHistory(exchange);
            return;
        }

        // List Ratings for Media
        if (path.matches("/api/media/\\d+/ratings") && method.equals("GET")) {
            ratingController.handleListByMedia(exchange);
            return;
        }

        // Media Recommendations
        if (path.equals("/api/recommendations") && method.equals("GET")) {
            recommendationController.handle(exchange);
            return;
        }

        // Get Average Score for Media
        if (path.matches("/api/media/\\d+/average") && method.equals("GET")) {
            ratingController.handleGetAverageScore(exchange);
            return;
        }

        // 404 fallback
        JsonResponse.sendNotFound(exchange);
    }
}