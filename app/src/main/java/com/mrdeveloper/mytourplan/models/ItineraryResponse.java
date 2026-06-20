package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ItineraryResponse {
    @SerializedName("total_budget")
    private String totalBudget; // e.g. "৳ 45,600.00"

    @SerializedName("duration")
    private String duration; // e.g. "4 Days"

    @SerializedName("travelers_count")
    private String travelersCount; // e.g. "2 Travelers"

    @SerializedName("schedule")
    private List<ItineraryItem> schedule;

    @SerializedName("error")
    private String error;

    public String getTotalBudget() { return totalBudget; }
    public String getDuration() { return duration; }
    public String getTravelersCount() { return travelersCount; }
    public List<ItineraryItem> getSchedule() { return schedule; }
    public String getError() { return error; }
}
