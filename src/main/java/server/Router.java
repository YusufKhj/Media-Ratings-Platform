package server;

import com.sun.net.httpserver.HttpExchange;
import controllers.UserRegisterController;
import controllers.UserLoginController;
import controllers.MediaCreateController;
import controllers.MediaUpdateController;
import controllers.MediaDeleteController;
import utils.JsonResponse;
import utils.TokenManager;

import java.io.IOException;

public class Router {

    private final UserRegisterController userRegisterController = new UserRegisterController();
    private final UserLoginController userLoginController = new UserLoginController();
    private final MediaCreateController mediaCreateController = new MediaCreateController();
    private final MediaUpdateController mediaUpdateController = new MediaUpdateController();
    private final MediaDeleteController mediaDeleteController = new MediaDeleteController();

    public void route(HttpExchange exchange) throws IOException {
        System.out.println("=====================================");
        System.out.println("===> ROUTER HAT EINE ANFRAGE ERHALTEN! <===");
        System.out.println("Method: " + exchange.getRequestMethod());
        System.out.println("Path: " + exchange.getRequestURI().getPath());
        System.out.println("=====================================");

        String path = exchange.getRequestURI().getPath().trim();
        String method = exchange.getRequestMethod();

        // Public endpoints (kein Auth erforderlich)
        if (path.equals("/api/users/register") && method.equals("POST")) {
            userRegisterController.handle(exchange);
            return;
        }

        if (path.equals("/api/users/login") && method.equals("POST")) {
            userLoginController.handle(exchange);
            return;
        }

        // Protected endpoints (Auth erforderlich)
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Auth fehlgeschlagen: Kein Authorization Header");
            var errorResponse = new Object() {
                public final String error = "Unauthorized - Missing token";
            };
            JsonResponse.send(exchange, 401, errorResponse);
            return;
        }

        String token = authHeader.substring(7);
        if (!TokenManager.isValid(token)) {
            System.out.println("Auth fehlgeschlagen: Ungültiger Token");
            var errorResponse = new Object() {
                public final String error = "Unauthorized - Invalid token";
            };
            JsonResponse.send(exchange, 401, errorResponse);
            return;
        }

        // Protected routes
        if (path.equals("/api/media") && method.equals("POST")) {
            mediaCreateController.handle(exchange);
            return;
        }

        // Update Media: PUT /api/media/{id}
        if (path.startsWith("/api/media/") && method.equals("PUT")) {
            mediaUpdateController.handle(exchange);
            return;
        }

        // Delete Media: DELETE /api/media/{id}
        if (path.startsWith("/api/media/") && method.equals("DELETE")) {
            mediaDeleteController.handle(exchange);
            return;
        }

        // 404 für alle anderen Pfade
        System.out.println("404: Route nicht gefunden");
        JsonResponse.sendNotFound(exchange);
    }
}