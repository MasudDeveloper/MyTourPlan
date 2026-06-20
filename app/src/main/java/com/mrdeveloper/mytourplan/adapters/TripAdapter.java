package com.mrdeveloper.mytourplan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.models.Trip;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private List<Trip> trips;

    public TripAdapter(List<Trip> trips) {
        this.trips = trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.tvDestination.setText(trip.getDestination());
        holder.tvDates.setText(trip.getStartDate() + " - " + trip.getEndDate());
        holder.tvStatus.setText(trip.getStatus() != null ? trip.getStatus() : "Upcoming");
        holder.tvMembers.setText(trip.getMembersCount() + " People");
        holder.tvBudget.setText("৳ " + trip.getBudget());

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), com.mrdeveloper.mytourplan.activities.TripDashboardActivity.class);
            try {
                intent.putExtra("trip_id", Integer.parseInt(trip.getId()));
                intent.putExtra("members", trip.getMembersCount());
                intent.putExtra("budget", trip.getBudget());
            } catch (NumberFormatException e) {
                // Handle parsing error if necessary
            }
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return trips != null ? trips.size() : 0;
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tvDestination, tvDates, tvStatus, tvMembers, tvBudget;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvDates = itemView.findViewById(R.id.tvDates);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvMembers = itemView.findViewById(R.id.tvMembers);
            tvBudget = itemView.findViewById(R.id.tvBudget);
        }
    }
}
