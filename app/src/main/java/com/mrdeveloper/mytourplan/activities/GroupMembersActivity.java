package com.mrdeveloper.mytourplan.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.models.MembersResponse;
import com.mrdeveloper.mytourplan.models.SyncGenericResponse;
import com.mrdeveloper.mytourplan.models.TripMember;
import com.mrdeveloper.mytourplan.utils.NetworkUtils;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class GroupMembersActivity extends AppCompatActivity {

    private SharedPrefs sharedPrefs;
    private String tripId;
    private double budgetPerPerson;
    
    private LinearLayout layoutMemberList;
    private TextView tvBudgetPerPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPrefs = new SharedPrefs(this);
        tripId = getIntent().getStringExtra("trip_id");
        
        if (tripId == null) {
            Toast.makeText(this, "ত্রুটিপূর্ণ ট্যুর তথ্য", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        layoutMemberList = findViewById(R.id.layoutMemberList);
        tvBudgetPerPerson = findViewById(R.id.tvBudgetPerPerson);
        
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        
        ImageView btnAddMember = findViewById(R.id.btnAddMember);
        btnAddMember.setOnClickListener(v -> showAddMemberDialog(null));
        
        loadMembersData();
    }
    
    private void loadMembersData() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "ইন্টারনেট সংযোগ প্রয়োজন", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = sharedPrefs.getToken();

        apiService.getMembers("Bearer " + token, tripId).enqueue(new Callback<MembersResponse>() {
            @Override
            public void onResponse(Call<MembersResponse> call, Response<MembersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MembersResponse data = response.body();
                    if (data.getError() == null || data.getError().isEmpty()) {
                        budgetPerPerson = data.getBudgetPerPerson();
                        tvBudgetPerPerson.setText("৳" + String.format("%.2f", budgetPerPerson));
                        
                        populateMembersList(data.getMembers());
                    } else {
                        Toast.makeText(GroupMembersActivity.this, data.getError(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MembersResponse> call, Throwable t) {
                Toast.makeText(GroupMembersActivity.this, "মেম্বার লোড করা যায়নি", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateMembersList(List<TripMember> members) {
        layoutMemberList.removeAllViews();
        
        if (members == null || members.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("কোনো মেম্বার যোগ করা হয়নি।");
            empty.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layoutMemberList.addView(empty);
            return;
        }

        for (TripMember member : members) {
            View memberView = getLayoutInflater().inflate(R.layout.item_group_member, layoutMemberList, false);
            
            TextView tvName = memberView.findViewById(R.id.tvName);
            TextView tvPaidMethod = memberView.findViewById(R.id.tvPaidMethod);
            TextView tvDueRefund = memberView.findViewById(R.id.tvDueRefund);
            TextView tvInitials = memberView.findViewById(R.id.tvInitials);
            TextView tvPercentage = memberView.findViewById(R.id.tvPercentage);
            android.widget.ProgressBar progressBar = memberView.findViewById(R.id.progressBar);
            
            tvName.setText(member.getName());
            tvPaidMethod.setText("পরিশোধিত: ৳" + String.format("%.2f", member.getAmountPaid()) + " (" + getPaymentMethodBn(member.getPaymentMethod()) + ")");
            
            // Set Initials Avatar
            String name = member.getName();
            String initials = "MB";
            if (name != null && !name.trim().isEmpty()) {
                String[] parts = name.trim().split("\\s+");
                if (parts.length >= 2) {
                    initials = (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
                } else {
                    initials = name.trim().substring(0, Math.min(2, name.trim().length())).toUpperCase();
                }
            }
            tvInitials.setText(initials);
            
            // Set Progress and Percentage
            int percentage = budgetPerPerson > 0 ? (int) ((member.getAmountPaid() / budgetPerPerson) * 100) : 0;
            tvPercentage.setText(percentage + "%");
            progressBar.setProgress(Math.min(percentage, 100));
            
            double diff = member.getAmountPaid() - budgetPerPerson;
            if (diff < 0) {
                tvDueRefund.setText("বাকি: ৳" + String.format("%.2f", Math.abs(diff)));
                int redColor = Color.parseColor("#DC2626");
                tvDueRefund.setTextColor(redColor);
                tvPercentage.setTextColor(redColor);
                progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(redColor));
            } else if (diff > 0) {
                tvDueRefund.setText("ফেরত: ৳" + String.format("%.2f", diff));
                int greenColor = Color.parseColor("#16A34A");
                tvDueRefund.setTextColor(greenColor);
                tvPercentage.setTextColor(greenColor);
                progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(greenColor));
            } else {
                tvDueRefund.setText("পরিশোধিত");
                int primaryColor = Color.parseColor("#0056D2");
                tvDueRefund.setTextColor(primaryColor);
                tvPercentage.setTextColor(primaryColor);
                progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(primaryColor));
            }
            
            memberView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                    .setTitle("মেম্বার পরিচালনা")
                    .setItems(new CharSequence[]{"পেমেন্ট যোগ করুন", "সম্পাদনা করুন", "মুছে ফেলুন"}, (dialog, which) -> {
                        if (which == 0) {
                            showAddPaymentDialog(member);
                        } else if (which == 1) {
                            showAddMemberDialog(member);
                        } else if (which == 2) {
                            new AlertDialog.Builder(this)
                                .setTitle("মেম্বার মুছে ফেলুন")
                                .setMessage("আপনি কি নিশ্চিতভাবে এই মেম্বারকে মুছে ফেলতে চান?")
                                .setPositiveButton("হ্যাঁ", (d, w) -> {
                                    syncMemberAPI("DELETE", Integer.parseInt(member.getId()), "", 0, "");
                                })
                                .setNegativeButton("না", null)
                                .show();
                        }
                    })
                    .show();
                return true;
            });
            
            layoutMemberList.addView(memberView);
        }
    }
    
    private void showAddMemberDialog(TripMember existingMember) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_member, null);
        EditText etName = view.findViewById(R.id.etName);
        EditText etAmount = view.findViewById(R.id.etAmount);
        Spinner spinnerMethod = view.findViewById(R.id.spinnerMethod);
        
        String[] methodsBn = {"ক্যাশ", "বিকাশ", "নগদ", "ব্যাংক ট্রান্সফার"};
        String[] methodsEn = {"Cash", "bKash", "Nagad", "Bank Transfer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methodsBn);
        spinnerMethod.setAdapter(adapter);
        
        if (existingMember != null) {
            etName.setText(existingMember.getName());
            etAmount.setText(String.valueOf(existingMember.getAmountPaid()));
            for (int i = 0; i < methodsEn.length; i++) {
                if (methodsEn[i].equalsIgnoreCase(existingMember.getPaymentMethod())) {
                    spinnerMethod.setSelection(i);
                    break;
                }
            }
        }
        
        new AlertDialog.Builder(this)
            .setTitle(existingMember == null ? "মেম্বার যোগ করুন" : "মেম্বার সংশোধন")
            .setView(view)
            .setPositiveButton("সংরক্ষণ", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                String amountStr = etAmount.getText().toString().trim();
                int selectedPos = spinnerMethod.getSelectedItemPosition();
                String method = methodsEn[selectedPos >= 0 && selectedPos < methodsEn.length ? selectedPos : 0];
                
                if (name.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(this, "সবগুলো ফিল্ড পূরণ করুন", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                double amount = Double.parseDouble(amountStr);
                
                if (existingMember == null) {
                    syncMemberAPI("INSERT", -1, name, amount, method);
                } else {
                    syncMemberAPI("UPDATE", Integer.parseInt(existingMember.getId()), name, amount, method);
                }
            })
            .setNegativeButton("বাতিল", null)
            .show();
    }

    private void syncMemberAPI(String action, int serverId, String name, double amountPaid, String paymentMethod) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "ইন্টারনেট সংযোগ প্রয়োজন", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = sharedPrefs.getToken();

        apiService.syncMember("Bearer " + token, tripId, name, amountPaid, paymentMethod, "", action, serverId).enqueue(new Callback<SyncGenericResponse>() {
            @Override
            public void onResponse(Call<SyncGenericResponse> call, Response<SyncGenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SyncGenericResponse body = response.body();
                    if (body.isSuccess()) {
                        loadMembersData();
                    } else {
                        String errorMsg = body.getError() != null && !body.getError().isEmpty() ? body.getError() : "মেম্বার সিঙ্ক করা যায়নি";
                        Toast.makeText(GroupMembersActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GroupMembersActivity.this, "মেম্বার সিঙ্ক করা যায়নি", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SyncGenericResponse> call, Throwable t) {
                Toast.makeText(GroupMembersActivity.this, "ত্রুটি: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddPaymentDialog(TripMember member) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_member, null);
        EditText etName = view.findViewById(R.id.etName);
        EditText etAmount = view.findViewById(R.id.etAmount);
        Spinner spinnerMethod = view.findViewById(R.id.spinnerMethod);
        
        etName.setText(member.getName());
        etName.setEnabled(false); // Can't change name here
        
        String[] methodsBn = {"ক্যাশ", "বিকাশ", "নগদ", "ব্যাংক ট্রান্সফার"};
        String[] methodsEn = {"Cash", "bKash", "Nagad", "Bank Transfer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methodsBn);
        spinnerMethod.setAdapter(adapter);
        
        new AlertDialog.Builder(this)
            .setTitle("পেমেন্ট যোগ করুন")
            .setView(view)
            .setPositiveButton("যোগ করুন", (dialog, which) -> {
                String amountStr = etAmount.getText().toString().trim();
                int selectedPos = spinnerMethod.getSelectedItemPosition();
                String method = methodsEn[selectedPos >= 0 && selectedPos < methodsEn.length ? selectedPos : 0];
                
                if (amountStr.isEmpty()) {
                    Toast.makeText(this, "দয়া করে টাকার পরিমাণ লিখুন", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                double addedAmount = Double.parseDouble(amountStr);
                double newTotal = member.getAmountPaid() + addedAmount;
                
                syncMemberAPI("UPDATE", Integer.parseInt(member.getId()), member.getName(), newTotal, method);
            })
            .setNegativeButton("বাতিল", null)
            .show();
    }

    private String getPaymentMethodBn(String englishMethod) {
        if (englishMethod == null) return "";
        switch (englishMethod.toLowerCase()) {
            case "cash": return "ক্যাশ";
            case "bkash": return "বিকাশ";
            case "nagad": return "নগদ";
            case "bank transfer": return "ব্যাংক ট্রান্সফার";
            default: return englishMethod;
        }
    }
}
