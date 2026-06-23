package com.mrdeveloper.mytourplan.activities;

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

import com.bumptech.glide.Glide;
import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.models.ProfileResponse;
import com.mrdeveloper.mytourplan.models.SyncGenericResponse;
import com.mrdeveloper.mytourplan.utils.NetworkUtils;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.widget.Toolbar;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivEditProfilePic;
    private EditText etProfileName, etProfilePhone, etProfileEmail, etTravelBio;
    private Button btnSaveProfile;

    private SharedPrefs sharedPrefs;
    private Uri selectedImageUri;
    private ProfileResponse currentUser;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).into(ivEditProfilePic);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sharedPrefs = new SharedPrefs(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ivEditProfilePic = findViewById(R.id.ivEditProfilePic);
        etProfileName = findViewById(R.id.etProfileName);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etTravelBio = findViewById(R.id.etTravelBio);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        loadUserData();

        ivEditProfilePic.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        // Load cached values instantly for high-end offline responsiveness
        etProfileName.setText(sharedPrefs.getUserName() != null ? sharedPrefs.getUserName() : "");
        etProfileEmail.setText(sharedPrefs.getUserEmail() != null ? sharedPrefs.getUserEmail() : "");
        etProfilePhone.setText(sharedPrefs.getUserPhone() != null ? sharedPrefs.getUserPhone() : "");
        etTravelBio.setText("সমুদ্র ও পাহাড়প্রেমী। বাংলাদেশের সৌন্দর্য উপভোগ করছি।");

        String cachedPic = sharedPrefs.getProfilePic();
        if (cachedPic != null && !cachedPic.isEmpty()) {
            Glide.with(this).load(Uri.parse(cachedPic)).into(ivEditProfilePic);
        } else {
            Glide.with(this).load(R.drawable.ic_profile).into(ivEditProfilePic);
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            return; // Fail silently, keeping cached data intact
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = sharedPrefs.getToken();

        apiService.getProfile("Bearer " + token).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    etProfileName.setText(currentUser.getName());
                    etProfileEmail.setText(currentUser.getEmail());
                    etProfilePhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");

                    // Update local cache
                    sharedPrefs.saveUserSession(token, currentUser.getId(), currentUser.getName(), currentUser.getEmail(), currentUser.getPhone());

                    if (currentUser.getProfilePic() != null && !currentUser.getProfilePic().isEmpty()) {
                        sharedPrefs.saveProfilePic(currentUser.getProfilePic());
                        Glide.with(EditProfileActivity.this).load(Uri.parse(currentUser.getProfilePic())).into(ivEditProfilePic);
                    } else {
                        sharedPrefs.saveProfilePic(null);
                        Glide.with(EditProfileActivity.this).load(R.drawable.ic_profile).into(ivEditProfilePic);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                // Fail silently since cache data is already loaded
            }
        });
    }

    private void saveProfile() {
        String name = etProfileName.getText().toString().trim();
        String phone = etProfilePhone.getText().toString().trim();

        if (name.isEmpty()) {
            etProfileName.setError("নাম আবশ্যক");
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "প্রোফাইল পরিবর্তন সংরক্ষণ করার জন্য ইন্টারনেট সংযোগ প্রয়োজন", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("সংরক্ষণ করা হচ্ছে...");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = sharedPrefs.getToken();

        RequestBody namePart = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody phonePart = RequestBody.create(MediaType.parse("text/plain"), phone);
        MultipartBody.Part imagePart = null;

        if (selectedImageUri != null) {
            try {
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                File file = new File(getCacheDir(), "profile.jpg");
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                is.close();
                
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                imagePart = MultipartBody.Part.createFormData("profile_pic", file.getName(), requestFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        apiService.updateProfile("Bearer " + token, namePart, phonePart, imagePart).enqueue(new Callback<SyncGenericResponse>() {
            @Override
            public void onResponse(Call<SyncGenericResponse> call, Response<SyncGenericResponse> response) {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("সংরক্ষণ করুন");
                if (response.isSuccessful() && response.body() != null) {
                    SyncGenericResponse data = response.body();
                    if (data.isSuccess()) {
                        Toast.makeText(EditProfileActivity.this, "প্রোফাইল সফলভাবে আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show();
                        
                        // Cache updated info locally in SharedPrefs instantly
                        sharedPrefs.saveUserSession(token, sharedPrefs.getUserId(), name, sharedPrefs.getUserEmail(), phone);
                        if (data.getProfilePic() != null && !data.getProfilePic().isEmpty()) {
                            sharedPrefs.saveProfilePic(data.getProfilePic());
                        }
                        
                        finish();
                    } else {
                        String error = data.getError() != null && !data.getError().isEmpty() ? data.getError() : "প্রোফাইল আপডেট করা যায়নি";
                        Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "প্রোফাইল আপডেট ব্যর্থ হয়েছে (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(EditProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SyncGenericResponse> call, Throwable t) {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("সংরক্ষণ করুন");
                Toast.makeText(EditProfileActivity.this, "ত্রুটি: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
