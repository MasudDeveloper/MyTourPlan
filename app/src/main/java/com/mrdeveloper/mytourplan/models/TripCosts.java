package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class TripCosts {
    @SerializedName("transport")
    private double transport;

    @SerializedName("hotel")
    private double hotel;

    @SerializedName("food")
    private double food;

    @SerializedName("entry_fees")
    private double entryFees;

    @SerializedName("guide")
    private double guide;

    @SerializedName("emergency_buffer")
    private double emergencyBuffer;

    @SerializedName("total_estimated_cost")
    private double totalEstimatedCost;

    @SerializedName("budget_status")
    private String budgetStatus;

    public double getTransport() { return transport; }
    public double getHotel() { return hotel; }
    public double getFood() { return food; }
    public double getEntryFees() { return entryFees; }
    public double getGuide() { return guide; }
    public double getEmergencyBuffer() { return emergencyBuffer; }
    public double getTotalEstimatedCost() { return totalEstimatedCost; }
    public String getBudgetStatus() { return budgetStatus; }
}
