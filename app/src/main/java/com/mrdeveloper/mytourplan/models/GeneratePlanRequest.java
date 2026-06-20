package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class GeneratePlanRequest {
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

    public GeneratePlanRequest(String fromLocation, String toLocation, int days, int persons, double budget) {
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.days = days;
        this.persons = persons;
        this.budget = budget;
    }

    public String getFromLocation() { return fromLocation; }
    public String getToLocation() { return toLocation; }
    public int getDays() { return days; }
    public int getPersons() { return persons; }
    public double getBudget() { return budget; }
}
