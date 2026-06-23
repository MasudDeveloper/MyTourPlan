package com.mrdeveloper.mytourplan.api;

import com.mrdeveloper.mytourplan.models.AuthResponse;
import com.mrdeveloper.mytourplan.models.DashboardResponse;
import com.mrdeveloper.mytourplan.models.ExpenseTrackerResponse;
import com.mrdeveloper.mytourplan.models.ItineraryResponse;
import com.mrdeveloper.mytourplan.models.LoginRequest;
import com.mrdeveloper.mytourplan.models.GeneratePlanRequest;
import com.mrdeveloper.mytourplan.models.GeneratePlanResponse;
import com.mrdeveloper.mytourplan.models.GenericResponse;
import com.mrdeveloper.mytourplan.models.RegisterRequest;
import com.mrdeveloper.mytourplan.models.SaveTripRequest;
import com.mrdeveloper.mytourplan.models.SyncGenericResponse;
import com.mrdeveloper.mytourplan.models.SyncTripResponse;
import com.mrdeveloper.mytourplan.models.TripsResponse;
import com.mrdeveloper.mytourplan.models.TripNotesResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Field;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("register.php")
    Call<AuthResponse> register(@Body RegisterRequest body);

    @POST("login.php")
    Call<AuthResponse> login(@Body LoginRequest body);

    @GET("get_dashboard.php")
    Call<DashboardResponse> getDashboard(@Header("Authorization") String token);

    @GET("get_my_trips.php")
    Call<TripsResponse> getMyTrips(@Header("Authorization") String token);

    @GET("get_expense_tracker.php")
    Call<ExpenseTrackerResponse> getExpenseTracker(@Header("Authorization") String token, @Query("trip_id") String tripId);

    @GET("get_trip_dashboard.php")
    Call<com.mrdeveloper.mytourplan.models.TripDashboardResponse> getTripDashboard(@Header("Authorization") String token, @Query("trip_id") String tripId);

    @GET("get_itinerary.php")
    Call<ItineraryResponse> getItinerary(@Header("Authorization") String token, @Query("trip_id") String tripId);

    @POST("generate_plan.php")
    Call<GeneratePlanResponse> generatePlan(@Header("Authorization") String token, @Body GeneratePlanRequest body);

    @POST("save_trip.php")
    Call<GenericResponse> saveTrip(@Header("Authorization") String token, @Body SaveTripRequest body);

    @POST("delete_trip.php")
    Call<GenericResponse> deleteTrip(@Header("Authorization") String token, @Body com.mrdeveloper.mytourplan.models.GenericRequest body);

    @Multipart
    @POST("add_trip.php")
    Call<SyncTripResponse> addTrip(
            @Header("Authorization") String token,
            @Part("from_location") RequestBody fromLocation,
            @Part("destination") RequestBody destination,
            @Part("start_date") RequestBody startDate,
            @Part("end_date") RequestBody endDate,
            @Part("members_count") RequestBody membersCount,
            @Part("budget") RequestBody budget,
            @Part("status") RequestBody status,
            @Part MultipartBody.Part image
    );

    @Multipart
    @POST("update_trip.php")
    Call<GenericResponse> updateTrip(
            @Header("Authorization") String token,
            @Part("trip_id") RequestBody tripId,
            @Part("from_location") RequestBody fromLocation,
            @Part("destination") RequestBody destination,
            @Part("start_date") RequestBody startDate,
            @Part("end_date") RequestBody endDate,
            @Part("members_count") RequestBody membersCount,
            @Part("budget") RequestBody budget,
            @Part("status") RequestBody status,
            @Part MultipartBody.Part image
    );

    @Multipart
    @POST("sync_trip.php")
    Call<SyncTripResponse> syncTrip(
            @Header("Authorization") String token,
            @Part("user_id") RequestBody userId,
            @Part("from_location") RequestBody fromLocation,
            @Part("destination") RequestBody destination,
            @Part("start_date") RequestBody startDate,
            @Part("end_date") RequestBody endDate,
            @Part("members_count") RequestBody membersCount,
            @Part("budget") RequestBody budget,
            @Part("status") RequestBody status,
            @Part("local_id") RequestBody localId,
            @Part MultipartBody.Part image
    );

    @FormUrlEncoded
    @POST("sync_expense.php")
    Call<SyncGenericResponse> syncExpense(
            @Header("Authorization") String token,
            @Field("trip_id") String tripId,
            @Field("category") String category,
            @Field("amount") double amount,
            @Field("note") String note,
            @Field("created_at") String createdAt,
            @Field("local_id") String localId,
            @Field("action") String action,
            @Field("server_id") int serverId
    );

    @FormUrlEncoded
    @POST("sync_itinerary.php")
    Call<SyncGenericResponse> syncItinerary(
            @Header("Authorization") String token,
            @Field("trip_id") String tripId,
            @Field("day") int day,
            @Field("time") String time,
            @Field("activity") String activity,
            @Field("location") String location,
            @Field("local_id") String localId,
            @Field("action") String action,
            @Field("server_id") int serverId
    );

    @FormUrlEncoded
    @POST("sync_member.php")
    Call<SyncGenericResponse> syncMember(
            @Header("Authorization") String token,
            @Field("trip_id") String tripId,
            @Field("name") String name,
            @Field("amount_paid") double amountPaid,
            @Field("payment_method") String paymentMethod,
            @Field("local_id") String localId,
            @Field("action") String action,
            @Field("server_id") int serverId
    );
    @GET("get_members.php")
    Call<com.mrdeveloper.mytourplan.models.MembersResponse> getMembers(@Header("Authorization") String token, @Query("trip_id") String tripId);

    @GET("get_profile.php")
    Call<com.mrdeveloper.mytourplan.models.ProfileResponse> getProfile(@Header("Authorization") String token);

    @Multipart
    @POST("update_profile.php")
    Call<SyncGenericResponse> updateProfile(
            @Header("Authorization") String token,
            @Part("name") RequestBody name,
            @Part("phone") RequestBody phone,
            @Part okhttp3.MultipartBody.Part profilePic
    );

    @GET("get_notes_checklist.php")
    Call<TripNotesResponse> getNotesChecklist(
            @Header("Authorization") String token,
            @Query("trip_id") String tripId
    );

    @FormUrlEncoded
    @POST("sync_notes_checklist.php")
    Call<SyncGenericResponse> syncNotesChecklist(
            @Header("Authorization") String token,
            @Field("trip_id") String tripId,
            @Field("type") String type,
            @Field("action") String action,
            @Field("id") int id,
            @Field("title") String title,
            @Field("content") String content,
            @Field("is_checked") int isChecked
    );
}
