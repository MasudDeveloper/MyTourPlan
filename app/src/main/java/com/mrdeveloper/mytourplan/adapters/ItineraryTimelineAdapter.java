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

    public interface OnItineraryLongClickListener {
        void onItineraryLongClick(ItineraryItem item);
    }

    private List<ItineraryItem> items;
    private OnItineraryLongClickListener longClickListener;

    public ItineraryTimelineAdapter(List<ItineraryItem> items, OnItineraryLongClickListener longClickListener) {
        this.items = items;
        this.longClickListener = longClickListener;
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

        if (position == 0 || items.get(position - 1).getDay() != item.getDay()) {
            holder.tvDay.setVisibility(View.VISIBLE);
            holder.tvDay.setText(formatBengaliDay(item.getDay()));
        } else {
            holder.tvDay.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItineraryLongClick(item);
            }
            return true;
        });
    }

    private String formatBengaliDay(int day) {
        String dayStr = String.valueOf(day);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dayStr.length(); i++) {
            char c = dayStr.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append((char) (c - '0' + '০'));
            } else {
                sb.append(c);
            }
        }
        return "দিন " + sb.toString();
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvTime, tvActivity, tvLocation;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvActivity = itemView.findViewById(R.id.tvActivity);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }
    }
}
