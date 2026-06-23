package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class TripChecklistItem {
    private int id;
    
    @SerializedName("trip_id")
    private String tripId;
    
    private String title;
    
    @SerializedName("is_checked")
    private int isChecked; // 0 for unchecked, 1 for checked

    public TripChecklistItem() {}

    public TripChecklistItem(int id, String tripId, String title, int isChecked) {
        this.id = id;
        this.tripId = tripId;
        this.title = title;
        this.isChecked = isChecked;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getIsChecked() { return isChecked; }
    public void setIsChecked(int isChecked) { this.isChecked = isChecked; }

    public boolean isCheckedBoolean() { return isChecked == 1; }
    public void setCheckedBoolean(boolean checked) { this.isChecked = checked ? 1 : 0; }
}
