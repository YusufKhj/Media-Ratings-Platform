package controllers;

import com.sun.net.httpserver.HttpExchange;
import models.MediaEntry;
import services.MediaService;
import utils.JsonResponse;
import utils.JsonUtil;
import utils.UserManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MediaController {

    private final MediaService mediaService = new MediaService();

    // ===================== CREATE =====================
    public void handleCreate(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        MediaEntry media = JsonUtil.fromJson(requestBody, MediaEntry.class);

        MediaEntry created = mediaService.createMedia(media);
        JsonResponse.send(exchange, 201, created);
    }

    // ===================== LIST =====================
    public void handleList(HttpExchange exchange) throws IOException {
        try {
            List<?> mediaList = mediaService.getAllMedia();
            JsonResponse.send(exchange, 200, mediaList);
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.send(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    // ===================== DELETE =====================
    public void handleDelete(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            int mediaId = Integer.parseInt(segments[segments.length - 1]);

            String token = exchange.getRequestHeaders()
                    .getFirst("Authorization")
                    .substring(7);

            int userId = UserManager.getUserIdFromToken(token);

            boolean success = mediaService.deleteMedia(mediaId, userId);
            if (success) {
                JsonResponse.send(exchange, 200, "Media deleted successfully");
            } else {
                JsonResponse.send(exchange, 403,
                        "Forbidden: Not the creator or media not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.send(exchange, 500,
                    "Internal Server Error: " + e.getMessage());
        }
    }

    // ===================== UPDATE =====================
    public void handleUpdate(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            int mediaId = Integer.parseInt(segments[segments.length - 1]);

            String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            MediaEntry updateData = JsonUtil.fromJson(body, MediaEntry.class);
            updateData.setId(mediaId);

            String token = exchange.getRequestHeaders()
                    .getFirst("Authorization")
                    .substring(7);

            int userId = UserManager.getUserIdFromToken(token);

            MediaEntry updated = mediaService.updateMedia(updateData, userId);
            if (updated != null) {
                JsonResponse.send(exchange, 200, updated);
            } else {
                JsonResponse.send(exchange, 403,
                        "Forbidden: Not the creator or media not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.send(exchange, 500,
                    "Internal Server Error: " + e.getMessage());
        }
    }

    // ===================== ADD FAVORITE =====================
    public void handleAddFavorite(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            if (segments.length < 5) {
                JsonResponse.sendBadRequest(exchange, "Invalid media ID");
                return;
            }

            int mediaId = Integer.parseInt(segments[segments.length - 2]);

            String authHeader = exchange.getRequestHeaders()
                    .getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                JsonResponse.send(exchange, 401,
                        "Unauthorized - Missing token");
                return;
            }

            String token = authHeader.substring(7);
            int userId = UserManager.getUserIdFromToken(token);

            if (userId == -1) {
                JsonResponse.send(exchange, 401,
                        "Unauthorized - Invalid token");
                return;
            }

            boolean success = mediaService.addFavorite(mediaId, userId);
            if (success) {
                JsonResponse.send(exchange, 200,
                        Map.of("message", "Added to favorites",
                                "mediaIdResponse", mediaId));
                return;
            }

            JsonResponse.sendBadRequest(exchange,
                    "Could not add to favorites");

        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.send(exchange, 500,
                    "Internal Server Error: " + e.getMessage());
        }
    }

    // ===================== REMOVE FAVORITE =====================
    public void handleRemoveFavorite(HttpExchange exchange) throws IOException {
        try {
            if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                JsonResponse.send(exchange, 405, "Method Not Allowed");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            int mediaId = Integer.parseInt(segments[segments.length - 2]);

            String authHeader = exchange.getRequestHeaders()
                    .getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                JsonResponse.send(exchange, 401,
                        "Unauthorized - Missing token");
                return;
            }

            int userId = UserManager.getUserIdFromToken(
                    authHeader.substring(7));

            boolean success =
                    mediaService.removeFavorite(mediaId, userId);

            if (success) {
                JsonResponse.send(exchange, 200,
                        Map.of("message", "Removed from favorites"));
            } else {
                JsonResponse.sendBadRequest(exchange,
                        "Favorite does not exist or was not created by you");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.send(exchange, 500,
                    "Internal Server Error: " + e.getMessage());
        }
    }

    // ===================== GET FAVORITES =====================
    public void handleGetFavorites(HttpExchange exchange) throws IOException {
        try {
            String authHeader = exchange.getRequestHeaders()
                    .getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                JsonResponse.send(exchange, 401,
                        "Unauthorized - Missing token");
                return;
            }

            int userId = UserManager.getUserIdFromToken(
                    authHeader.substring(7));

            List<Map<String, Object>> favorites =
                    mediaService.getFavorites(userId);

            JsonResponse.send(exchange, 200, favorites);

        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.send(exchange, 500,
                    "Internal Server Error: " + e.getMessage());
        }
    }
}