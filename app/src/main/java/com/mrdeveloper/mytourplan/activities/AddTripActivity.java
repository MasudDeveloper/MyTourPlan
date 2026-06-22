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
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.models.Trip;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.models.GenericResponse;
import com.mrdeveloper.mytourplan.models.SyncTripResponse;
import com.mrdeveloper.mytourplan.utils.NetworkUtils;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTripActivity extends AppCompatActivity {

    private EditText etFrom, etDestination, etStartDate, etEndDate, etMembers, etBudget;
    private ImageView ivTripCover;
    private Button btnCreateTrip;
    private SharedPrefs sharedPrefs;
    private Uri selectedImageUri = null;
    private boolean isEditMode = false;
    private String editTripId = null;

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

        sharedPrefs = new SharedPrefs(this);

        ivTripCover = findViewById(R.id.ivTripCover);
        etFrom = findViewById(R.id.etFrom);
        etDestination = findViewById(R.id.etDestination);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etMembers = findViewById(R.id.etMembers);
        etBudget = findViewById(R.id.etBudget);
        btnCreateTrip = findViewById(R.id.btnCreateTrip);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ivTripCover.setOnClickListener(v -> pickImage());
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        btnCreateTrip.setOnClickListener(v -> createTrip());

        // Check for edit mode
        if (getIntent().hasExtra("edit_trip_id")) {
            isEditMode = true;
            editTripId = getIntent().getStringExtra("edit_trip_id");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Update Trip");
            }
            btnCreateTrip.setText("Update Trip");
            loadTripData(editTripId);
        }
    }

    private void loadTripData(String tripId) {
        Intent intent = getIntent();
        etFrom.setText(intent.getStringExtra("from_location"));
        etDestination.setText(intent.getStringExtra("destination"));
        etStartDate.setText(intent.getStringExtra("start_date"));
        etEndDate.setText(intent.getStringExtra("end_date"));
        etMembers.setText(String.valueOf(intent.getIntExtra("members_count", 1)));
        etBudget.setText(String.valueOf(intent.getDoubleExtra("budget", 0)));

        String imageUri = intent.getStringExtra("image_uri");
        if (imageUri != null && !imageUri.isEmpty()) {
            selectedImageUri = Uri.parse(imageUri);
            com.bumptech.glide.Glide.with(this).load(imageUri).into(ivTripCover);
        }
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

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Internet connection required to save trip", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCreateTrip.setEnabled(false);
        btnCreateTrip.setText("Saving...");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = sharedPrefs.getToken();

        RequestBody fromBody = RequestBody.create(MediaType.parse("text/plain"), from);
        RequestBody destBody = RequestBody.create(MediaType.parse("text/plain"), destination);
        RequestBody startBody = RequestBody.create(MediaType.parse("text/plain"), startDate);
        RequestBody endBody = RequestBody.create(MediaType.parse("text/plain"), endDate);
        RequestBody membersBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(members));
        RequestBody budgetBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(budget));
        RequestBody statusBody = RequestBody.create(MediaType.parse("text/plain"), "Upcoming");
        
        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            try {
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                if (is != null) {
                    File file = new File(getCacheDir(), "trip_temp.jpg");
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    is.close();

                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                    imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (isEditMode) {
            RequestBody tripIdBody = RequestBody.create(MediaType.parse("text/plain"), editTripId);
            apiService.updateTrip("Bearer " + token, tripIdBody, fromBody, destBody, startBody, endBody, membersBody, budgetBody, statusBody, imagePart).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddTripActivity.this, "ট্যুর আপডেট সফল হয়েছে!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        btnCreateTrip.setEnabled(true);
                        btnCreateTrip.setText("Update Trip");
                        Toast.makeText(AddTripActivity.this, "ট্যুর আপডেট করা যায়নি", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    btnCreateTrip.setEnabled(true);
                    btnCreateTrip.setText("Update Trip");
                    Toast.makeText(AddTripActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            apiService.addTrip("Bearer " + token, fromBody, destBody, startBody, endBody, membersBody, budgetBody, statusBody, imagePart).enqueue(new Callback<SyncTripResponse>() {
                @Override
                public void onResponse(Call<SyncTripResponse> call, Response<SyncTripResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(AddTripActivity.this, "ট্যুর তৈরি সফল হয়েছে!", Toast.LENGTH_SHORT).show();
                        int newTripId = response.body().getServerId();
                        Intent intent = new Intent(AddTripActivity.this, TripDashboardActivity.class);
                        intent.putExtra("trip_id", newTripId);
                        startActivity(intent);
                        finish();
                    } else {
                        btnCreateTrip.setEnabled(true);
                        btnCreateTrip.setText("Create Trip");
                        Toast.makeText(AddTripActivity.this, "ট্যুর তৈরি করা যায়নি", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<SyncTripResponse> call, Throwable t) {
                    btnCreateTrip.setEnabled(true);
                    btnCreateTrip.setText("Create Trip");
                    Toast.makeText(AddTripActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
