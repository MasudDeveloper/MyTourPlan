package com.mrdeveloper.mytourplan.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.adapters.TripChecklistAdapter;
import com.mrdeveloper.mytourplan.adapters.TripNotesAdapter;
import com.mrdeveloper.mytourplan.api.ApiClient;
import com.mrdeveloper.mytourplan.api.ApiService;
import com.mrdeveloper.mytourplan.models.SyncGenericResponse;
import com.mrdeveloper.mytourplan.models.TripChecklistItem;
import com.mrdeveloper.mytourplan.models.TripNote;
import com.mrdeveloper.mytourplan.models.TripNotesResponse;
import com.mrdeveloper.mytourplan.utils.NetworkUtils;
import com.mrdeveloper.mytourplan.utils.SharedPrefs;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripNotesActivity extends AppCompatActivity {

    private String tripId;
    private SharedPrefs sharedPrefs;
    private ApiService apiService;

    private TabLayout tabLayout;
    private View containerNotes, containerChecklist;
    private TextView tvNotesEmpty, tvChecklistEmpty;
    private FloatingActionButton fabAddNotes;

    private RecyclerView rvNotes, rvChecklist;
    private TripNotesAdapter notesAdapter;
    private TripChecklistAdapter checklistAdapter;

    private List<TripNote> notesList = new ArrayList<>();
    private List<TripChecklistItem> checklistList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_notes);

        sharedPrefs = new SharedPrefs(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        tripId = getIntent().getStringExtra("trip_id");
        if (tripId == null) {
            Toast.makeText(this, "ট্যুর আইডি পাওয়া যায়নি!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("নোট ও চেকলিস্ট");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // UI Components
        tabLayout = findViewById(R.id.tabLayout);
        containerNotes = findViewById(R.id.containerNotes);
        containerChecklist = findViewById(R.id.containerChecklist);
        tvNotesEmpty = findViewById(R.id.tvNotesEmpty);
        tvChecklistEmpty = findViewById(R.id.tvChecklistEmpty);
        fabAddNotes = findViewById(R.id.fabAddNotes);

        rvNotes = findViewById(R.id.rvNotes);
        rvChecklist = findViewById(R.id.rvChecklist);

        // Setup Recycler Views
        setupRecyclerViews();

        // TabLayout switcher
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    containerNotes.setVisibility(View.VISIBLE);
                    containerChecklist.setVisibility(View.GONE);
                    checkEmptyStates();
                } else {
                    containerNotes.setVisibility(View.GONE);
                    containerChecklist.setVisibility(View.VISIBLE);
                    checkEmptyStates();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // FAB Click listener
        fabAddNotes.setOnClickListener(v -> {
            if (tabLayout.getSelectedTabPosition() == 0) {
                showAddNoteDialog(null);
            } else {
                showAddChecklistDialog();
            }
        });

        // Load Initial Data
        loadData();
    }

    private void setupRecyclerViews() {
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        notesAdapter = new TripNotesAdapter(notesList, new TripNotesAdapter.OnNoteActionListener() {
            @Override
            public void onEditNote(TripNote note) {
                showAddNoteDialog(note);
            }

            @Override
            public void onDeleteNote(TripNote note) {
                showDeleteConfirmationDialog(note);
            }
        });
        rvNotes.setAdapter(notesAdapter);

        rvChecklist.setLayoutManager(new LinearLayoutManager(this));
        checklistAdapter = new TripChecklistAdapter(checklistList, new TripChecklistAdapter.OnChecklistActionListener() {
            @Override
            public void onToggleChecklist(TripChecklistItem item, boolean isChecked) {
                syncChecklistItem(item, "TOGGLE", isChecked ? 1 : 0);
            }

            @Override
            public void onDeleteChecklist(TripChecklistItem item) {
                showDeleteChecklistConfirmationDialog(item);
            }
        });
        rvChecklist.setAdapter(checklistAdapter);
    }

    private void loadData() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "আপনি অফলাইনে আছেন!", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sharedPrefs.getToken();
        apiService.getNotesChecklist("Bearer " + token, tripId).enqueue(new Callback<TripNotesResponse>() {
            @Override
            public void onResponse(Call<TripNotesResponse> call, Response<TripNotesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TripNotesResponse data = response.body();
                    if (data.isSuccess()) {
                        notesList = data.getNotes();
                        checklistList = data.getChecklist();

                        notesAdapter.setNotes(notesList);
                        checklistAdapter.setItems(checklistList);

                        checkEmptyStates();
                    } else {
                        Toast.makeText(TripNotesActivity.this, "ডেটা লোড করা যায়নি: " + data.getError(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TripNotesActivity.this, "সার্ভার রেসপন্স পাওয়া যায়নি!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TripNotesResponse> call, Throwable t) {
                Toast.makeText(TripNotesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkEmptyStates() {
        if (tabLayout.getSelectedTabPosition() == 0) {
            if (notesList == null || notesList.isEmpty()) {
                tvNotesEmpty.setVisibility(View.VISIBLE);
            } else {
                tvNotesEmpty.setVisibility(View.GONE);
            }
            tvChecklistEmpty.setVisibility(View.GONE);
        } else {
            if (checklistList == null || checklistList.isEmpty()) {
                tvChecklistEmpty.setVisibility(View.VISIBLE);
            } else {
                tvChecklistEmpty.setVisibility(View.GONE);
            }
            tvNotesEmpty.setVisibility(View.GONE);
        }
    }

    // --- Notes CRUD Dialogs ---

    private void showAddNoteDialog(TripNote existingNote) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_note, null);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etNoteTitle = dialogView.findViewById(R.id.etNoteTitle);
        EditText etNoteContent = dialogView.findViewById(R.id.etNoteContent);

        if (existingNote != null) {
            tvDialogTitle.setText("নোট আপডেট করুন");
            etNoteTitle.setText(existingNote.getTitle());
            etNoteContent.setText(existingNote.getContent());
        }

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("সেভ করুন", (dialog, which) -> {
                    String title = etNoteTitle.getText().toString().trim();
                    String content = etNoteContent.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "শিরোনাম প্রয়োজন!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (existingNote == null) {
                        syncNote(null, "INSERT", title, content);
                    } else {
                        existingNote.setTitle(title);
                        existingNote.setContent(content);
                        syncNote(existingNote, "UPDATE", title, content);
                    }
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void showDeleteConfirmationDialog(TripNote note) {
        new AlertDialog.Builder(this)
                .setTitle("নোট মুছে ফেলুন")
                .setMessage("আপনি কি নিশ্চিতভাবে এই নোটটি মুছে ফেলতে চান?")
                .setPositiveButton("হ্যাঁ", (dialog, which) -> syncNote(note, "DELETE", "", ""))
                .setNegativeButton("না", null)
                .show();
    }

    private void syncNote(TripNote note, String action, String title, String content) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "ইন্টারনেট সংযোগ নেই!", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sharedPrefs.getToken();
        int noteId = (note != null) ? note.getId() : -1;

        apiService.syncNotesChecklist("Bearer " + token, tripId, "NOTE", action, noteId, title, content, 0)
                .enqueue(new Callback<SyncGenericResponse>() {
                    @Override
                    public void onResponse(Call<SyncGenericResponse> call, Response<SyncGenericResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            SyncGenericResponse res = response.body();
                            if (res.isSuccess()) {
                                Toast.makeText(TripNotesActivity.this, "নোট সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show();
                                loadData();
                            } else {
                                Toast.makeText(TripNotesActivity.this, "ব্যর্থ হয়েছে: " + res.getError(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(TripNotesActivity.this, "সার্ভার সিঙ্ক ব্যর্থ হয়েছে!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<SyncGenericResponse> call, Throwable t) {
                        Toast.makeText(TripNotesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- Checklist CRUD Dialogs ---

    private void showAddChecklistDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_checklist, null);
        EditText etChecklistTitle = dialogView.findViewById(R.id.etChecklistTitle);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("যোগ করুন", (dialog, which) -> {
                    String title = etChecklistTitle.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "চেকলিস্টের নাম লিখুন!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    TripChecklistItem item = new TripChecklistItem(-1, tripId, title, 0);
                    syncChecklistItem(item, "INSERT", 0);
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void showDeleteChecklistConfirmationDialog(TripChecklistItem item) {
        new AlertDialog.Builder(this)
                .setTitle("চেকলিস্ট ডিলিট করুন")
                .setMessage("আপনি কি নিশ্চিতভাবে এই আইটেমটি মুছে ফেলতে চান?")
                .setPositiveButton("হ্যাঁ", (dialog, which) -> syncChecklistItem(item, "DELETE", 0))
                .setNegativeButton("না", null)
                .show();
    }

    private void syncChecklistItem(TripChecklistItem item, String action, int isChecked) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "ইন্টারনেট সংযোগ নেই!", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sharedPrefs.getToken();
        int itemId = item.getId();

        apiService.syncNotesChecklist("Bearer " + token, tripId, "CHECKLIST", action, itemId, item.getTitle(), "", isChecked)
                .enqueue(new Callback<SyncGenericResponse>() {
                    @Override
                    public void onResponse(Call<SyncGenericResponse> call, Response<SyncGenericResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            SyncGenericResponse res = response.body();
                            if (res.isSuccess()) {
                                if (action.equals("TOGGLE")) {
                                    // Just show a subtle message
                                    Toast.makeText(TripNotesActivity.this, "আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(TripNotesActivity.this, "চেকলিস্ট সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show();
                                }
                                loadData();
                            } else {
                                Toast.makeText(TripNotesActivity.this, "ব্যর্থ হয়েছে: " + res.getError(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(TripNotesActivity.this, "সার্ভার সিঙ্ক ব্যর্থ হয়েছে!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<SyncGenericResponse> call, Throwable t) {
                        Toast.makeText(TripNotesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
