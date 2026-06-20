package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ExpenseTrackerResponse {
    @SerializedName("total_budget")
    private double totalBudget;

    @SerializedName("total_spent")
    private double totalSpent;

    @SerializedName("budget_string")
    private String budgetString; // e.g. "8 Travelers x 2,500 BDT"

    @SerializedName("contributors")
    private List<Contributor> contributors;

    @SerializedName("error")
    private String error;

    public double getTotalBudget() { return totalBudget; }
    public double getTotalSpent() { return totalSpent; }
    public String getBudgetString() { return budgetString; }
    public List<Contributor> getContributors() { return contributors; }
    public String getError() { return error; }
}
