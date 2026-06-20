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
import com.mrdeveloper.mytourplan.database.DatabaseHelper;
import com.mrdeveloper.mytourplan.models.User;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivEditProfilePic;
    private EditText etProfileName, etProfilePhone, etProfileEmail, etTravelBio;
    private Button btnSaveProfile;

    private DatabaseHelper db;
    private SharedPrefs sharedPrefs;
    private Uri selectedImageUri;
    private User currentUser;

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

        db = new DatabaseHelper(this);
        sharedPrefs = new SharedPrefs(this);

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
        int userId = sharedPrefs.getUserId();
        if (userId != -1) {
            currentUser = db.getUserById(userId);
            if (currentUser != null) {
                etProfileName.setText(currentUser.getName());
                etProfileEmail.setText(currentUser.getEmail());
                etProfilePhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
                etTravelBio.setText("Lover of beaches and mountains. Exploring the beauty of Bangladesh.");

                if (currentUser.getProfilePic() != null && !currentUser.getProfilePic().isEmpty()) {
                    selectedImageUri = Uri.parse(currentUser.getProfilePic());
                    Glide.with(this).load(selectedImageUri).into(ivEditProfilePic);
                } else {
                    Glide.with(this).load("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80").into(ivEditProfilePic);
                }
            }
        }
    }

    private void saveProfile() {
        if (currentUser == null) return;

        String name = etProfileName.getText().toString().trim();
        String phone = etProfilePhone.getText().toString().trim();
        String picUriStr = selectedImageUri != null ? selectedImageUri.toString() : currentUser.getProfilePic();

        if (name.isEmpty()) {
            etProfileName.setError("Name is required");
            return;
        }

        boolean success = db.updateUserProfile(currentUser.getId(), name, phone, picUriStr);
        if (success) {
            Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
}
