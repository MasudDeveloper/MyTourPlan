package com.mrdeveloper.mytourplan.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.activities.AddTripActivity;

public class PlanTripFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan_trip, container, false);
        
        view.findViewById(R.id.btnGeneratePlan).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddTripActivity.class));
        });

        view.findViewById(R.id.cvExploreTemplates).setOnClickListener(v -> {
            android.widget.Toast.makeText(getContext(), "টেমপ্লেট এক্সপ্লোরার শীঘ্রই আসছে!", android.widget.Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.cvJoinTour).setOnClickListener(v -> {
            android.widget.Toast.makeText(getContext(), "ট্যুরে যোগ দেওয়ার ফিচারটি শীঘ্রই আসছে!", android.widget.Toast.LENGTH_SHORT).show();
        });
        
        return view;
    }
}
