package com.mrdeveloper.mytourplan.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.database.DatabaseHelper;
import com.mrdeveloper.mytourplan.models.Trip;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

import java.util.Calendar;

public class AddTripActivity extends AppCompatActivity {

    private EditText etFrom, etDestination, etStartDate, etEndDate, etMembers, etBudget;
    private ImageView ivTripCover;
    private Button btnCreateTrip;
    private DatabaseHelper db;
    private SharedPrefs sharedPrefs;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ivTripCover.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        sharedPrefs = new SharedPrefs(this);

        ivTripCover = findViewById(R.id.ivTripCover);
        etFrom = findViewById(R.id.etFrom);
        etDestination = findViewById(R.id.etDestination);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etMembers = findViewById(R.id.etMembers);
        etBudget = findViewById(R.id.etBudget);
        btnCreateTrip = findViewById(R.id.btnCreateTrip);

        ivTripCover.setOnClickListener(v -> pickImage());
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        btnCreateTrip.setOnClickListener(v -> createTrip());
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editText.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void createTrip() {
        String from = etFrom.getText().toString().trim();
        String destination = etDestination.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String membersStr = etMembers.getText().toString().trim();
        String budgetStr = etBudget.getText().toString().trim();

        if (destination.isEmpty() || from.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || membersStr.isEmpty() || budgetStr.isEmpty()) {
            Toast.makeText(this, "সবগুলো ফিল্ড পূরণ করুন", Toast.LENGTH_SHORT).show();
            return;
        }

        int members = Integer.parseInt(membersStr);
        double budget = Double.parseDouble(budgetStr);
        int userId = sharedPrefs.getUserId();

        Trip trip = new Trip();
        trip.setUserId(String.valueOf(userId));
        trip.setFromLocation(from);
        trip.setDestination(destination);
        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        trip.setMembersCount(members);
        trip.setBudget(budget);
        trip.setStatus("Upcoming"); // Set as upcoming automatically
        trip.setImageUri(selectedImageUri != null ? selectedImageUri.toString() : "");

        long result = db.createTripLocally(trip);

        if (result != -1) {
            Toast.makeText(this, "ট্যুর তৈরি সফল হয়েছে!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, TripDashboardActivity.class);
            intent.putExtra("trip_id", (int) result);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "ট্যুর তৈরি করা যায়নি", Toast.LENGTH_SHORT).show();
        }
    }
}
