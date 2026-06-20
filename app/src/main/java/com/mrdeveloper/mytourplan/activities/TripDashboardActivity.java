package com.mrdeveloper.mytourplan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.database.DatabaseHelper;
import com.mrdeveloper.mytourplan.models.Expense;

import java.util.List;

public class TripDashboardActivity extends AppCompatActivity {

    private TextView tvTotalBudget, tvMembers, tvEmptyState;
    private Button btnAddExpense, btnCalculate, btnViewItinerary;
    private LinearLayout layoutExpenseList;
    private DatabaseHelper db;
    private int tripId;
    private int totalMembers;
    private double budgetPerPerson;
    private double totalExpense = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        tripId = getIntent().getIntExtra("trip_id", -1);

        tvTotalBudget = findViewById(R.id.tvTotalBudget);
        tvMembers = findViewById(R.id.tvMembers);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        layoutExpenseList = findViewById(R.id.layoutExpenseList);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnViewItinerary = findViewById(R.id.btnViewItinerary);

        // Fetch Trip Details (Need to fetch it from DB actually, wait, we don't have getTripById in DatabaseHelper)
        // Let's pass members and budget from AddTrip or just add getTripById.
        // It's cleaner to add getTripById.
        
        btnAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(TripDashboardActivity.this, AddExpenseActivity.class);
            intent.putExtra("trip_id", tripId);
            startActivity(intent);
        });

        btnCalculate.setOnClickListener(v -> {
            Intent intent = new Intent(TripDashboardActivity.this, CalculationActivity.class);
            intent.putExtra("trip_id", tripId);
            startActivity(intent);
        });

        btnViewItinerary.setOnClickListener(v -> {
            Intent intent = new Intent(TripDashboardActivity.this, ItineraryActivity.class);
            intent.putExtra("trip_id", String.valueOf(tripId));
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTripData();
    }

    private void loadTripData() {
        com.mrdeveloper.mytourplan.models.Trip trip = db.getTripById(String.valueOf(tripId));
        if (trip != null) {
            totalMembers = trip.getMembersCount();
            budgetPerPerson = trip.getBudget();
        } else {
            totalMembers = getIntent().getIntExtra("members", 1);
            budgetPerPerson = getIntent().getDoubleExtra("budget", 0.0);
        }

        double totalBudget = totalMembers * budgetPerPerson;
        tvMembers.setText(String.valueOf(totalMembers));
        tvTotalBudget.setText("৳" + String.format("%.2f", totalBudget));

        List<Expense> expenses = db.getExpensesByTrip(String.valueOf(tripId));
        layoutExpenseList.removeAllViews();
        totalExpense = 0;

        if (expenses.isEmpty()) {
            layoutExpenseList.addView(tvEmptyState);
        } else {
            for (Expense expense : expenses) {
                totalExpense += expense.getAmount();
                
                TextView tv = new TextView(this);
                tv.setText(expense.getCategory() + " - ৳" + expense.getAmount() + (expense.getNote().isEmpty() ? "" : " (" + expense.getNote() + ")"));
                tv.setPadding(0, 16, 0, 16);
                tv.setTextSize(16);
                
                tv.setOnLongClickListener(v -> {
                    new android.app.AlertDialog.Builder(TripDashboardActivity.this)
                        .setTitle("Manage Expense")
                        .setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                            if (which == 0) {
                                Intent intent = new Intent(TripDashboardActivity.this, AddExpenseActivity.class);
                                intent.putExtra("trip_id", tripId);
                                intent.putExtra("expense_id", expense.getId());
                                startActivity(intent);
                            } else if (which == 1) {
                                new android.app.AlertDialog.Builder(TripDashboardActivity.this)
                                    .setTitle("Delete Expense")
                                    .setMessage("Are you sure you want to delete this expense?")
                                    .setPositiveButton("Yes", (d, w) -> {
                                        db.deleteExpenseLocally(expense.getId());
                                        loadTripData();
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                            }
                        })
                        .show();
                    return true;
                });
                
                layoutExpenseList.addView(tv);
                
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(0xFFEEEEEE);
                layoutExpenseList.addView(divider);
            }
        }
    }
}
