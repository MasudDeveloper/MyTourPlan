package com.mrdeveloper.mytourplan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.models.ItineraryItem;

import java.util.List;

public class ItineraryTimelineAdapter extends RecyclerView.Adapter<ItineraryTimelineAdapter.TimelineViewHolder> {

    private List<ItineraryItem> items;

    public ItineraryTimelineAdapter(List<ItineraryItem> items) {
        this.items = items;
    }

    public void setItems(List<ItineraryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        ItineraryItem item = items.get(position);
        holder.tvTime.setText(item.getTime());
        holder.tvActivity.setText(item.getActivity());
        holder.tvLocation.setText(item.getLocation());
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvActivity, tvLocation;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvActivity = itemView.findViewById(R.id.tvActivity);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }
    }
}
