package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SaveTripRequest {
    @SerializedName("from_location")
    private String fromLocation;

    @SerializedName("to_location")
    private String toLocation;

    @SerializedName("days")
    private int days;

    @SerializedName("persons")
    private int persons;

    @SerializedName("budget")
    private double budget;

    @SerializedName("total_cost")
    private double totalCost;

    @SerializedName("costs")
    private TripCosts costs;

    @SerializedName("itinerary")
    private List<ItineraryItem> itinerary;

    public SaveTripRequest(String fromLocation, String toLocation, int days, int persons, double budget, double totalCost, TripCosts costs, List<ItineraryItem> itinerary) {
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.days = days;
        this.persons = persons;
        this.budget = budget;
        this.totalCost = totalCost;
        this.costs = costs;
        this.itinerary = itinerary;
    }
}
