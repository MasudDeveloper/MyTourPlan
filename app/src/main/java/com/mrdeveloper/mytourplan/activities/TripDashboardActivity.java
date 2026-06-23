package com.mrdeveloper.mytourplan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.models.GenericRequest;
import com.mrdeveloper.mytourplan.models.GenericResponse;
import com.mrdeveloper.mytourplan.models.TripDashboardResponse;
import com.mrdeveloper.mytourplan.utils.NetworkUtils;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;
import android.widget.Toast;
import android.net.Uri;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class TripDashboardActivity extends AppCompatActivity {

    private TextView tvTotalBudget, tvMembers, tvEmptyState, tvBudgetPercentage, tvTripDates, tvRemainingBudget;
    private View btnAddExpense, btnCalculate, btnViewItinerary, btnGroupMembers, fabAddExpense, btnTripNotes;
    private LinearLayout layoutExpenseList;
    private android.widget.ProgressBar budgetProgress;
    private ImageView ivDashboardCover;
    private CollapsingToolbarLayout collapsingToolbar;
    private SharedPrefs sharedPrefs;
    private int tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_dashboard);

        sharedPrefs = new SharedPrefs(this);
        tripId = getIntent().getIntExtra("trip_id", -1);

        tvTotalBudget = findViewById(R.id.tvTotalBudget);
        tvMembers = findViewById(R.id.tvMembers);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget);
        layoutExpenseList = findViewById(R.id.layoutExpenseList);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnViewItinerary = findViewById(R.id.btnViewItinerary);
        btnGroupMembers = findViewById(R.id.btnGroupMembers);
        btnTripNotes = findViewById(R.id.btnTripNotes);
        fabAddExpense = findViewById(R.id.fabAddExpense);
        
        tvBudgetPercentage = findViewById(R.id.tvBudgetPercentage);
        tvTripDates = findViewById(R.id.tvTripDates);
        budgetProgress = findViewById(R.id.budgetProgress);
        ivDashboardCover = findViewById(R.id.ivDashboardCover);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        View.OnClickListener addExpenseListener = v -> {
            Intent intent = new Intent(TripDashboardActivity.this, AddExpenseActivity.class);
            intent.putExtra("trip_id", tripId);
            startActivity(intent);
        };
        
        btnAddExpense.setOnClickListener(addExpenseListener);
        fabAddExpense.setOnClickListener(addExpenseListener);

        btnGroupMembers.setOnClickListener(v -> {
            Intent intent = new Intent(TripDashboardActivity.this, GroupMembersActivity.class);
            intent.putExtra("trip_id", String.valueOf(tripId));
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

        btnTripNotes.setOnClickListener(v -> {
            Intent intent = new Intent(TripDashboardActivity.this, TripNotesActivity.class);
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
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "আপনি অফলাইনে আছেন", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sharedPrefs.getToken();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getTripDashboard("Bearer " + token, String.valueOf(tripId)).enqueue(new Callback<TripDashboardResponse>() {
            @Override
            public void onResponse(Call<TripDashboardResponse> call, Response<TripDashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TripDashboardResponse data = response.body();
                    if (data.getError() == null || data.getError().isEmpty()) {
                        com.mrdeveloper.mytourplan.models.Trip trip = data.getTrip();
                        if (trip != null) {
                            int totalMembers = trip.getMembersCount();
                            double budgetPerPerson = trip.getBudget();
                            if (tvTripDates != null) tvTripDates.setText(trip.getStartDate() + " থেকে " + trip.getEndDate());
                            if (collapsingToolbar != null) collapsingToolbar.setTitle(trip.getDestination());
                            
                            if (ivDashboardCover != null && trip.getImageUri() != null && !trip.getImageUri().isEmpty()) {
                                Glide.with(TripDashboardActivity.this).load(Uri.parse(trip.getImageUri())).centerCrop().into(ivDashboardCover);
                            }

                            double totalBudget = totalMembers * budgetPerPerson;
                            tvMembers.setText("+" + totalMembers);
                            tvTotalBudget.setText("৳" + String.format("%.2f", totalBudget));

                            List<com.mrdeveloper.mytourplan.models.Expense> expenses = data.getExpenses();
                            layoutExpenseList.removeAllViews();
                            double totalExpense = 0;

                            if (expenses == null || expenses.isEmpty()) {
                                layoutExpenseList.addView(tvEmptyState);
                            } else {
                                for (com.mrdeveloper.mytourplan.models.Expense expense : expenses) {
                                    totalExpense += expense.getAmount();
                                    
                                    View expenseView = getLayoutInflater().inflate(R.layout.item_expense_recent, layoutExpenseList, false);
                                    TextView tvTitle = expenseView.findViewById(R.id.tvExpenseTitle);
                                    TextView tvSubtitle = expenseView.findViewById(R.id.tvExpenseSubtitle);
                                    TextView tvAmount = expenseView.findViewById(R.id.tvExpenseAmount);
                                    
                                    tvTitle.setText(expense.getCategory());
                                    tvSubtitle.setText(expense.getNote() == null || expense.getNote().isEmpty() ? expense.getCategory() : expense.getNote());
                                    tvAmount.setText("-৳" + String.format("%.2f", expense.getAmount()));
                                                                      expenseView.setOnLongClickListener(v -> {
                                        new android.app.AlertDialog.Builder(TripDashboardActivity.this)
                                            .setTitle("খরচ পরিচালনা")
                                            .setItems(new CharSequence[]{"সম্পাদনা করুন", "মুছে ফেলুন"}, (dialog, which) -> {
                                                if (which == 0) {
                                                    Intent intent = new Intent(TripDashboardActivity.this, AddExpenseActivity.class);
                                                    intent.putExtra("trip_id", tripId);
                                                    intent.putExtra("expense_id", expense.getId());
                                                    intent.putExtra("category", expense.getCategory());
                                                    intent.putExtra("amount", expense.getAmount());
                                                    intent.putExtra("note", expense.getNote());
                                                    startActivity(intent);
                                                } else if (which == 1) {
                                                    new android.app.AlertDialog.Builder(TripDashboardActivity.this)
                                                        .setTitle("খরচ মুছে ফেলুন")
                                                        .setMessage("আপনি কি নিশ্চিতভাবে এই খরচটি মুছে ফেলতে চান?")
                                                        .setPositiveButton("হ্যাঁ", (d, w) -> {
                                                            ApiService service = ApiClient.getClient().create(ApiService.class);
                                                            service.syncExpense("Bearer " + token, String.valueOf(tripId), "", 0, "", "", "", "DELETE", Integer.parseInt(expense.getId())).enqueue(new Callback<com.mrdeveloper.mytourplan.models.SyncGenericResponse>() {
                                                                @Override
                                                                public void onResponse(Call<com.mrdeveloper.mytourplan.models.SyncGenericResponse> c, Response<com.mrdeveloper.mytourplan.models.SyncGenericResponse> res) {
                                                                    if (res.isSuccessful()) {
                                                                        loadTripData();
                                                                    } else {
                                                                        Toast.makeText(TripDashboardActivity.this, "মুছে ফেলা যায়নি", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                                @Override
                                                                public void onFailure(Call<com.mrdeveloper.mytourplan.models.SyncGenericResponse> c, Throwable t) {
                                                                    Toast.makeText(TripDashboardActivity.this, "ত্রুটি: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        })
                                                        .setNegativeButton("না", null)
                                                        .show();
                                                }
                                            })
                                            .show();
                                        return true;
                                    });
                                    
                                    layoutExpenseList.addView(expenseView);
                                }
                            }
                            
                            // Update Budget Progress
                            int progress = 0;
                            if (totalBudget > 0) {
                                progress = (int) ((totalExpense / totalBudget) * 100);
                            }
                            tvBudgetPercentage.setText(progress + "% (৳" + String.format("%.0f", totalExpense) + ")");
                            budgetProgress.setProgress(progress);

                            double remainingBudget = totalBudget - totalExpense;
                            if (tvRemainingBudget != null) {
                                if (remainingBudget >= 0) {
                                    tvRemainingBudget.setText("অবশিষ্ট: ৳" + String.format("%.2f", remainingBudget));
                                    tvRemainingBudget.setTextColor(android.graphics.Color.parseColor("#E2E8F0"));
                                } else {
                                    tvRemainingBudget.setText("অতিরিক্ত খরচ: ৳" + String.format("%.2f", Math.abs(remainingBudget)));
                                    tvRemainingBudget.setTextColor(android.graphics.Color.parseColor("#EF4444"));
                                }
                            }
                        }
                    } else {
                        Toast.makeText(TripDashboardActivity.this, data.getError(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<TripDashboardResponse> call, Throwable t) {
                Toast.makeText(TripDashboardActivity.this, "ট্যুর তথ্য লোড করা যায়নি", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
