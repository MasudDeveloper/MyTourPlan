package com.mrdeveloper.mytourplan.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.adapters.ContributorAdapter;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.database.DatabaseHelper;
import com.mrdeveloper.mytourplan.models.ExpenseTrackerResponse;
import com.mrdeveloper.mytourplan.models.Expense;
import com.mrdeveloper.mytourplan.utils.NetworkUtils;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;
import com.mrdeveloper.mytourplan.workers.SyncWorker;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseTrackerActivity extends AppCompatActivity {

    private TextView tvTotalBudget, tvBudgetString, tvTotalSpent, tvRemaining;
    private RecyclerView rvContributors;
    private ProgressBar progressBar;
    private ContributorAdapter adapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_tracker);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvTotalBudget = findViewById(R.id.tvTotalBudget);
        tvBudgetString = findViewById(R.id.tvBudgetString);
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvRemaining = findViewById(R.id.tvRemaining);
        rvContributors = findViewById(R.id.rvContributors);
        progressBar = findViewById(R.id.progressBar);

        rvContributors.setLayoutManager(new LinearLayoutManager(this));
        db = new DatabaseHelper(this);

        // Get trip_id from Intent, default to "1" for testing
        String tripId = getIntent().getStringExtra("trip_id");
        if (tripId == null) tripId = "1";

        loadExpenseData(tripId);
    }

    private void loadExpenseData(String tripId) {
        // 1. Load Offline First
        List<Expense> localExpenses = db.getExpensesByTrip(tripId);
        double localTotalSpent = 0.0;
        for (Expense e : localExpenses) {
            localTotalSpent += e.getAmount();
        }
        
        if (!localExpenses.isEmpty()) {
            tvTotalSpent.setText("৳ " + localTotalSpent);
            // Local expenses are currently just a raw list. We would map them to contributors if needed.
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }

        // 2. Fetch Online
        if (NetworkUtils.isNetworkAvailable(this)) {
            SyncWorker.scheduleSync(this);

            String token = new SharedPrefs(this).getToken();
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<ExpenseTrackerResponse> call = apiService.getExpenseTracker("Bearer " + token, tripId);

            call.enqueue(new Callback<ExpenseTrackerResponse>() {
                @Override
                public void onResponse(Call<ExpenseTrackerResponse> call, Response<ExpenseTrackerResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        ExpenseTrackerResponse data = response.body();
                        if (data.getError() == null || data.getError().isEmpty()) {
                            tvTotalBudget.setText("৳ " + data.getTotalBudget());
                            tvTotalSpent.setText("৳ " + data.getTotalSpent());
                            tvBudgetString.setText(data.getBudgetString());
                            
                            double remaining = data.getTotalBudget() - data.getTotalSpent();
                            tvRemaining.setText("৳ " + remaining);

                            adapter = new ContributorAdapter(data.getContributors());
                            rvContributors.setAdapter(adapter);
                        } else {
                            Toast.makeText(ExpenseTrackerActivity.this, data.getError(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ExpenseTrackerResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    if (localExpenses.isEmpty()) {
                        Toast.makeText(ExpenseTrackerActivity.this, "Failed to fetch expenses offline.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            if (localExpenses.isEmpty()) {
                Toast.makeText(this, "You are offline and have no saved expenses.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Showing offline cached data.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
