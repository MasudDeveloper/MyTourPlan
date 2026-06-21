package com.mrdeveloper.mytourplan.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.models.SyncGenericResponse;
import com.mrdeveloper.mytourplan.utils.NetworkUtils;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Calendar;
import java.util.Locale;

public class AddItineraryActivity extends AppCompatActivity {

    private EditText etDay, etTime, etActivity, etLocation;
    private Button btnSaveItinerary;
    private String tripId;
    private SharedPrefs sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_itinerary);

        sharedPrefs = new SharedPrefs(this);
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

        etTime.setFocusable(false);
        etTime.setClickable(true);
        etTime.setOnClickListener(v -> showTimePicker());

        btnSaveItinerary.setOnClickListener(v -> saveItinerary());
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    String amPm;
                    if (hourOfDay >= 12) {
                        amPm = "PM";
                        if (hourOfDay > 12) hourOfDay -= 12;
                    } else {
                        amPm = "AM";
                        if (hourOfDay == 0) hourOfDay = 12;
                    }
                    String time = String.format(Locale.getDefault(), "%02d:%02d %s", hourOfDay, minuteOfHour, amPm);
                    etTime.setText(time);
                }, hour, minute, false);
        timePickerDialog.show();
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

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Internet connection required", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveItinerary.setEnabled(false);
        btnSaveItinerary.setText("Saving...");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = sharedPrefs.getToken();

        apiService.syncItinerary("Bearer " + token, tripId, day, time, activity, location, "").enqueue(new Callback<SyncGenericResponse>() {
            @Override
            public void onResponse(Call<SyncGenericResponse> call, Response<SyncGenericResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddItineraryActivity.this, "Activity Added", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnSaveItinerary.setEnabled(true);
                    btnSaveItinerary.setText("Save Itinerary");
                    Toast.makeText(AddItineraryActivity.this, "Failed to add activity", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SyncGenericResponse> call, Throwable t) {
                btnSaveItinerary.setEnabled(true);
                btnSaveItinerary.setText("Save Itinerary");
                Toast.makeText(AddItineraryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
