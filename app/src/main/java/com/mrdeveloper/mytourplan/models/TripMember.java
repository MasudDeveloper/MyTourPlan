package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class TripMember {
    @SerializedName("id")
    private String id;

    @SerializedName("trip_id")
    private String tripId;

    @SerializedName("name")
    private String name;

    @SerializedName(value = "amount_paid", alternate = {"paid_amount"})
    private double amountPaid;

    @SerializedName("payment_method")
    private String paymentMethod;

    // Local Sync fields
    private int isSynced;
    private String syncAction;
    private int serverId;

    public TripMember() {}

    public TripMember(String id, String tripId, String name, double amountPaid, String paymentMethod) {
        this.id = id;
        this.tripId = tripId;
        this.name = name;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public int getIsSynced() { return isSynced; }
    public void setIsSynced(int isSynced) { this.isSynced = isSynced; }

    public String getSyncAction() { return syncAction; }
    public void setSyncAction(String syncAction) { this.syncAction = syncAction; }

    public int getServerId() { return serverId; }
    public void setServerId(int serverId) { this.serverId = serverId; }
}
