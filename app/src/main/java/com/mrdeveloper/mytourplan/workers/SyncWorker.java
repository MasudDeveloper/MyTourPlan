package com.mrdeveloper.mytourplan.workers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mrdeveloper.mytourplan.database.DatabaseHelper;
import com.mrdeveloper.mytourplan.models.Expense;
import com.mrdeveloper.mytourplan.models.Trip;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void scheduleSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // One-time sync request for immediate sync when network is available
        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueue(syncRequest);
    }

    public static void schedulePeriodicSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest periodicSync = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "PeriodicSync",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                periodicSync
        );
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting sync process...");
        Context context = getApplicationContext();

        DatabaseHelper db = new DatabaseHelper(context);
        com.mrdeveloper.mytourplan.utils.SharedPrefs prefs = new com.mrdeveloper.mytourplan.utils.SharedPrefs(context);
        String token = "Bearer " + prefs.getToken();
        com.mrdeveloper.mytourplan.api.ApiService apiService = com.mrdeveloper.mytourplan.api.ApiClient.getClient().create(com.mrdeveloper.mytourplan.api.ApiService.class);

        boolean success = true;

        Intent startIntent = new Intent("com.mrdeveloper.mytourplan.SYNC_STATE_CHANGED");
        startIntent.putExtra("is_syncing", true);
        context.sendBroadcast(startIntent);

        List<Trip> unsyncedTrips = db.getUnsyncedTrips();
        List<Expense> unsyncedExpenses = db.getUnsyncedExpenses();
        List<com.mrdeveloper.mytourplan.models.TripMember> unsyncedMembers = db.getUnsyncedMembers();
        
        int totalItems = unsyncedTrips.size() + unsyncedExpenses.size() + unsyncedMembers.size();
        int currentItem = 0;
        
        if (totalItems == 0) {
            // Fake a tiny delay so user sees the progress bar validating
            try { Thread.sleep(800); } catch (InterruptedException e) {}
            
            broadcastProgress(context, 100);
            Intent endIntent = new Intent("com.mrdeveloper.mytourplan.SYNC_STATE_CHANGED");
            endIntent.putExtra("is_syncing", false);
            context.sendBroadcast(endIntent);
            return Result.success();
        }
        
        if (!unsyncedTrips.isEmpty()) {
            Log.d(TAG, "Found " + unsyncedTrips.size() + " unsynced trips.");
            for (Trip t : unsyncedTrips) {
                try {
                    okhttp3.RequestBody userId = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), t.getUserId() != null ? t.getUserId() : "");
                    okhttp3.RequestBody fromLocation = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), t.getFromLocation() != null ? t.getFromLocation() : "");
                    okhttp3.RequestBody destination = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), t.getDestination() != null ? t.getDestination() : "");
                    okhttp3.RequestBody startDate = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), t.getStartDate() != null ? t.getStartDate() : "");
                    okhttp3.RequestBody endDate = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), t.getEndDate() != null ? t.getEndDate() : "");
                    okhttp3.RequestBody membersCount = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), String.valueOf(t.getMembersCount()));
                    okhttp3.RequestBody budget = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), String.valueOf(t.getBudget()));
                    okhttp3.RequestBody status = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), t.getStatus() != null ? t.getStatus() : "");
                    okhttp3.RequestBody localId = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), t.getId());

                    // Image handling omitted for simplicity in background worker to avoid URI permission crashes
                    // Retrofit does not allow null for @Part parameters, so we pass a dummy empty part.
                    okhttp3.RequestBody emptyFileBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/*"), new byte[0]);
                    okhttp3.MultipartBody.Part imagePart = okhttp3.MultipartBody.Part.createFormData("image", "", emptyFileBody);

                    retrofit2.Response<com.mrdeveloper.mytourplan.models.SyncTripResponse> response = apiService.syncTrip(
                            token, userId, fromLocation, destination, startDate, endDate, membersCount, budget, status, localId, imagePart
                    ).execute();

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        int newServerId = response.body().getServerId();
                        db.markTripAsSynced(t.getId(), newServerId);
                        Log.d(TAG, "Synced Trip ID: " + t.getId() + " with Server ID: " + newServerId);
                    } else {
                        success = false;
                        Log.e(TAG, "Failed to sync Trip ID: " + t.getId());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception syncing trip", e);
                    success = false;
                }
                currentItem++;
                broadcastProgress(context, (int) ((currentItem / (float) totalItems) * 100));
            }
        }

        unsyncedExpenses = db.getUnsyncedExpenses();
        if (!unsyncedExpenses.isEmpty()) {
            Log.d(TAG, "Found " + unsyncedExpenses.size() + " unsynced expenses.");
            for (Expense exp : unsyncedExpenses) {
                try {
                    Trip parentTrip = db.getTripById(exp.getTripId());
                    if (parentTrip == null || parentTrip.getServerId() == -1) {
                        Log.d(TAG, "Skipping Expense ID: " + exp.getId() + " because parent trip is not synced yet.");
                        continue;
                    }
                    String serverTripId = String.valueOf(parentTrip.getServerId());

                    retrofit2.Response<com.mrdeveloper.mytourplan.models.SyncGenericResponse> response = apiService.syncExpense(
                            token, serverTripId, exp.getCategory(), exp.getAmount(), exp.getNote(), exp.getCreatedAt(), exp.getId(), exp.getSyncAction(), exp.getServerId()
                    ).execute();

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        if ("DELETE".equals(exp.getSyncAction())) {
                            db.permanentlyDeleteExpense(exp.getId());
                            Log.d(TAG, "Permanently deleted Expense ID: " + exp.getId());
                        } else {
                            int newServerId = response.body().getServerId();
                            db.markExpenseAsSynced(exp.getId(), newServerId != 0 ? newServerId : exp.getServerId());
                            Log.d(TAG, "Synced Expense ID: " + exp.getId());
                        }
                    } else {
                        success = false;
                        Log.e(TAG, "Failed to sync Expense ID: " + exp.getId());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception syncing expense", e);
                    success = false;
                }
                currentItem++;
                broadcastProgress(context, (int) ((currentItem / (float) totalItems) * 100));
            }
        }

        // Sync Members
        unsyncedMembers = db.getUnsyncedMembers();
        if (!unsyncedMembers.isEmpty()) {
            Log.d(TAG, "Found " + unsyncedMembers.size() + " unsynced members.");
            for (com.mrdeveloper.mytourplan.models.TripMember member : unsyncedMembers) {
                try {
                    Trip parentTrip = db.getTripById(member.getTripId());
                    if (parentTrip == null || parentTrip.getServerId() == -1) {
                        Log.d(TAG, "Skipping Member ID: " + member.getId() + " because parent trip is not synced yet.");
                        continue;
                    }
                    String serverTripId = String.valueOf(parentTrip.getServerId());

                    retrofit2.Response<com.mrdeveloper.mytourplan.models.SyncGenericResponse> response = apiService.syncMember(
                            token, serverTripId, member.getName(), member.getAmountPaid(), member.getPaymentMethod(), member.getId(), member.getSyncAction(), member.getServerId()
                    ).execute();

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        if ("DELETE".equals(member.getSyncAction())) {
                            db.permanentlyDeleteMember(member.getId());
                            Log.d(TAG, "Permanently deleted Member ID: " + member.getId());
                        } else {
                            int newServerId = response.body().getServerId();
                            db.markMemberAsSynced(member.getId(), newServerId != 0 ? newServerId : member.getServerId());
                            Log.d(TAG, "Synced Member ID: " + member.getId());
                        }
                    } else {
                        success = false;
                        Log.e(TAG, "Failed to sync Member ID: " + member.getId());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception syncing member", e);
                    success = false;
                }
                currentItem++;
                broadcastProgress(context, (int) ((currentItem / (float) totalItems) * 100));
            }
        }

        Intent endIntent = new Intent("com.mrdeveloper.mytourplan.SYNC_STATE_CHANGED");
        endIntent.putExtra("is_syncing", false);
        context.sendBroadcast(endIntent);

        Log.d(TAG, "Sync process completed.");
        return success ? Result.success() : Result.retry();
    }
    
    private void broadcastProgress(Context context, int percentage) {
        Intent intent = new Intent("com.mrdeveloper.mytourplan.SYNC_PROGRESS");
        intent.putExtra("progress", percentage);
        context.sendBroadcast(intent);
    }
}
