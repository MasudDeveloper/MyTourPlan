package com.mrdeveloper.mytourplan.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.activities.EditProfileActivity;
import com.mrdeveloper.mytourplan.activities.LoginActivity;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.models.ProfileResponse;
import com.mrdeveloper.mytourplan.utils.NetworkUtils;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ImageView ivProfileTop, ivProfileLarge;
    private TextView tvUserName, tvUserEmail, tvSyncStatus, tvSyncProgressText;
    private LinearLayout llManualSync, llSyncProgress;
    private LinearProgressIndicator progressSync;
    private SharedPrefs sharedPrefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfileTop = view.findViewById(R.id.ivProfileTop);
        ivProfileLarge = view.findViewById(R.id.ivProfileLarge);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);

        sharedPrefs = new SharedPrefs(getContext());

        // Load cached credentials instantly for a seamless, dynamic feel
        if (sharedPrefs.isLoggedIn()) {
            tvUserName.setText(sharedPrefs.getUserName());
            tvUserEmail.setText(sharedPrefs.getUserEmail() != null ? sharedPrefs.getUserEmail() : "");
            String cachedPic = sharedPrefs.getProfilePic();
            if (cachedPic != null && !cachedPic.isEmpty()) {
                if (ivProfileTop != null) {
                    Glide.with(this).load(Uri.parse(cachedPic)).into(ivProfileTop);
                }
                Glide.with(this).load(Uri.parse(cachedPic)).into(ivProfileLarge);
            } else {
                if (ivProfileTop != null) {
                    Glide.with(this).load(R.drawable.ic_profile).into(ivProfileTop);
                }
                Glide.with(this).load(R.drawable.ic_profile).into(ivProfileLarge);
            }
        }

        view.findViewById(R.id.tvEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        tvSyncStatus = view.findViewById(R.id.tvSyncStatus);
        tvSyncProgressText = view.findViewById(R.id.tvSyncProgressText);
        llManualSync = view.findViewById(R.id.llManualSync);
        llSyncProgress = view.findViewById(R.id.llSyncProgress);
        if (llManualSync != null) {
            llManualSync.setVisibility(View.GONE);
        }
        if (llSyncProgress != null) {
            llSyncProgress.setVisibility(View.GONE);
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
        if (getContext() == null || !NetworkUtils.isNetworkAvailable(getContext())) {
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = sharedPrefs.getToken();

        apiService.getProfile("Bearer " + token).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse data = response.body();
                    if (data.getError() == null || data.getError().isEmpty()) {
                        tvUserName.setText(data.getName());
                        tvUserEmail.setText(data.getEmail());
                        
                        sharedPrefs.saveUserSession(token, data.getId(), data.getName(), data.getEmail(), data.getPhone());

                        if (data.getProfilePic() != null && !data.getProfilePic().isEmpty()) {
                            sharedPrefs.saveProfilePic(data.getProfilePic());
                            if (ivProfileTop != null) {
                                Glide.with(ProfileFragment.this).load(Uri.parse(data.getProfilePic())).into(ivProfileTop);
                            }
                            Glide.with(ProfileFragment.this).load(Uri.parse(data.getProfilePic())).into(ivProfileLarge);
                        } else {
                            sharedPrefs.saveProfilePic(null);
                            if (ivProfileTop != null) {
                                Glide.with(ProfileFragment.this).load(R.drawable.ic_profile).into(ivProfileTop);
                            }
                            Glide.with(ProfileFragment.this).load(R.drawable.ic_profile).into(ivProfileLarge);
                        }
                    } else {
                        Toast.makeText(getContext(), data.getError(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Failed to sync profile data (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Sync error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
