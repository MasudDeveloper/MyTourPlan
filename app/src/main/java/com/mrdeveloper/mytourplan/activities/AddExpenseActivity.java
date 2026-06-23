package com.mrdeveloper.mytourplan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.models.SyncGenericResponse;
import com.mrdeveloper.mytourplan.utils.NetworkUtils;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etCategory, etAmount, etNote;
    private Button btnSaveExpense;
    private int tripId;
    private String expenseId;
    private SharedPrefs sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        sharedPrefs = new SharedPrefs(this);
        tripId = getIntent().getIntExtra("trip_id", -1);

        if (tripId == -1) {
            Toast.makeText(this, "ত্রুটিপূর্ণ ট্যুর আইডি", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        expenseId = getIntent().getStringExtra("expense_id");

        etCategory = findViewById(R.id.etCategory);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
        
        if (expenseId != null) {
            btnSaveExpense.setText("খরচ আপডেট করুন");
            etCategory.setText(getIntent().getStringExtra("category"));
            etAmount.setText(String.valueOf(getIntent().getDoubleExtra("amount", 0.0)));
            etNote.setText(getIntent().getStringExtra("note"));
        }

        btnSaveExpense.setOnClickListener(v -> saveExpense());
    }

    private void saveExpense() {
        String category = etCategory.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (category.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "ক্যাটাগরি এবং খরচের পরিমাণ দিন", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "ইন্টারনেট সংযোগ প্রয়োজন", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveExpense.setEnabled(false);
        btnSaveExpense.setText("সংরক্ষণ করা হচ্ছে...");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = sharedPrefs.getToken();

        String action = expenseId != null ? "UPDATE" : "INSERT";
        int serverId = expenseId != null ? Integer.parseInt(expenseId) : -1;
        String createdAt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());

        apiService.syncExpense("Bearer " + token, String.valueOf(tripId), category, amount, note, createdAt, "", action, serverId).enqueue(new Callback<SyncGenericResponse>() {
            @Override
            public void onResponse(Call<SyncGenericResponse> call, Response<SyncGenericResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddExpenseActivity.this, "খরচ সেভ করা হয়েছে!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnSaveExpense.setEnabled(true);
                    btnSaveExpense.setText(expenseId != null ? "খরচ আপডেট করুন" : "সেভ করুন");
                    Toast.makeText(AddExpenseActivity.this, "খরচ সেভ করা যায়নি", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SyncGenericResponse> call, Throwable t) {
                btnSaveExpense.setEnabled(true);
                btnSaveExpense.setText(expenseId != null ? "খরচ আপডেট করুন" : "সেভ করুন");
                Toast.makeText(AddExpenseActivity.this, "ত্রুটি: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
