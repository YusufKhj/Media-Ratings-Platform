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
        User user = JsonUtil.fromJson(exchange.getRequestBody(), User.class);
        String token = userService.login(user.getUsername(), user.getPassword());
        if (token != null) {
            JsonResponse.send(exchange, 200, new Object() { public String Token = token; });
        } else {
            JsonResponse.send(exchange, 401, new Object() { public String error = "Login failed"; });
        }
    }
}
