package models;

import java.time.LocalDateTime;

public class Rating {
    private int id;
    private int userId;
    private int mediaId;
    private int stars; // 1-5
    private String comment;
    private LocalDateTime timestamp;
    private int likes;
    private boolean confirmed;

    public Rating(int userId, int mediaId, int stars, String comment) {
        this.userId = userId;
        this.mediaId = mediaId;
        this.stars = stars;
        this.comment = comment;
        this.timestamp = LocalDateTime.now();
        this.likes = 0;
        this.confirmed = false;
    }

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }

    public int getMediaId() { return mediaId; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public int getLikes() { return likes; }
    public void like() { likes++; }

    public boolean isConfirmed() { return confirmed; }
    public void confirm() { confirmed = true; }
}
