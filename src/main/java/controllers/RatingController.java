package controllers;

import com.sun.net.httpserver.HttpExchange;
import services.RatingService;
import utils.JsonResponse;
import utils.JsonUtil;
import utils.UserManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RatingController {

    private final RatingService ratingService = new RatingService();

    // CREATE
    public void handleCreate(HttpExchange exchange) throws IOException {

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.send(exchange, 405, "Method Not Allowed");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int mediaId = Integer.parseInt(parts[3]);

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        int userId = UserManager.getUserIdFromToken(auth.substring(7));

        Map<String, Object> body =
                JsonUtil.fromJsonToMap(new String(exchange.getRequestBody().readAllBytes()));

        int stars = ((Number) body.get("stars")).intValue();
        String comment = body.get("comment") != null
                ? body.get("comment").toString()
                : null;

        if (stars < 1 || stars > 5) {
            JsonResponse.sendBadRequest(exchange, "Stars must be between 1 and 5");
            return;
        }

        int ratingId = ratingService.createRating(mediaId, userId, stars, comment);

        if (ratingId == -1) {
            JsonResponse.sendBadRequest(exchange, "You already rated this media");
            return;
        }

        if (ratingId < 0) {
            JsonResponse.send(exchange, 500, "Could not create rating");
            return;
        }

        JsonResponse.send(exchange, 201, new Object() {
            public final String message = "Rating created";
            public final int id = ratingId;
        });
    }

    // LIST BY MEDIA
    public void handleListByMedia(HttpExchange exchange) throws IOException {

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.send(exchange, 405, "Method Not Allowed");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int mediaId = Integer.parseInt(parts[3]);

        List<Map<String, Object>> ratings =
                ratingService.getRatingsByMedia(mediaId);

        JsonResponse.send(exchange, 200, new Object() {
            public final List<Map<String, Object>> data = ratings;
            public final int count = ratings.size();
        });
    }

    // UPDATE
    public void handleUpdate(HttpExchange exchange) throws IOException {

        if (!"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.send(exchange, 405, "Method Not Allowed");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int ratingId = Integer.parseInt(parts[3]);

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        int userId = UserManager.getUserIdFromToken(auth.substring(7));

        if (!ratingService.userOwnsRating(ratingId, userId)) {
            JsonResponse.send(exchange, 403, new Object() {
                public final String error =
                        "Forbidden - You can only edit your own ratings";
            });
            return;
        }

        Map<String, Object> body =
                JsonUtil.fromJsonToMap(new String(exchange.getRequestBody().readAllBytes()));

        int stars = ((Number) body.get("stars")).intValue();
        String comment = body.get("comment") != null
                ? body.get("comment").toString()
                : null;

        if (stars < 1 || stars > 5) {
            JsonResponse.sendBadRequest(exchange, "Stars must be between 1 and 5");
            return;
        }

        boolean success =
                ratingService.updateRating(ratingId, userId, stars, comment);

        if (!success) {
            JsonResponse.send(exchange, 500, "Could not update rating");
            return;
        }

        JsonResponse.send(exchange, 200, new Object() {
            public final String message = "Rating updated successfully";
            public final int id = ratingId;
        });
    }

    // DELETE
    public void handleDelete(HttpExchange exchange) {
        try {
            String[] parts = exchange.getRequestURI().getPath().split("/");
            int ratingId = Integer.parseInt(parts[parts.length - 1]);

            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            int userId = UserManager.getUserIdFromToken(auth.substring(7));

            if (!ratingService.userOwnsRating(ratingId, userId)) {
                JsonResponse.send(exchange, 403, new Object() {
                    public final String error =
                            "Forbidden: You do not own this rating";
                });
                return;
            }

            boolean deleted =
                    ratingService.deleteRating(ratingId, userId);

            if (deleted) {
                JsonResponse.send(exchange, 200, new Object() {
                    public final String message =
                            "Rating deleted successfully";
                });
            } else {
                JsonResponse.send(exchange, 404, new Object() {
                    public final String error = "Rating not found";
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                JsonResponse.send(exchange, 500, "Internal Server Error");
            } catch (Exception ignored) {}
        }
    }

    // CONFIRM
    public void handleConfirm(HttpExchange exchange) throws IOException {

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.send(exchange, 405, "Method Not Allowed");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int ratingId = Integer.parseInt(parts[3]);

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        int userId = UserManager.getUserIdFromToken(auth.substring(7));

        if (!ratingService.userOwnsRating(ratingId, userId)) {
            JsonResponse.send(exchange, 403, new Object() {
                public final String error =
                        "Forbidden - You can only confirm your own ratings";
            });
            return;
        }

        boolean success =
                ratingService.confirmComment(ratingId, userId);

        if (!success) {
            JsonResponse.send(exchange, 500, "Could not confirm comment");
            return;
        }

        JsonResponse.send(exchange, 200, new Object() {
            public final String message =
                    "Comment confirmed successfully";
            public final int id = ratingId;
        });
    }

    // LIKE
    public void handleLike(HttpExchange exchange) throws IOException {

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.send(exchange, 405, "Method Not Allowed");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int ratingId = Integer.parseInt(parts[3]);

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        int userId = UserManager.getUserIdFromToken(auth.substring(7));

        boolean success =
                ratingService.likeRating(ratingId, userId);

        if (!success) {
            JsonResponse.sendBadRequest(exchange,
                    "You already liked this rating");
            return;
        }

        int likeCount =
                ratingService.getLikeCount(ratingId);

        JsonResponse.send(exchange, 200, new Object() {
            public final String message =
                    "Rating liked successfully";
            public final int id = ratingId;
            public final int likes = likeCount;
        });
    }

    // USER HISTORY
    public void handleUserHistory(HttpExchange exchange) throws IOException {

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.send(exchange, 405, "Method Not Allowed");
            return;
        }

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        int userId = UserManager.getUserIdFromToken(auth.substring(7));

        List<Map<String, Object>> ratings =
                ratingService.getRatingsByUser(userId);

        JsonResponse.send(exchange, 200, new Object() {
            public final List<Map<String, Object>> data = ratings;
            public final int count = ratings.size();
        });
    }
    // GET AVERAGE SCORE
    public void handleGetAverageScore(HttpExchange exchange) throws IOException {

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.send(exchange, 405, "Method Not Allowed");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int mediaId = Integer.parseInt(parts[3]);

        Map<String, Object> averageData = ratingService.getAverageScore(mediaId);

        if (averageData == null) {
            JsonResponse.send(exchange, 404, new Object() {
                public final String error = "Media does not exist";
            });
            return;
        }

        JsonResponse.send(exchange, 200, averageData);
    }

}