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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.adapters.ItineraryTimelineAdapter;
import com.mrdeveloper.mytourplan.database.DatabaseHelper;
import com.mrdeveloper.mytourplan.models.ItineraryItem;
import com.mrdeveloper.mytourplan.models.ItineraryResponse;
import com.mrdeveloper.mytourplan.models.Trip;

import java.util.ArrayList;
import java.util.List;

public class ItineraryActivity extends AppCompatActivity {

    private TextView tvTotalBudget, tvDuration, tvTravelersCount;
    private RecyclerView rvItinerary;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddItinerary;
    private ItineraryTimelineAdapter adapter;
    private DatabaseHelper db;
    private String tripId;

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

        db = new DatabaseHelper(this);

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
        progressBar.setVisibility(View.VISIBLE);
        
        Trip trip = db.getTripById(tripId);
        if (trip != null) {
            tvTotalBudget.setText(String.format("৳%.2f", trip.getBudget() * trip.getMembersCount()));
            tvDuration.setText(trip.getStartDate() + " to " + trip.getEndDate());
            tvTravelersCount.setText(trip.getMembersCount() + " Members");
        }

        List<ItineraryItem> items = db.getItineraryByTrip(tripId);

        adapter = new ItineraryTimelineAdapter(items);
        rvItinerary.setAdapter(adapter);
        
        progressBar.setVisibility(View.GONE);
    }
}
