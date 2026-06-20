package com.mrdeveloper.mytourplan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.database.DatabaseHelper;
import com.mrdeveloper.mytourplan.models.Expense;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etCategory, etAmount, etNote;
    private Button btnSaveExpense;
    private DatabaseHelper db;
    private int tripId;
    private String expenseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        tripId = getIntent().getIntExtra("trip_id", -1);

        if (tripId == -1) {
            Toast.makeText(this, "Invalid Trip ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        expenseId = getIntent().getStringExtra("expense_id");

        etCategory = findViewById(R.id.etCategory);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
        
        if (expenseId != null) {
            btnSaveExpense.setText("Update Expense");
            Expense existingExp = db.getExpenseById(expenseId);
            if (existingExp != null) {
                etCategory.setText(existingExp.getCategory());
                etAmount.setText(String.valueOf(existingExp.getAmount()));
                etNote.setText(existingExp.getNote());
            }
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

        Expense exp = new Expense();
        exp.setTripId(String.valueOf(tripId));
        exp.setCategory(category);
        exp.setAmount(amount);
        exp.setNote(note);

        if (expenseId != null) {
            exp.setId(expenseId);
            db.updateExpenseLocally(exp);
            Toast.makeText(this, "খরচ আপডেট করা হয়েছে!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            long result = db.addExpenseLocally(exp);
            if (result != -1) {
                Toast.makeText(this, "খরচ যোগ করা হয়েছে!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "খরচ যোগ করা যায়নি", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
