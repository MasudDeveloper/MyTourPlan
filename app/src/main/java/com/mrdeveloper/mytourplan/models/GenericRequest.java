package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class GenericRequest {
    @SerializedName("trip_id")
    private String tripId;

    public GenericRequest(String tripId) {
        this.tripId = tripId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
