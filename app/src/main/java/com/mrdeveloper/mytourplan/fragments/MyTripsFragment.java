package com.mrdeveloper.mytourplan.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private TripAdapter adapter;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_trips, container, false);
        
        rvTrips = view.findViewById(R.id.rvTrips);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);

        rvTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        db = new DatabaseHelper(getContext());
        
        loadTrips();

        return view;
    }

    private void loadTrips() {
        if (getContext() == null) return;
        
        // 1. Load from Local SQLite (Offline First)
        String userId = String.valueOf(new SharedPrefs(getContext()).getUserId());
        List<Trip> localTrips = db.getTripsByUser(userId);
        if (!localTrips.isEmpty()) {
            layoutEmptyState.setVisibility(View.GONE);
            adapter = new TripAdapter(localTrips);
            rvTrips.setAdapter(adapter);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }

        // 2. Fetch from Online API if network is available
        if (NetworkUtils.isNetworkAvailable(getContext())) {
            SyncWorker.scheduleSync(getContext()); // Push local changes first
            
            String token = new SharedPrefs(getContext()).getToken();
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<TripsResponse> call = apiService.getMyTrips("Bearer " + token);

            call.enqueue(new Callback<TripsResponse>() {
                @Override
                public void onResponse(Call<TripsResponse> call, Response<TripsResponse> response) {
                    if (!isAdded() || getContext() == null) return;
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        TripsResponse data = response.body();
                        if (data.getError() == null || data.getError().isEmpty()) {
                            // Update UI with fresh online data
                            List<Trip> onlineTrips = data.getTrips();
                            // Merge local trips so they don't disappear before syncing (or if they were falsely marked as synced)
                            List<Trip> tripsToKeep = new java.util.ArrayList<>();
                            for (Trip localTrip : localTrips) {
                                boolean found = false;
                                for (Trip onlineTrip : onlineTrips) {
                                    if (localTrip.getDestination().equals(onlineTrip.getDestination()) && localTrip.getStartDate().equals(onlineTrip.getStartDate())) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    tripsToKeep.add(localTrip);
                                }
                            }
                            onlineTrips.addAll(0, tripsToKeep);
                            
                            if (adapter == null) {
                                adapter = new TripAdapter(onlineTrips);
                                rvTrips.setAdapter(adapter);
                            } else {
                                adapter.setTrips(onlineTrips);
                            }
                            
                            if (onlineTrips.isEmpty() && localTrips.isEmpty()) {
                                layoutEmptyState.setVisibility(View.VISIBLE);
                            } else {
                                layoutEmptyState.setVisibility(View.GONE);
                            }
                            
                            // Optional: Save fresh online trips to SQLite here to update local cache
                        } else {
                            Toast.makeText(getContext(), data.getError(), Toast.LENGTH_SHORT).show();
                            if (localTrips.isEmpty()) layoutEmptyState.setVisibility(View.VISIBLE);
                        }
                    } else if (localTrips.isEmpty()) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<TripsResponse> call, Throwable t) {
                    if (!isAdded() || getContext() == null) return;
                    progressBar.setVisibility(View.GONE);
                    if (localTrips.isEmpty()) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "Failed to fetch trips. Check your connection.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            if (localTrips.isEmpty()) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "You are offline and have no saved trips.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
