package com.mrdeveloper.mytourplan.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.google.android.material.card.MaterialCardView;
import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.database.DatabaseHelper;
import com.mrdeveloper.mytourplan.models.Expense;
import com.mrdeveloper.mytourplan.models.Trip;

import java.util.List;

public class CalculationActivity extends AppCompatActivity {

    private TextView tvPerPersonCost, tvMembers, tvBudgetPerPerson, tvTotalExpense;
    private TextView tvResultTitle, tvResultAmount;
    private MaterialCardView cardResult;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        int tripId = getIntent().getIntExtra("trip_id", -1);

        tvPerPersonCost = findViewById(R.id.tvPerPersonCost);
        tvMembers = findViewById(R.id.tvMembers);
        tvBudgetPerPerson = findViewById(R.id.tvBudgetPerPerson);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultAmount = findViewById(R.id.tvResultAmount);
        cardResult = findViewById(R.id.cardResult);

        calculate(tripId);
    }

    private void calculate(int tripId) {
        Trip trip = db.getTripById(String.valueOf(tripId));
        if (trip == null) return;

        List<Expense> expenses = db.getExpensesByTrip(String.valueOf(tripId));
        double totalExpense = 0;
        for (Expense exp : expenses) {
            totalExpense += exp.getAmount();
        }

        int members = trip.getMembersCount();
        double budgetPerPerson = trip.getBudget();
        double perPersonExpense = members > 0 ? (totalExpense / members) : 0;

        tvMembers.setText(String.valueOf(members));
        tvBudgetPerPerson.setText("৳" + String.format("%.2f", budgetPerPerson));
        tvTotalExpense.setText("৳" + String.format("%.2f", totalExpense));
        tvPerPersonCost.setText("৳" + String.format("%.2f", perPersonExpense));

        double diff = budgetPerPerson - perPersonExpense;

        if (diff > 0) {
            // Under budget, need to refund
            tvResultTitle.setText("সবাইকে ফেরত দিতে হবে (জনপ্রতি)");
            tvResultAmount.setText("৳" + String.format("%.2f", diff));
            cardResult.setCardBackgroundColor(Color.parseColor("#4CAF50")); // Green
        } else if (diff < 0) {
            // Over budget, need to collect
            tvResultTitle.setText("সবার থেকে আরও তুলতে হবে (জনপ্রতি)");
            tvResultAmount.setText("৳" + String.format("%.2f", Math.abs(diff)));
            cardResult.setCardBackgroundColor(Color.parseColor("#F44336")); // Red
        } else {
            // Exact
            tvResultTitle.setText("হিসাব বরাবর (ফেরত বা দেওয়া লাগবে না)");
            tvResultAmount.setText("৳0.00");
            cardResult.setCardBackgroundColor(Color.parseColor("#0582CA")); // Blue
        }
    }
}
