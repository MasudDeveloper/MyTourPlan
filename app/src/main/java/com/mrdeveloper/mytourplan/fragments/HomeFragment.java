package com.mrdeveloper.mytourplan.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.models.DashboardResponse;
import com.mrdeveloper.mytourplan.models.Trip;
import com.mrdeveloper.mytourplan.models.User;
import com.mrdeveloper.mytourplan.utils.NetworkUtils;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView tvWelcome, tvUpcomingDest, tvUpcomingDates, tvUpcomingTime, tvUpcomingMembers, tvUpcomingBudget;
    private ImageView ivProfileTop, ivUpcomingTrip;
    private SharedPrefs sharedPrefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        sharedPrefs = new SharedPrefs(getContext());

        ivProfileTop = view.findViewById(R.id.ivProfileTop);
        ImageView ivHeroImage = view.findViewById(R.id.ivHeroImage);
        ivUpcomingTrip = view.findViewById(R.id.ivUpcomingTrip);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvUpcomingDest = view.findViewById(R.id.tvUpcomingDest);
        tvUpcomingDates = view.findViewById(R.id.tvUpcomingDates);
        tvUpcomingTime = view.findViewById(R.id.tvUpcomingTime);
        tvUpcomingMembers = view.findViewById(R.id.tvUpcomingMembers);
        tvUpcomingBudget = view.findViewById(R.id.tvUpcomingBudget);

        TextView tvViewAllUpcoming = view.findViewById(R.id.tvViewAllUpcoming);
        View cvMyTrips = view.findViewById(R.id.cvMyTrips);

        View.OnClickListener goToMyTrips = v -> {
            if (getActivity() instanceof com.mrdeveloper.mytourplan.MainActivity) {
                ((com.mrdeveloper.mytourplan.MainActivity) getActivity()).switchToMyTrips();
            }
        };

        if (tvViewAllUpcoming != null) tvViewAllUpcoming.setOnClickListener(goToMyTrips);
        if (cvMyTrips != null) cvMyTrips.setOnClickListener(goToMyTrips);

        if (getContext() != null) {
            Glide.with(this)
                 .load("https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?auto=format&fit=crop&w=800&q=80")
                 .into(ivHeroImage);
        }

        view.findViewById(R.id.btnStartPlanning).setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                nav.setSelectedItemId(R.id.nav_plan_trip);
            }
        });

        setupQuickActions(view);

        return view;
    }

    private void setupQuickActions(View view) {
        view.findViewById(R.id.cvMyTrips).setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                nav.setSelectedItemId(R.id.nav_my_trips);
            }
        });

        view.findViewById(R.id.cvExplore).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Explore feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.cvSaved).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Saved feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        if (getContext() == null) return;

        if (tvWelcome != null) tvWelcome.setText("Welcome back"); // Fallback text

        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            Toast.makeText(getContext(), "You are offline.", Toast.LENGTH_SHORT).show();
            showEmptyUpcomingTrip();
            return;
        }

        String token = sharedPrefs.getToken();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getDashboard("Bearer " + token).enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (!isAdded() || getContext() == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse data = response.body();
                    
                    if (data.getError() == null || data.getError().isEmpty()) {
                        if (tvWelcome != null) tvWelcome.setText("Welcome back, " + data.getUserName());
                        // Assume profile pic logic can be added to backend later, for now placeholder
                        if (ivProfileTop != null) {
                            Glide.with(HomeFragment.this).load("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80").into(ivProfileTop);
                        }

                        Trip upcoming = data.getUpcomingTrip();
                        if (upcoming != null && upcoming.getId() != null) {
                            if (tvUpcomingDest != null) tvUpcomingDest.setText(upcoming.getDestination());
                            if (tvUpcomingDates != null) tvUpcomingDates.setText(upcoming.getStartDate() + " to " + upcoming.getEndDate());
                            if (tvUpcomingMembers != null) tvUpcomingMembers.setText(String.valueOf(upcoming.getMembersCount()) + " Members");
                            double totalBudget = upcoming.getBudget() * upcoming.getMembersCount();
                            if (tvUpcomingBudget != null) tvUpcomingBudget.setText(String.format("৳%.2f", totalBudget));
                            if (tvUpcomingTime != null) tvUpcomingTime.setText("08:00 AM"); 
                            
                            if (upcoming.getImageUri() != null && !upcoming.getImageUri().isEmpty() && ivUpcomingTrip != null) {
                                Glide.with(HomeFragment.this).load(Uri.parse(upcoming.getImageUri())).into(ivUpcomingTrip);
                            } else if (ivUpcomingTrip != null) {
                                Glide.with(HomeFragment.this).load("https://images.unsplash.com/photo-1540206351-d7ce9f1ea280?auto=format&fit=crop&w=800&q=80").into(ivUpcomingTrip);
                            }

                            if (ivUpcomingTrip != null) {
                                ivUpcomingTrip.setOnClickListener(v -> {
                                    Intent intent = new Intent(getActivity(), com.mrdeveloper.mytourplan.activities.TripDashboardActivity.class);
                                    try {
                                        intent.putExtra("trip_id", Integer.parseInt(upcoming.getId()));
                                        startActivity(intent);
                                    } catch (NumberFormatException e) {}
                                });
                            }
                        } else {
                            showEmptyUpcomingTrip();
                        }
                    } else {
                        showEmptyUpcomingTrip();
                    }
                } else {
                    showEmptyUpcomingTrip();
                }
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                showEmptyUpcomingTrip();
            }
        });
    }

    private void showEmptyUpcomingTrip() {
        if (tvUpcomingDest != null) tvUpcomingDest.setText("No upcoming trips");
        if (tvUpcomingDates != null) tvUpcomingDates.setText("Plan your next adventure!");
        if (ivUpcomingTrip != null) {
            Glide.with(this).load("https://images.unsplash.com/photo-1540206351-d7ce9f1ea280?auto=format&fit=crop&w=800&q=80").into(ivUpcomingTrip);
            ivUpcomingTrip.setOnClickListener(null);
        }
    }
}
