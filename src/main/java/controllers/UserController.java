package controllers;

import com.sun.net.httpserver.HttpExchange;
import models.User;
import services.UserService;
import utils.JsonResponse;
import utils.JsonUtil;

import java.io.IOException;

public class UserController {

    private final UserService userService = new UserService();

    // LOGIN
    public void handleLogin(HttpExchange exchange) throws IOException {
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

    // REGISTER
    public void handleRegister(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            User user = JsonUtil.fromJson(requestBody, User.class);

            User created = userService.register(
                    user.getUsername(),
                    user.getPassword()
            );

            if (created != null) {
                JsonResponse.send(exchange, 201, created);
            } else {
                var errorResponse = new Object() {
                    public final String error =
                            "Registration failed. Username may already be in use.";
                };
                JsonResponse.send(exchange, 409, errorResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            var errorResponse = new Object() {
                public final String error =
                        "Internal Server Error during registration.";
            };
            JsonResponse.send(exchange, 500, errorResponse);
        }
    }
}