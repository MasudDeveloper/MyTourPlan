package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class Expense {
    @SerializedName("id")
    private String id;

    @SerializedName("trip_id")
    private String tripId;

    @SerializedName("category")
    private String category;

    @SerializedName("amount")
    private double amount;

    @SerializedName("note")
    private String note;

    @SerializedName("created_at")
    private String createdAt;

    // Local Sync fields
    private int isSynced;
    private String syncAction;
    private int serverId;

    public Expense() {}

    public Expense(String id, String tripId, String category, double amount, String note, String createdAt) {
        this.id = id;
        this.tripId = tripId;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getIsSynced() { return isSynced; }
    public void setIsSynced(int isSynced) { this.isSynced = isSynced; }

    public String getSyncAction() { return syncAction; }
    public void setSyncAction(String syncAction) { this.syncAction = syncAction; }

    public int getServerId() { return serverId; }
    public void setServerId(int serverId) { this.serverId = serverId; }
}
