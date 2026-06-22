package com.mrdeveloper.mytourplan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mrdeveloper.mytourplan.MainActivity;
import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        android.widget.ImageView ivSplashLogo = findViewById(R.id.ivSplashLogo);
        android.widget.TextView tvAppName = findViewById(R.id.tvAppName);
        android.widget.TextView tvTagline = findViewById(R.id.tvTagline);

        // Set initial states for animations
        ivSplashLogo.setAlpha(0f);
        ivSplashLogo.setScaleX(0.7f);
        ivSplashLogo.setScaleY(0.7f);

        tvAppName.setAlpha(0f);
        tvAppName.setTranslationY(50f);

        tvTagline.setAlpha(0f);
        tvTagline.setTranslationY(30f);

        // Start entrance animations
        ivSplashLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        tvAppName.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(250)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        tvTagline.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(450)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        SharedPrefs sharedPrefs = new SharedPrefs(this);

        // Wait for 2.2 seconds then navigate to allow animation to complete
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (sharedPrefs.isLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 2200);
    }
}
