package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class TripNote {
    private int id;
    
    @SerializedName("trip_id")
    private String tripId;
    
    private String title;
    private String content;
    
    @SerializedName("created_at")
    private String createdAt;

    public TripNote() {}

    public TripNote(int id, String tripId, String title, String content, String createdAt) {
        this.id = id;
        this.tripId = tripId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
