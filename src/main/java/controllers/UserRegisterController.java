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
        User user = JsonUtil.fromJson(exchange.getRequestBody(), User.class);
        User created = userService.register(user.getUsername(), user.getPassword());
        JsonResponse.send(exchange, 201, created);
    }
}
