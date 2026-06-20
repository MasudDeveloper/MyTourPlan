package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class Contributor {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("initials")
    private String initials;

    @SerializedName("amount_paid")
    private double amountPaid;

    @SerializedName("total_due")
    private double totalDue;

    @SerializedName("status")
    private String status;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getInitials() { return initials; }
    public double getAmountPaid() { return amountPaid; }
    public double getTotalDue() { return totalDue; }
    public String getStatus() { return status; }
}
