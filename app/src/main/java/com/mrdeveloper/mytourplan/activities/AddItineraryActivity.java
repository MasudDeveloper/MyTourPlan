package com.mrdeveloper.mytourplan.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.database.DatabaseHelper;
import com.mrdeveloper.mytourplan.models.ItineraryItem;

public class AddItineraryActivity extends AppCompatActivity {

    private EditText etDay, etTime, etActivity, etLocation;
    private Button btnSaveItinerary;
    private DatabaseHelper db;
    private String tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_itinerary);

        db = new DatabaseHelper(this);
        tripId = getIntent().getStringExtra("trip_id");
        if (tripId == null) {
            Toast.makeText(this, "Error: Trip ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etDay = findViewById(R.id.etDay);
        etTime = findViewById(R.id.etTime);
        etActivity = findViewById(R.id.etActivity);
        etLocation = findViewById(R.id.etLocation);
        btnSaveItinerary = findViewById(R.id.btnSaveItinerary);

        btnSaveItinerary.setOnClickListener(v -> saveItinerary());
    }

    private void saveItinerary() {
        String dayStr = etDay.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String activity = etActivity.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (dayStr.isEmpty() || time.isEmpty() || activity.isEmpty()) {
            Toast.makeText(this, "Day, Time, and Activity are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int day = Integer.parseInt(dayStr);

        ItineraryItem item = new ItineraryItem();
        item.setTripId(tripId);
        item.setDay(day);
        item.setTime(time);
        item.setActivity(activity);
        item.setLocation(location);

        long result = db.addItineraryLocally(item);
        if (result != -1) {
            Toast.makeText(this, "Activity Added", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add activity", Toast.LENGTH_SHORT).show();
        }
    }
}
