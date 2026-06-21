package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MembersResponse {

    @SerializedName("budget_per_person")
    private double budgetPerPerson;

    @SerializedName("members")
    private List<TripMember> members;

    @SerializedName("error")
    private String error;

    public double getBudgetPerPerson() {
        return budgetPerPerson;
    }

    public List<TripMember> getMembers() {
        return members;
    }

    public String getError() {
        return error;
    }
}
