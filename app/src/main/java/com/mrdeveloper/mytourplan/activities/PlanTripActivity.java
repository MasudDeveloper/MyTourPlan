package com.mrdeveloper.mytourplan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.models.GeneratePlanRequest;
import com.mrdeveloper.mytourplan.models.GeneratePlanResponse;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanTripActivity extends AppCompatActivity {

    private EditText etFrom, etTo, etDays, etPersons, etBudget;
    private Button btnGeneratePlan;
    private SharedPrefs sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_trip);

        sharedPrefs = new SharedPrefs(this);

        etFrom = findViewById(R.id.etFrom);
        etTo = findViewById(R.id.etTo);
        etDays = findViewById(R.id.etDays);
        etPersons = findViewById(R.id.etPersons);
        etBudget = findViewById(R.id.etBudget);
        btnGeneratePlan = findViewById(R.id.btnGeneratePlan);

        btnGeneratePlan.setOnClickListener(v -> generatePlan());
    }

    private void generatePlan() {
        String from = etFrom.getText().toString();
        String to = etTo.getText().toString();
        String days = etDays.getText().toString();
        String persons = etPersons.getText().toString();
        String budget = etBudget.getText().toString();

        if (from.isEmpty() || to.isEmpty() || days.isEmpty() || persons.isEmpty() || budget.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        GeneratePlanRequest request = new GeneratePlanRequest(from, to, Integer.parseInt(days), Integer.parseInt(persons), Double.parseDouble(budget));

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.generatePlan("Bearer " + sharedPrefs.getToken(), request).enqueue(new Callback<GeneratePlanResponse>() {
            @Override
            public void onResponse(Call<GeneratePlanResponse> call, Response<GeneratePlanResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PlanTripActivity.this, "Plan Generated!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PlanTripActivity.this, TripResultActivity.class);
                    // Pass the whole response as a JSON string to the next activity
                    intent.putExtra("plan_data", new Gson().toJson(response.body()));
                    intent.putExtra("inputs", new Gson().toJson(request));
                    startActivity(intent);
                } else {
                    Toast.makeText(PlanTripActivity.this, "Failed to generate plan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeneratePlanResponse> call, Throwable t) {
                Toast.makeText(PlanTripActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
