package controllers;

import com.sun.net.httpserver.HttpExchange;
import models.User;
import services.UserService;
import utils.JsonResponse;
import utils.JsonUtil;
import java.io.IOException;

public class UserLoginController {
    private final UserService userService = new UserService();

    public void handle(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());

        User user = JsonUtil.fromJson(requestBody, User.class);

        String token = userService.login(user.getUsername(), user.getPassword());

        if (token != null) {
            var response = new Object() {
                public final String Token = token;
            };
            JsonResponse.send(exchange, 200, response);
        } else {
            var errorResponse = new Object() {
                public final String error = "Invalid username or password";
            };
            JsonResponse.send(exchange, 401, errorResponse);
        }
    }
}