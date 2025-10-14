package server;

import com.sun.net.httpserver.HttpExchange;
import controllers.UserRegisterController;
import controllers.UserLoginController;
import controllers.MediaCreateController;
import java.io.IOException;

public class Router {

    private final UserRegisterController userRegisterController = new UserRegisterController();
    private final UserLoginController userLoginController = new UserLoginController();
    private final MediaCreateController mediaCreateController = new MediaCreateController();

    public void route(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath().trim();
        String method = exchange.getRequestMethod();

        // User Endpoints
        if (path.equals("/api/users/register") && method.equals("POST")) {
            userRegisterController.handle(exchange);
        } else if (path.equals("/api/users/login") && method.equals("POST")) {
            userLoginController.handle(exchange);

            // Media Endpoints
        } else if (path.equals("/api/media") && method.equals("POST")) {
            mediaCreateController.handle(exchange);

        } else {
            // 404 für alles andere
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        }
    }
}
