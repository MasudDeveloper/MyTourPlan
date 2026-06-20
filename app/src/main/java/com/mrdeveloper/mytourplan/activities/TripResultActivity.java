package com.mrdeveloper.mytourplan.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.models.GeneratePlanRequest;
import com.mrdeveloper.mytourplan.models.GeneratePlanResponse;
import com.mrdeveloper.mytourplan.models.GenericResponse;
import com.mrdeveloper.mytourplan.models.ItineraryItem;
import com.mrdeveloper.mytourplan.models.SaveTripRequest;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripResultActivity extends AppCompatActivity {

    private TextView tvTotalCost, tvCostPerPerson, tvBudgetStatus, tvHotelCost, tvTransportCost, tvFoodCost, tvItinerary;
    private Button btnSaveTrip;
    private GeneratePlanResponse planData;
    private GeneratePlanRequest inputs;
    private SharedPrefs sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_result);

        sharedPrefs = new SharedPrefs(this);

        tvTotalCost = findViewById(R.id.tvTotalCost);
        tvCostPerPerson = findViewById(R.id.tvCostPerPerson);
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus);
        tvHotelCost = findViewById(R.id.tvHotelCost);
        tvTransportCost = findViewById(R.id.tvTransportCost);
        tvFoodCost = findViewById(R.id.tvFoodCost);
        tvItinerary = findViewById(R.id.tvItinerary);
        btnSaveTrip = findViewById(R.id.btnSaveTrip);

        String planJson = getIntent().getStringExtra("plan_data");
        String inputsJson = getIntent().getStringExtra("inputs");
        if (planJson != null && inputsJson != null) {
            Gson gson = new Gson();
            planData = gson.fromJson(planJson, GeneratePlanResponse.class);
            // Need a way to get the inputs, but since it's just variables we can parse it to GeneratePlanRequest class if we modify it to have getters, or JsonObject. 
            // Wait, we can add getters to GeneratePlanRequest, or just use JsonObject for inputs.
            // Let's create a quick local parse for inputs to save it.
            inputs = gson.fromJson(inputsJson, GeneratePlanRequest.class);
            displayData();
        }

        btnSaveTrip.setOnClickListener(v -> saveTrip());
    }

    private void displayData() {
        double totalCost = planData.getCosts().getTotalEstimatedCost();
        int persons = inputs.getPersons();
        double costPerPerson = persons > 0 ? (totalCost / persons) : totalCost;

        tvTotalCost.setText(String.format("Total Estimated Cost: ৳%.2f", totalCost));
        tvCostPerPerson.setText(String.format("Cost Per Person: ৳%.2f", costPerPerson));
        tvBudgetStatus.setText("Status: " + planData.getCosts().getBudgetStatus());
        tvHotelCost.setText("Hotel Cost: ৳" + planData.getCosts().getHotel());
        tvTransportCost.setText("Transport Cost: ৳" + planData.getCosts().getTransport());
        tvFoodCost.setText("Food Cost: ৳" + planData.getCosts().getFood());

        List<ItineraryItem> itinerary = planData.getItinerary();
        StringBuilder sb = new StringBuilder();
        for (ItineraryItem item : itinerary) {
            sb.append("Day ").append(item.getDay()).append(" - ");
            sb.append(item.getTime()).append("\n");
            sb.append(item.getActivity()).append(" (").append(item.getLocation()).append(")\n\n");
        }
        tvItinerary.setText(sb.toString());
    }

    private void saveTrip() {
        // Need getters in GeneratePlanRequest
        // For simplicity, let's parse raw JSON for inputs or just add getters to GeneratePlanRequest class in another call.
        // I will just use the getters I am about to add to GeneratePlanRequest.
        
        SaveTripRequest request = new SaveTripRequest(
            inputs.getFromLocation(),
            inputs.getToLocation(),
            inputs.getDays(),
            inputs.getPersons(),
            inputs.getBudget(),
            planData.getCosts().getTotalEstimatedCost(),
            planData.getCosts(),
            planData.getItinerary()
        );

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.saveTrip("Bearer " + sharedPrefs.getToken(), request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TripResultActivity.this, "Trip Saved Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(TripResultActivity.this, "Failed to save trip", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(TripResultActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
