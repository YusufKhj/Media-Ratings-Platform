package utils;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;

public class JsonResponse {

    public static void send(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String response = JsonUtil.toJson(data);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    public static void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
        exchange.close();
    }

    public static void sendNoContent(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }
    public static void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        var errorResponse = new Object() {
            public final String error = message;
        };
        send(exchange, 400, errorResponse);
    }
}

