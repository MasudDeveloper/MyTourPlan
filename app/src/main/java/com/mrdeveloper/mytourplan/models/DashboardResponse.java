package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class DashboardResponse {
    @SerializedName("user_name")
    private String userName;

    @SerializedName("upcoming_trip")
    private Trip upcomingTrip;

    @SerializedName("error")
    private String error;

    public String getUserName() { return userName; }
    public Trip getUpcomingTrip() { return upcomingTrip; }
    public String getError() { return error; }
}
