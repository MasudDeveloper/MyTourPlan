package com.mrdeveloper.mytourplan.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.activities.EditProfileActivity;
import com.mrdeveloper.mytourplan.activities.LoginActivity;
import com.mrdeveloper.mytourplan.database.DatabaseHelper;
import com.mrdeveloper.mytourplan.models.User;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;
import com.mrdeveloper.mytourplan.workers.SyncWorker;
import android.widget.Toast;

public class ProfileFragment extends Fragment {

    private ImageView ivProfileTop, ivProfileLarge;
    private TextView tvUserName, tvUserEmail;
    private DatabaseHelper db;
    private SharedPrefs sharedPrefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfileTop = view.findViewById(R.id.ivProfileTop);
        ivProfileLarge = view.findViewById(R.id.ivProfileLarge);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);

        db = new DatabaseHelper(getContext());
        sharedPrefs = new SharedPrefs(getContext());

        view.findViewById(R.id.tvEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        View llManualSync = view.findViewById(R.id.llManualSync);
        if (llManualSync != null) {
            llManualSync.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Sync Started", Toast.LENGTH_SHORT).show();
                SyncWorker.scheduleSync(getContext());
            });
        }

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            if (getActivity() != null) {
                sharedPrefs.clearSession();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        if (getContext() == null) return;
        int userId = sharedPrefs.getUserId();
        if (userId != -1) {
            User user = db.getUserById(userId);
            if (user != null) {
                tvUserName.setText(user.getName());
                tvUserEmail.setText(user.getEmail());

                if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
                    Glide.with(this).load(Uri.parse(user.getProfilePic())).into(ivProfileTop);
                    Glide.with(this).load(Uri.parse(user.getProfilePic())).into(ivProfileLarge);
                } else {
                    String defaultUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80";
                    Glide.with(this).load(defaultUrl).into(ivProfileTop);
                    Glide.with(this).load(defaultUrl).into(ivProfileLarge);
                }
            }
        }
    }
}
