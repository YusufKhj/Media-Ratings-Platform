package models;

import java.util.List;

public class MediaEntry {

    // Enum für Medientyp
    public enum MediaType {
        MOVIE, SERIES, GAME
    }

    private int id;
    private String title;
    private String description;
    private MediaType mediaType;
    private int releaseYear;
    private List<String> genres;
    private int ageRestriction;
    private int creatorId;

    public MediaEntry() {
    }

    public MediaEntry(String title, String description, MediaType mediaType, int releaseYear,
                      List<String> genres, int ageRestriction, int creatorId) {
        this.title = title;
        this.description = description;
        this.mediaType = mediaType;
        this.releaseYear = releaseYear;
        this.genres = genres;
        this.ageRestriction = ageRestriction;
        this.creatorId = creatorId;
    }

    public MediaEntry(int id, String title, String description, MediaType mediaType, int releaseYear,
                      List<String> genres, int ageRestriction, int creatorId) {
        this(title, description, mediaType, releaseYear, genres, ageRestriction, creatorId);
        this.id = id;
    }

    // ----- Getter / Setter -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MediaType getMediaType() { return mediaType; }
    public void setMediaType(MediaType mediaType) { this.mediaType = mediaType; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public int getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(int ageRestriction) { this.ageRestriction = ageRestriction; }

    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }
}
