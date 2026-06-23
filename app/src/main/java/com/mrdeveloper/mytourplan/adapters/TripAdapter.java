package com.mrdeveloper.mytourplan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.models.Trip;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    public interface OnTripActionListener {
        void onEditClick(Trip trip);
        void onDeleteClick(Trip trip);
        void onTripClick(Trip trip);
    }

    private List<Trip> trips;
    private OnTripActionListener listener;

    public TripAdapter(List<Trip> trips, OnTripActionListener listener) {
        this.trips = trips;
        this.listener = listener;
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
        holder.tvDates.setText(trip.getStartDate() + " থেকে " + trip.getEndDate());
        String status = trip.getStatus() != null ? trip.getStatus() : "Upcoming";
        if ("Upcoming".equalsIgnoreCase(status)) {
            holder.tvStatus.setText("আসন্ন");
        } else if ("Completed".equalsIgnoreCase(status)) {
            holder.tvStatus.setText("সম্পন্ন");
        } else {
            holder.tvStatus.setText(status);
        }
        holder.tvMembers.setText(trip.getMembersCount() + " জন");
        holder.tvBudget.setText("৳ " + trip.getBudget());

        String imageUri = trip.getImageUri();
        if (imageUri != null && !imageUri.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_login_hero)
                    .into(holder.ivTripCover);
        } else {
            holder.ivTripCover.setImageResource(R.drawable.ic_login_hero);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(trip);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(trip);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTripClick(trip);
        });
    }

    @Override
    public int getItemCount() {
        return trips != null ? trips.size() : 0;
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tvDestination, tvDates, tvStatus, tvMembers, tvBudget;
        ImageView ivTripCover, btnEdit, btnDelete;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvDates = itemView.findViewById(R.id.tvDates);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvMembers = itemView.findViewById(R.id.tvMembers);
            tvBudget = itemView.findViewById(R.id.tvBudget);
            ivTripCover = itemView.findViewById(R.id.ivTripCover);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
