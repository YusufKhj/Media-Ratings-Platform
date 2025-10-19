package controllers;

import com.sun.net.httpserver.HttpExchange;
import models.User;
import services.UserService;
import utils.JsonResponse;
import utils.JsonUtil;
import java.io.IOException;

public class UserRegisterController {
    private final UserService userService = new UserService();

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            User user = JsonUtil.fromJson(requestBody, User.class);
            User created = userService.register(user.getUsername(), user.getPassword());

            if (created != null) {
                JsonResponse.send(exchange, 201, created);
            } else {
                var errorResponse = new Object() {
                    public final String error = "Registration failed. Username may already be in use.";
                };
                JsonResponse.send(exchange, 409, errorResponse); // 409 Conflict
            }
        } catch (Exception e) {
            e.printStackTrace();
            var errorResponse = new Object() {
                public final String error = "Internal Server Error during registration.";
            };
            JsonResponse.send(exchange, 500, errorResponse);
        }
    }
}
