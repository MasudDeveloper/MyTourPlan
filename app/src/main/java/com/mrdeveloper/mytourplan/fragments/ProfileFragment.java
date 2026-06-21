package com.mrdeveloper.mytourplan.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
    private TextView tvUserName, tvUserEmail, tvSyncStatus, tvSyncProgressText;
    private LinearLayout llManualSync, llSyncProgress;
    private LinearProgressIndicator progressSync;
    private DatabaseHelper db;
    private SharedPrefs sharedPrefs;
    private boolean isSyncing = false;

    private final BroadcastReceiver syncReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.mrdeveloper.mytourplan.SYNC_STATE_CHANGED".equals(intent.getAction())) {
                isSyncing = intent.getBooleanExtra("is_syncing", false);
                if (isSyncing) {
                    llSyncProgress.setVisibility(View.VISIBLE);
                    llManualSync.setEnabled(false);
                    llManualSync.setAlpha(0.5f);
                    tvSyncStatus.setText("SYNCING...");
                } else {
                    llSyncProgress.setVisibility(View.GONE);
                    llManualSync.setEnabled(true);
                    llManualSync.setAlpha(1.0f);
                    updateSyncStatus();
                }
            } else if ("com.mrdeveloper.mytourplan.SYNC_PROGRESS".equals(intent.getAction())) {
                int progress = intent.getIntExtra("progress", 0);
                progressSync.setProgressCompat(progress, true);
                tvSyncProgressText.setText("Syncing... " + progress + "%");
            }
        }
    };

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

        tvSyncStatus = view.findViewById(R.id.tvSyncStatus);
        tvSyncProgressText = view.findViewById(R.id.tvSyncProgressText);
        llManualSync = view.findViewById(R.id.llManualSync);
        llSyncProgress = view.findViewById(R.id.llSyncProgress);
        progressSync = view.findViewById(R.id.progressSync);

        if (llManualSync != null) {
            llManualSync.setOnClickListener(v -> {
                if (!isSyncing) {
                    SyncWorker.scheduleSync(getContext());
                }
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
        updateSyncStatus();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.mrdeveloper.mytourplan.SYNC_STATE_CHANGED");
        filter.addAction("com.mrdeveloper.mytourplan.SYNC_PROGRESS");
        if (getActivity() != null) {
            ContextCompat.registerReceiver(getActivity(), syncReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            try {
                getActivity().unregisterReceiver(syncReceiver);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private void updateSyncStatus() {
        if (db == null || tvSyncStatus == null) return;
        boolean hasUnsyncedData = !db.getUnsyncedTrips().isEmpty() 
                               || !db.getUnsyncedExpenses().isEmpty() 
                               || !db.getUnsyncedMembers().isEmpty();
                               
        if (hasUnsyncedData) {
            tvSyncStatus.setText("Sync Now");
            tvSyncStatus.setTextColor(0xFF007BFF); // Blue
            tvSyncStatus.setBackgroundColor(0x00000000); // Transparent
        } else {
            tvSyncStatus.setText("UPDATED");
            tvSyncStatus.setTextColor(0xFF64748B); // Slate gray
            tvSyncStatus.setBackgroundColor(0xFFF1F5F9); // Light gray
        }
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
