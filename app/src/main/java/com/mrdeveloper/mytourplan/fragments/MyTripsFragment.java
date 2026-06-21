package com.mrdeveloper.mytourplan.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.adapters.TripAdapter;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.database.DatabaseHelper;
import com.mrdeveloper.mytourplan.models.Trip;
import com.mrdeveloper.mytourplan.models.TripsResponse;
import com.mrdeveloper.mytourplan.utils.NetworkUtils;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;
import com.mrdeveloper.mytourplan.workers.SyncWorker;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyTripsFragment extends Fragment {

    private RecyclerView rvTrips;
    private ProgressBar progressBar;
    private View layoutEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TripAdapter adapter;
    private DatabaseHelper db;
    private TripAdapter.OnTripActionListener tripListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_trips, container, false);
        
        rvTrips = view.findViewById(R.id.rvTrips);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        rvTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        db = new DatabaseHelper(getContext());
        
        swipeRefreshLayout.setOnRefreshListener(this::loadTrips);

        setupListener();
        loadTrips();

        return view;
    }

    private void setupListener() {
        tripListener = new TripAdapter.OnTripActionListener() {
            @Override
            public void onEditClick(Trip trip) {
                android.content.Intent intent = new android.content.Intent(getContext(), com.mrdeveloper.mytourplan.activities.AddTripActivity.class);
                intent.putExtra("edit_trip_id", trip.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Trip trip) {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Trip")
                    .setMessage("Are you sure you want to delete this trip? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        progressBar.setVisibility(View.VISIBLE);
                        String token = new SharedPrefs(getContext()).getToken();
                        ApiService apiService = ApiClient.getClient().create(ApiService.class);
                        com.mrdeveloper.mytourplan.models.GenericRequest req = new com.mrdeveloper.mytourplan.models.GenericRequest(trip.getId());
                        apiService.deleteTrip("Bearer " + token, req).enqueue(new Callback<com.mrdeveloper.mytourplan.models.GenericResponse>() {
                            @Override
                            public void onResponse(Call<com.mrdeveloper.mytourplan.models.GenericResponse> call, Response<com.mrdeveloper.mytourplan.models.GenericResponse> response) {
                                progressBar.setVisibility(View.GONE);
                                if (response.isSuccessful()) {
                                    Toast.makeText(getContext(), "Trip deleted successfully", Toast.LENGTH_SHORT).show();
                                    loadTrips();
                                } else {
                                    Toast.makeText(getContext(), "Failed to delete trip", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<com.mrdeveloper.mytourplan.models.GenericResponse> call, Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }

            @Override
            public void onTripClick(Trip trip) {
                android.content.Intent intent = new android.content.Intent(getContext(), com.mrdeveloper.mytourplan.activities.TripDashboardActivity.class);
                try {
                    intent.putExtra("trip_id", Integer.parseInt(trip.getId()));
                    intent.putExtra("members", trip.getMembersCount());
                    intent.putExtra("budget", trip.getBudget());
                } catch (NumberFormatException e) {
                    // Ignore
                }
                startActivity(intent);
            }
        };
    }

    private void loadTrips() {
        if (getContext() == null) return;
        
        if (NetworkUtils.isNetworkAvailable(getContext())) {
            progressBar.setVisibility(View.VISIBLE);
            String token = new SharedPrefs(getContext()).getToken();
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<TripsResponse> call = apiService.getMyTrips("Bearer " + token);

            call.enqueue(new Callback<TripsResponse>() {
                @Override
                public void onResponse(Call<TripsResponse> call, Response<TripsResponse> response) {
                    if (!isAdded() || getContext() == null) return;
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        TripsResponse data = response.body();
                        if (data.getError() == null || data.getError().isEmpty()) {
                            List<Trip> onlineTrips = data.getTrips();
                            
                            if (adapter == null) {
                                adapter = new TripAdapter(onlineTrips, tripListener);
                                rvTrips.setAdapter(adapter);
                            } else {
                                adapter.setTrips(onlineTrips);
                            }
                            
                            if (onlineTrips.isEmpty()) {
                                layoutEmptyState.setVisibility(View.VISIBLE);
                            } else {
                                layoutEmptyState.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(getContext(), data.getError(), Toast.LENGTH_SHORT).show();
                            layoutEmptyState.setVisibility(View.VISIBLE);
                        }
                    } else {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<TripsResponse> call, Throwable t) {
                    if (!isAdded() || getContext() == null) return;
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Failed to fetch online trips", Toast.LENGTH_SHORT).show();
                    layoutEmptyState.setVisibility(View.VISIBLE);
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            layoutEmptyState.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "You are offline. Internet connection required.", Toast.LENGTH_SHORT).show();
        }
    }
}
