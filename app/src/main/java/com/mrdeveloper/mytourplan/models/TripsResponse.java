package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TripsResponse {
    @SerializedName("trips")
    private List<Trip> trips;

    @SerializedName("error")
    private String error;

    public List<Trip> getTrips() { return trips; }
    public String getError() { return error; }
}
