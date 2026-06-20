package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeneratePlanResponse {
    @SerializedName("costs")
    private TripCosts costs;

    @SerializedName("itinerary")
    private List<ItineraryItem> itinerary;
    
    @SerializedName("error")
    private String error;

    public TripCosts getCosts() { return costs; }
    public List<ItineraryItem> getItinerary() { return itinerary; }
    public String getError() { return error; }
}
