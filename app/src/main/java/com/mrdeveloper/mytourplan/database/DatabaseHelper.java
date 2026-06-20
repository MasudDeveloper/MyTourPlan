package com.mrdeveloper.mytourplan.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mrdeveloper.mytourplan.models.Expense;
import com.mrdeveloper.mytourplan.models.Trip;
import com.mrdeveloper.mytourplan.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyTourPlan.db";
    private static final int DATABASE_VERSION = 5; // Upgraded to fix missing columns

    // Common Sync Columns
    private static final String COL_IS_SYNCED = "is_synced";
    private static final String COL_SYNC_ACTION = "sync_action"; // "INSERT", "UPDATE", "DELETE"
    private static final String COL_SERVER_ID = "server_id";

    // Users Table
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_NAME = "name";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PHONE = "phone";
    private static final String COL_USER_PROFILE_PIC = "profile_pic";
    private static final String COL_USER_PASSWORD = "password";

    // Trips Table
    private static final String TABLE_TRIPS = "trips";
    private static final String COL_TRIP_ID = "id";
    private static final String COL_TRIP_USER_ID = "user_id";
    private static final String COL_TRIP_FROM = "from_location";
    private static final String COL_TRIP_DESTINATION = "destination";
    private static final String COL_TRIP_IMAGE = "image_uri";
    private static final String COL_TRIP_START_DATE = "start_date";
    private static final String COL_TRIP_END_DATE = "end_date";
    private static final String COL_TRIP_MEMBERS = "members_count";
    private static final String COL_TRIP_BUDGET = "budget";
    private static final String COL_TRIP_STATUS = "status";

    // Expenses Table
    private static final String TABLE_EXPENSES = "expenses";
    private static final String COL_EXP_ID = "id";
    private static final String COL_EXP_TRIP_ID = "trip_id";
    private static final String COL_EXP_CATEGORY = "category";
    private static final String COL_EXP_AMOUNT = "amount";
    private static final String COL_EXP_NOTE = "note";
    private static final String COL_EXP_DATE = "created_at";

    // Itinerary Table
    private static final String TABLE_ITINERARY = "itinerary";
    private static final String COL_ITIN_ID = "id";
    private static final String COL_ITIN_TRIP_ID = "trip_id";
    private static final String COL_ITIN_DAY = "day";
    private static final String COL_ITIN_TIME = "time";
    private static final String COL_ITIN_ACTIVITY = "activity";
    private static final String COL_ITIN_LOCATION = "location";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT, " +
                COL_USER_EMAIL + " TEXT UNIQUE, " +
                COL_USER_PHONE + " TEXT, " +
                COL_USER_PROFILE_PIC + " TEXT, " +
                COL_USER_PASSWORD + " TEXT)";
        db.execSQL(createUsersTable);

        String createTripsTable = "CREATE TABLE " + TABLE_TRIPS + " (" +
                COL_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TRIP_USER_ID + " TEXT, " +
                COL_TRIP_FROM + " TEXT, " +
                COL_TRIP_DESTINATION + " TEXT, " +
                COL_TRIP_IMAGE + " TEXT, " +
                COL_TRIP_START_DATE + " TEXT, " +
                COL_TRIP_END_DATE + " TEXT, " +
                COL_TRIP_MEMBERS + " INTEGER, " +
                COL_TRIP_BUDGET + " REAL, " +
                COL_TRIP_STATUS + " TEXT, " +
                COL_IS_SYNCED + " INTEGER DEFAULT 0, " +
                COL_SYNC_ACTION + " TEXT DEFAULT 'INSERT', " +
                COL_SERVER_ID + " INTEGER DEFAULT -1)";
        db.execSQL(createTripsTable);

        String createExpensesTable = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                COL_EXP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EXP_TRIP_ID + " TEXT, " +
                COL_EXP_CATEGORY + " TEXT, " +
                COL_EXP_AMOUNT + " REAL, " +
                COL_EXP_NOTE + " TEXT, " +
                COL_EXP_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COL_IS_SYNCED + " INTEGER DEFAULT 0, " +
                COL_SYNC_ACTION + " TEXT DEFAULT 'INSERT', " +
                COL_SERVER_ID + " INTEGER DEFAULT -1)";
        db.execSQL(createExpensesTable);

        String createItineraryTable = "CREATE TABLE " + TABLE_ITINERARY + " (" +
                COL_ITIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ITIN_TRIP_ID + " TEXT, " +
                COL_ITIN_DAY + " INTEGER, " +
                COL_ITIN_TIME + " TEXT, " +
                COL_ITIN_ACTIVITY + " TEXT, " +
                COL_ITIN_LOCATION + " TEXT, " +
                COL_SERVER_ID + " INTEGER DEFAULT -1)";
        db.execSQL(createItineraryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITINERARY);
        onCreate(db);
    }

    // --- User Methods ---
    public long registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);
        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PASSWORD, password);
        return db.insert(TABLE_USERS, null, values);
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID, COL_USER_NAME, COL_USER_EMAIL, COL_USER_PHONE, COL_USER_PROFILE_PIC},
                COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(cursor.getInt(0), cursor.getString(1), cursor.getString(2), 
                cursor.getString(3), cursor.getString(4));
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, user.getName());
        values.put(COL_USER_PHONE, user.getPhone());
        values.put(COL_USER_PROFILE_PIC, user.getProfilePic());
        db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(user.getId())});
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            User user = new User(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PHONE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PROFILE_PIC))
            );
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public boolean updateUserProfile(int userId, String name, String phone, String profilePic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);
        values.put(COL_USER_PHONE, phone);
        values.put(COL_USER_PROFILE_PIC, profilePic);

        int rows = db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public void saveOrUpdateUserLocally(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, user.getId());
        values.put(COL_USER_NAME, user.getName());
        values.put(COL_USER_EMAIL, user.getEmail());
        values.put(COL_USER_PHONE, user.getPhone());
        values.put(COL_USER_PROFILE_PIC, user.getProfilePic());

        // Check if user exists
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_ID + "=?", new String[]{String.valueOf(user.getId())});
        if (cursor != null && cursor.moveToFirst()) {
            // Update
            db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(user.getId())});
            cursor.close();
        } else {
            // Insert
            db.insert(TABLE_USERS, null, values);
            if (cursor != null) cursor.close();
        }
    }

    // --- Trip Methods ---
    public long createTripLocally(Trip trip) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRIP_USER_ID, trip.getUserId());
        values.put(COL_TRIP_FROM, trip.getFromLocation());
        values.put(COL_TRIP_DESTINATION, trip.getDestination());
        values.put(COL_TRIP_IMAGE, trip.getImageUri());
        values.put(COL_TRIP_START_DATE, trip.getStartDate());
        values.put(COL_TRIP_END_DATE, trip.getEndDate());
        values.put(COL_TRIP_MEMBERS, trip.getMembersCount());
        values.put(COL_TRIP_BUDGET, trip.getBudget());
        values.put(COL_TRIP_STATUS, trip.getStatus());
        values.put(COL_IS_SYNCED, 0);
        values.put(COL_SYNC_ACTION, "INSERT");
        return db.insert(TABLE_TRIPS, null, values);
    }

    public void markTripAsSynced(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_SYNCED, 1);
        values.put(COL_SYNC_ACTION, "NONE");
        db.update(TABLE_TRIPS, values, COL_TRIP_ID + "=?", new String[]{id});
    }

    public Trip getTripById(String tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRIPS + " WHERE " + COL_TRIP_ID + "=?", new String[]{tripId});
        if (cursor.moveToFirst()) {
            Trip t = new Trip();
            t.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRIP_ID))));
            t.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_USER_ID)));
            t.setFromLocation(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_FROM)));
            t.setDestination(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_DESTINATION)));
            t.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_IMAGE)));
            t.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_START_DATE)));
            t.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_END_DATE)));
            t.setMembersCount(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRIP_MEMBERS)));
            t.setBudget(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRIP_BUDGET)));
            t.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_STATUS)));
            t.setIsSynced(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_SYNCED)));
            t.setSyncAction(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_ACTION)));
            cursor.close();
            return t;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<Trip> getTripsByUser(String userId) {
        List<Trip> trips = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRIPS + " WHERE " + COL_TRIP_USER_ID + "=? ORDER BY " + COL_TRIP_ID + " DESC", new String[]{userId});
        if (cursor.moveToFirst()) {
            do {
                Trip t = new Trip();
                t.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRIP_ID))));
                t.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_USER_ID)));
                t.setFromLocation(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_FROM)));
                t.setDestination(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_DESTINATION)));
                t.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_IMAGE)));
                t.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_START_DATE)));
                t.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_END_DATE)));
                t.setMembersCount(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRIP_MEMBERS)));
                t.setBudget(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRIP_BUDGET)));
                t.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_STATUS)));
                t.setIsSynced(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_SYNCED)));
                t.setSyncAction(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_ACTION)));
                trips.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return trips;
    }

    public Trip getUpcomingTrip(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Since sqlite date formats might vary, we'll just grab the most recent "Upcoming" status trip
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRIPS + " WHERE " + COL_TRIP_USER_ID + "=? AND " + COL_TRIP_STATUS + "='Upcoming' ORDER BY " + COL_TRIP_ID + " DESC LIMIT 1", new String[]{userId});
        if (cursor.moveToFirst()) {
            Trip t = new Trip();
            t.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRIP_ID))));
            t.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_USER_ID)));
            t.setFromLocation(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_FROM)));
            t.setDestination(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_DESTINATION)));
            t.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_IMAGE)));
            t.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_START_DATE)));
            t.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_END_DATE)));
            t.setMembersCount(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRIP_MEMBERS)));
            t.setBudget(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRIP_BUDGET)));
            t.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_STATUS)));
            t.setIsSynced(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_SYNCED)));
            t.setSyncAction(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_ACTION)));
            cursor.close();
            return t;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<Trip> getUnsyncedTrips() {
        List<Trip> trips = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRIPS + " WHERE " + COL_IS_SYNCED + "=0", null);
        if (cursor.moveToFirst()) {
            do {
                Trip t = new Trip();
                t.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRIP_ID))));
                t.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_USER_ID)));
                t.setFromLocation(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_FROM)));
                t.setDestination(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_DESTINATION)));
                t.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_IMAGE)));
                t.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_START_DATE)));
                t.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_END_DATE)));
                t.setMembersCount(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRIP_MEMBERS)));
                t.setBudget(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRIP_BUDGET)));
                t.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_STATUS)));
                t.setIsSynced(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_SYNCED)));
                t.setSyncAction(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_ACTION)));
                trips.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return trips;
    }

    // --- Expense Methods ---
    public long addExpenseLocally(Expense exp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EXP_TRIP_ID, exp.getTripId());
        values.put(COL_EXP_CATEGORY, exp.getCategory());
        values.put(COL_EXP_AMOUNT, exp.getAmount());
        values.put(COL_EXP_NOTE, exp.getNote());
        values.put(COL_IS_SYNCED, 0);
        values.put(COL_SYNC_ACTION, "INSERT");
        return db.insert(TABLE_EXPENSES, null, values);
    }

    public void markExpenseAsSynced(String id, int serverId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_SYNCED, 1);
        values.put(COL_SYNC_ACTION, "NONE");
        if (serverId != -1) {
            values.put(COL_SERVER_ID, serverId);
        }
        db.update(TABLE_EXPENSES, values, COL_EXP_ID + "=?", new String[]{id});
    }

    public void permanentlyDeleteExpense(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, COL_EXP_ID + "=?", new String[]{id});
    }

    public void updateExpenseLocally(Expense exp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EXP_CATEGORY, exp.getCategory());
        values.put(COL_EXP_AMOUNT, exp.getAmount());
        values.put(COL_EXP_NOTE, exp.getNote());
        values.put(COL_IS_SYNCED, 0);
        
        // If it was never synced, keep it as INSERT, else UPDATE
        Cursor cursor = db.rawQuery("SELECT " + COL_SYNC_ACTION + " FROM " + TABLE_EXPENSES + " WHERE " + COL_EXP_ID + "=?", new String[]{exp.getId()});
        String action = "UPDATE";
        if (cursor.moveToFirst()) {
            String currentAction = cursor.getString(0);
            if ("INSERT".equals(currentAction)) {
                action = "INSERT";
            }
        }
        cursor.close();
        
        values.put(COL_SYNC_ACTION, action);
        db.update(TABLE_EXPENSES, values, COL_EXP_ID + "=?", new String[]{exp.getId()});
    }

    public void deleteExpenseLocally(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_SYNC_ACTION + " FROM " + TABLE_EXPENSES + " WHERE " + COL_EXP_ID + "=?", new String[]{id});
        if (cursor.moveToFirst()) {
            String currentAction = cursor.getString(0);
            if ("INSERT".equals(currentAction)) {
                // Never synced to server, safe to permanently delete locally
                cursor.close();
                permanentlyDeleteExpense(id);
                return;
            }
        }
        cursor.close();
        
        // Soft delete for sync
        ContentValues values = new ContentValues();
        values.put(COL_IS_SYNCED, 0);
        values.put(COL_SYNC_ACTION, "DELETE");
        db.update(TABLE_EXPENSES, values, COL_EXP_ID + "=?", new String[]{id});
    }

    public Expense getExpenseById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXPENSES + " WHERE " + COL_EXP_ID + "=?", new String[]{id});
        if (cursor.moveToFirst()) {
            Expense exp = new Expense();
            exp.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_EXP_ID))));
            exp.setTripId(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_TRIP_ID)));
            exp.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_CATEGORY)));
            exp.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_EXP_AMOUNT)));
            exp.setNote(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_NOTE)));
            exp.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DATE)));
            exp.setIsSynced(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_SYNCED)));
            exp.setSyncAction(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_ACTION)));
            exp.setServerId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SERVER_ID)));
            cursor.close();
            return exp;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<Expense> getExpensesByTrip(String tripId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXPENSES + " WHERE " + COL_EXP_TRIP_ID + "=? AND " + COL_SYNC_ACTION + " != 'DELETE' ORDER BY " + COL_EXP_ID + " DESC", new String[]{tripId});
        if (cursor.moveToFirst()) {
            do {
                Expense exp = new Expense();
                exp.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_EXP_ID))));
                exp.setTripId(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_TRIP_ID)));
                exp.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_CATEGORY)));
                exp.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_EXP_AMOUNT)));
                exp.setNote(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_NOTE)));
                exp.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DATE)));
                exp.setIsSynced(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_SYNCED)));
                exp.setSyncAction(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_ACTION)));
                exp.setServerId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SERVER_ID)));
                expenses.add(exp);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenses;
    }

    public List<Expense> getUnsyncedExpenses() {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXPENSES + " WHERE " + COL_IS_SYNCED + "=0", null);
        if (cursor.moveToFirst()) {
            do {
                Expense exp = new Expense();
                exp.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_EXP_ID))));
                exp.setTripId(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_TRIP_ID)));
                exp.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_CATEGORY)));
                exp.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_EXP_AMOUNT)));
                exp.setNote(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_NOTE)));
                exp.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DATE)));
                exp.setIsSynced(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_SYNCED)));
                exp.setSyncAction(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_ACTION)));
                exp.setServerId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SERVER_ID)));
                expenses.add(exp);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenses;
    }

    // --- Itinerary Methods ---
    public long addItineraryLocally(com.mrdeveloper.mytourplan.models.ItineraryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITIN_TRIP_ID, item.getTripId());
        values.put(COL_ITIN_DAY, item.getDay());
        values.put(COL_ITIN_TIME, item.getTime());
        values.put(COL_ITIN_ACTIVITY, item.getActivity());
        values.put(COL_ITIN_LOCATION, item.getLocation());
        return db.insert(TABLE_ITINERARY, null, values);
    }

    public List<com.mrdeveloper.mytourplan.models.ItineraryItem> getItineraryByTrip(String tripId) {
        List<com.mrdeveloper.mytourplan.models.ItineraryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITINERARY + " WHERE " + COL_ITIN_TRIP_ID + "=? ORDER BY " + COL_ITIN_DAY + " ASC, " + COL_ITIN_TIME + " ASC", new String[]{tripId});
        if (cursor.moveToFirst()) {
            do {
                com.mrdeveloper.mytourplan.models.ItineraryItem item = new com.mrdeveloper.mytourplan.models.ItineraryItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITIN_ID)));
                item.setTripId(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITIN_TRIP_ID)));
                item.setDay(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITIN_DAY)));
                item.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITIN_TIME)));
                item.setActivity(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITIN_ACTIVITY)));
                item.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITIN_LOCATION)));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }
}
