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
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.models.ExpenseTrackerResponse;
import com.mrdeveloper.mytourplan.utils.NetworkUtils;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.mrdeveloper.mytourplan.adapters.ContributorAdapter;
import com.mrdeveloper.mytourplan.models.Contributor;

import java.util.ArrayList;
import java.util.List;

public class CalculationActivity extends AppCompatActivity {

    private TextView tvPerPersonCost, tvMembers, tvBudgetPerPerson, tvTotalExpense;
    private TextView tvResultTitle, tvResultAmount;
    private MaterialCardView cardResult;
    private androidx.recyclerview.widget.RecyclerView rvContributors;
    private SharedPrefs sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPrefs = new SharedPrefs(this);
        int tripId = getIntent().getIntExtra("trip_id", -1);

        tvPerPersonCost = findViewById(R.id.tvPerPersonCost);
        tvMembers = findViewById(R.id.tvMembers);
        tvBudgetPerPerson = findViewById(R.id.tvBudgetPerPerson);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultAmount = findViewById(R.id.tvResultAmount);
        cardResult = findViewById(R.id.cardResult);
        rvContributors = findViewById(R.id.rvContributors);
        rvContributors.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        calculate(tripId);
    }

    private void calculate(int tripId) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "ইন্টারনেট সংযোগ প্রয়োজন", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = sharedPrefs.getToken();

        apiService.getExpenseTracker("Bearer " + token, String.valueOf(tripId)).enqueue(new Callback<ExpenseTrackerResponse>() {
            @Override
            public void onResponse(Call<ExpenseTrackerResponse> call, Response<ExpenseTrackerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ExpenseTrackerResponse data = response.body();
                    if (data.getError() == null || data.getError().isEmpty()) {
                        double totalExpense = data.getTotalSpent();
                        double totalBudget = data.getTotalBudget();
                        
                        // Parse budget string "X Travelers x Y BDT"
                        int members = 0;
                        double budgetPerPerson = 0;
                        try {
                            String bs = data.getBudgetString();
                            String[] parts = bs.split(" x ");
                            members = Integer.parseInt(parts[0].split(" ")[0]);
                            budgetPerPerson = Double.parseDouble(parts[1].split(" ")[0].replace(",", ""));
                        } catch (Exception e) {}

                        double perPersonExpense = members > 0 ? (totalExpense / members) : 0;

                        tvMembers.setText(String.valueOf(members));
                        tvBudgetPerPerson.setText("৳" + String.format("%.2f", budgetPerPerson));
                        tvTotalExpense.setText("৳" + String.format("%.2f", totalExpense));
                        tvPerPersonCost.setText("৳" + String.format("%.2f", perPersonExpense));

                        double diff = budgetPerPerson - perPersonExpense;

                        if (diff > 0) {
                            tvResultTitle.setText("সবাইকে ফেরত দিতে হবে (জনপ্রতি)");
                            tvResultAmount.setText("৳" + String.format("%.2f", diff));
                            cardResult.setCardBackgroundColor(Color.parseColor("#4CAF50")); // Green
                        } else if (diff < 0) {
                            tvResultTitle.setText("সবার থেকে আরও তুলতে হবে (জনপ্রতি)");
                            tvResultAmount.setText("৳" + String.format("%.2f", Math.abs(diff)));
                            cardResult.setCardBackgroundColor(Color.parseColor("#F44336")); // Red
                        } else {
                            tvResultTitle.setText("হিসাব বরাবর (ফেরত বা দেওয়া লাগবে না)");
                            tvResultAmount.setText("৳0.00");
                            cardResult.setCardBackgroundColor(Color.parseColor("#0582CA")); // Blue
                        }

                        if (data.getContributors() != null) {
                            ContributorAdapter adapter = new ContributorAdapter(data.getContributors());
                            rvContributors.setAdapter(adapter);
                        }
                    } else {
                        Toast.makeText(CalculationActivity.this, data.getError(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ExpenseTrackerResponse> call, Throwable t) {
                Toast.makeText(CalculationActivity.this, "হিসাব লোড করা যায়নি", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
