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
            Toast.makeText(this, "Invalid Trip", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Internet connection required", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(GroupMembersActivity.this, "Failed to load members", Toast.LENGTH_SHORT).show();
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
            View memberView = getLayoutInflater().inflate(R.layout.item_group_member, null);
            
            TextView tvName = memberView.findViewById(R.id.tvName);
            TextView tvPaidMethod = memberView.findViewById(R.id.tvPaidMethod);
            TextView tvDueRefund = memberView.findViewById(R.id.tvDueRefund);
            
            tvName.setText(member.getName());
            tvPaidMethod.setText("Paid: ৳" + member.getAmountPaid() + " (" + member.getPaymentMethod() + ")");
            
            double diff = member.getAmountPaid() - budgetPerPerson;
            if (diff < 0) {
                tvDueRefund.setText("Due: ৳" + String.format("%.2f", Math.abs(diff)));
                tvDueRefund.setTextColor(Color.parseColor("#DC2626")); // Red
            } else if (diff > 0) {
                tvDueRefund.setText("Refund: ৳" + String.format("%.2f", diff));
                tvDueRefund.setTextColor(Color.parseColor("#16A34A")); // Green
            } else {
                tvDueRefund.setText("Cleared");
                tvDueRefund.setTextColor(Color.parseColor("#64748B")); // Gray
            }
            
            memberView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                    .setTitle("Manage Member")
                    .setItems(new CharSequence[]{"Add Payment", "Edit", "Delete"}, (dialog, which) -> {
                        if (which == 0) {
                            showAddPaymentDialog(member);
                        } else if (which == 1) {
                            showAddMemberDialog(member);
                        } else if (which == 2) {
                            new AlertDialog.Builder(this)
                                .setTitle("Delete Member")
                                .setMessage("Are you sure you want to delete this member?")
                                .setPositiveButton("Yes", (d, w) -> {
                                    syncMemberAPI("DELETE", Integer.parseInt(member.getId()), "", 0, "");
                                })
                                .setNegativeButton("No", null)
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
        
        String[] methods = {"Cash", "bKash", "Nagad", "Bank Transfer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methods);
        spinnerMethod.setAdapter(adapter);
        
        if (existingMember != null) {
            etName.setText(existingMember.getName());
            etAmount.setText(String.valueOf(existingMember.getAmountPaid()));
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].equals(existingMember.getPaymentMethod())) {
                    spinnerMethod.setSelection(i);
                    break;
                }
            }
        }
        
        new AlertDialog.Builder(this)
            .setTitle(existingMember == null ? "Add Member" : "Edit Member")
            .setView(view)
            .setPositiveButton("Save", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                String amountStr = etAmount.getText().toString().trim();
                String method = spinnerMethod.getSelectedItem().toString();
                
                if (name.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                double amount = Double.parseDouble(amountStr);
                
                if (existingMember == null) {
                    syncMemberAPI("INSERT", -1, name, amount, method);
                } else {
                    syncMemberAPI("UPDATE", Integer.parseInt(existingMember.getId()), name, amount, method);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void syncMemberAPI(String action, int serverId, String name, double amountPaid, String paymentMethod) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Internet connection required", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = sharedPrefs.getToken();

        apiService.syncMember("Bearer " + token, tripId, name, amountPaid, paymentMethod, "", action, serverId).enqueue(new Callback<SyncGenericResponse>() {
            @Override
            public void onResponse(Call<SyncGenericResponse> call, Response<SyncGenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadMembersData();
                } else {
                    Toast.makeText(GroupMembersActivity.this, "Failed to sync member", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SyncGenericResponse> call, Throwable t) {
                Toast.makeText(GroupMembersActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        
        String[] methods = {"Cash", "bKash", "Nagad", "Bank Transfer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methods);
        spinnerMethod.setAdapter(adapter);
        
        new AlertDialog.Builder(this)
            .setTitle("Add Payment")
            .setView(view)
            .setPositiveButton("Add", (dialog, which) -> {
                String amountStr = etAmount.getText().toString().trim();
                String method = spinnerMethod.getSelectedItem().toString();
                
                if (amountStr.isEmpty()) {
                    Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                double addedAmount = Double.parseDouble(amountStr);
                double newTotal = member.getAmountPaid() + addedAmount;
                
                syncMemberAPI("UPDATE", Integer.parseInt(member.getId()), member.getName(), newTotal, method);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
