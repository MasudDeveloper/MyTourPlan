package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class Trip {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("from_location")
    private String fromLocation;

    @SerializedName("destination")
    private String destination;

    @SerializedName("image_uri")
    private String imageUri;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("members_count")
    private int membersCount;

    @SerializedName("budget")
    private double budget;

    @SerializedName("status")
    private String status;

    // Local Sync fields
    private int isSynced; // 1 = true, 0 = false
    private String syncAction; // "INSERT", "UPDATE", "DELETE"

    public Trip() {}

    public Trip(String id, String userId, String fromLocation, String destination, String startDate, String endDate, int membersCount, double budget, String status, String imageUri) {
        this.id = id;
        this.userId = userId;
        this.fromLocation = fromLocation;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.membersCount = membersCount;
        this.budget = budget;
        this.status = status;
        this.imageUri = imageUri;
    }

    public String getFromLocation() { return fromLocation; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getMembersCount() { return membersCount; }
    public void setMembersCount(int membersCount) { this.membersCount = membersCount; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getIsSynced() { return isSynced; }
    public void setIsSynced(int isSynced) { this.isSynced = isSynced; }

    public String getSyncAction() { return syncAction; }
    public void setSyncAction(String syncAction) { this.syncAction = syncAction; }
}
