package models;

import java.time.LocalDateTime;

public class Rating {

    private int id;
    private int userId;
    private int mediaId;
    private int stars;
    private String comment;
    private LocalDateTime timestamp;
    private boolean confirmed;

    // Getter & Setter

    public void setUserId(int userId) { this.userId = userId; }
    public void setMediaId(int mediaId) { this.mediaId = mediaId; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public int getMediaId() { return mediaId; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
}