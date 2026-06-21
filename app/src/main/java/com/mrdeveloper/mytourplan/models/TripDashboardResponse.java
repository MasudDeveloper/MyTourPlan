package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TripDashboardResponse {

    @SerializedName("trip")
    private Trip trip;

    @SerializedName("expenses")
    private List<Expense> expenses;

    @SerializedName("error")
    private String error;

    public Trip getTrip() {
        return trip;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public String getError() {
        return error;
    }
}
