package com.mrdeveloper.mytourplan.activities;

import android.content.Intent;
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

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.adapters.ItineraryTimelineAdapter;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.models.ItineraryItem;
import com.mrdeveloper.mytourplan.models.ItineraryResponse;
import com.mrdeveloper.mytourplan.models.SyncGenericResponse;
import com.mrdeveloper.mytourplan.utils.NetworkUtils;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class ItineraryActivity extends AppCompatActivity {

    private TextView tvTotalBudget, tvDuration, tvTravelersCount;
    private RecyclerView rvItinerary;
    private ProgressBar progressBar;
    private ExtendedFloatingActionButton fabAddItinerary;
    private ItineraryTimelineAdapter adapter;
    private String tripId;
    private SharedPrefs sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        sharedPrefs = new SharedPrefs(this);

        tvTotalBudget = findViewById(R.id.tvTotalBudget);
        tvDuration = findViewById(R.id.tvDuration);
        tvTravelersCount = findViewById(R.id.tvTravelersCount);
        rvItinerary = findViewById(R.id.rvItinerary);
        progressBar = findViewById(R.id.progressBar);
        fabAddItinerary = findViewById(R.id.fabAddItinerary);

        if (rvItinerary != null) rvItinerary.setLayoutManager(new LinearLayoutManager(this));

        tripId = getIntent().getStringExtra("trip_id");
        if (tripId == null) {
            int intTripId = getIntent().getIntExtra("trip_id", -1);
            if (intTripId != -1) tripId = String.valueOf(intTripId);
            else tripId = "1";
        }

        fabAddItinerary.setOnClickListener(v -> {
            Intent intent = new Intent(ItineraryActivity.this, AddItineraryActivity.class);
            intent.putExtra("trip_id", tripId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItinerary();
    }

    private void loadItinerary() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "আপনি অফলাইনে আছেন", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = sharedPrefs.getToken();

        apiService.getItinerary("Bearer " + token, tripId).enqueue(new Callback<ItineraryResponse>() {
            @Override
            public void onResponse(Call<ItineraryResponse> call, Response<ItineraryResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ItineraryResponse data = response.body();
                    if (data.getError() == null || data.getError().isEmpty()) {
                        tvTotalBudget.setText(data.getTotalBudget());
                        tvDuration.setText(data.getDuration());
                        tvTravelersCount.setText(data.getTravelersCount());

                        List<ItineraryItem> items = data.getSchedule();
                        if (items != null) {
                            adapter = new ItineraryTimelineAdapter(items, item -> {
                                manageItinerary(item);
                            });
                            rvItinerary.setAdapter(adapter);
                        }
                    } else {
                        Toast.makeText(ItineraryActivity.this, data.getError(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ItineraryActivity.this, "ভ্রমণ পরিকল্পনা লোড করা যায়নি", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ItineraryResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ItineraryActivity.this, "ত্রুটি: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void manageItinerary(ItineraryItem item) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("ভ্রমণ পরিকল্পনা")
            .setItems(new CharSequence[]{"সম্পাদনা করুন", "মুছে ফেলুন"}, (dialog, which) -> {
                if (which == 0) {
                    Intent intent = new Intent(ItineraryActivity.this, AddItineraryActivity.class);
                    intent.putExtra("trip_id", tripId);
                    intent.putExtra("itinerary_id", item.getId());
                    intent.putExtra("day", item.getDay());
                    intent.putExtra("time", item.getTime());
                    intent.putExtra("activity", item.getActivity());
                    intent.putExtra("location", item.getLocation());
                    startActivity(intent);
                } else if (which == 1) {
                    new android.app.AlertDialog.Builder(ItineraryActivity.this)
                        .setTitle("পরিকল্পনা মুছে ফেলুন")
                        .setMessage("আপনি কি নিশ্চিতভাবে এই পরিকল্পনাটি মুছে ফেলতে চান?")
                        .setPositiveButton("হ্যাঁ", (d, w) -> deleteItinerary(item))
                        .setNegativeButton("না", null)
                        .show();
                }
            })
            .show();
    }

    private void deleteItinerary(ItineraryItem item) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "ইন্টারনেট সংযোগ প্রয়োজন", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = sharedPrefs.getToken();

        apiService.syncItinerary("Bearer " + token, tripId, item.getDay(), item.getTime(), item.getActivity(), item.getLocation(), "", "DELETE", item.getId()).enqueue(new Callback<SyncGenericResponse>() {
            @Override
            public void onResponse(Call<SyncGenericResponse> call, Response<SyncGenericResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ItineraryActivity.this, "পরিকল্পনা মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show();
                    loadItinerary();
                } else {
                    Toast.makeText(ItineraryActivity.this, "মুছে ফেলা যায়নি", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SyncGenericResponse> call, Throwable t) {
                Toast.makeText(ItineraryActivity.this, "ত্রুটি: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
