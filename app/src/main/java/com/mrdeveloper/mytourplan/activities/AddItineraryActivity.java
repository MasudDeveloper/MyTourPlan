package com.mrdeveloper.mytourplan.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

    private static final String TAG = "AddItineraryActivity";

    private EditText etDay, etTime, etActivity, etLocation;
    private Button btnSaveItinerary;
    private String tripId;
    private int itineraryId = -1;
    private SharedPrefs sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_itinerary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup Toolbar with back navigation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        sharedPrefs = new SharedPrefs(this);
        tripId = getIntent().getStringExtra("trip_id");
        if (tripId == null) {
            Toast.makeText(this, "Error: Trip ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        itineraryId = getIntent().getIntExtra("itinerary_id", -1);

        etDay = findViewById(R.id.etDay);
        etTime = findViewById(R.id.etTime);
        etActivity = findViewById(R.id.etActivity);
        etLocation = findViewById(R.id.etLocation);
        btnSaveItinerary = findViewById(R.id.btnSaveItinerary);

        if (itineraryId != -1) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("পরিকল্পনা সংশোধন");
            }
            btnSaveItinerary.setText("সংশোধন করুন");
            etDay.setText(String.valueOf(getIntent().getIntExtra("day", 1)));
            etTime.setText(getIntent().getStringExtra("time"));
            etActivity.setText(getIntent().getStringExtra("activity"));
            etLocation.setText(getIntent().getStringExtra("location"));
        }

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

        int day;
        try {
            day = Integer.parseInt(dayStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid day number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Internet connection required", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveItinerary.setEnabled(false);
        btnSaveItinerary.setText(itineraryId != -1 ? "সংরক্ষণ করা হচ্ছে..." : "যোগ করা হচ্ছে...");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = sharedPrefs.getToken();

        String action = itineraryId != -1 ? "UPDATE" : "INSERT";
        int serverId = itineraryId != -1 ? itineraryId : -1;

        Log.d(TAG, "Syncing itinerary: trip_id=" + tripId + ", day=" + day + ", time=" + time + ", activity=" + activity + ", location=" + location + ", action=" + action + ", server_id=" + serverId);

        apiService.syncItinerary("Bearer " + token, tripId, day, time, activity, location, "", action, serverId).enqueue(new Callback<SyncGenericResponse>() {
            @Override
            public void onResponse(Call<SyncGenericResponse> call, Response<SyncGenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SyncGenericResponse body = response.body();
                    if (body.isSuccess()) {
                        Toast.makeText(AddItineraryActivity.this, itineraryId != -1 ? "পরিকল্পনা সফলভাবে সংশোধন করা হয়েছে!" : "পরিকল্পনা সফলভাবে যোগ করা হয়েছে!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        btnSaveItinerary.setEnabled(true);
                        btnSaveItinerary.setText(itineraryId != -1 ? "সংশোধন করুন" : "যোগ করুন");
                        String errorMsg = body.getError() != null ? body.getError() : "Unknown server error";
                        Log.e(TAG, "Server returned error: " + errorMsg);
                        Toast.makeText(AddItineraryActivity.this, "Failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    btnSaveItinerary.setEnabled(true);
                    btnSaveItinerary.setText(itineraryId != -1 ? "সংশোধন করুন" : "যোগ করুন");
                    String errorDetail = "HTTP " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorDetail += ": " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Sync failed: " + errorDetail);
                    Toast.makeText(AddItineraryActivity.this, "Failed: " + errorDetail, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SyncGenericResponse> call, Throwable t) {
                btnSaveItinerary.setEnabled(true);
                btnSaveItinerary.setText(itineraryId != -1 ? "সংশোধন করুন" : "যোগ করুন");
                Log.e(TAG, "Network error syncing itinerary", t);
                Toast.makeText(AddItineraryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
