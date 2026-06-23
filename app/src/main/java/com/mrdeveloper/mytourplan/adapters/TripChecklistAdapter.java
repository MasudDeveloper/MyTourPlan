package com.mrdeveloper.mytourplan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.models.TripChecklistItem;

import java.util.List;

public class TripChecklistAdapter extends RecyclerView.Adapter<TripChecklistAdapter.ChecklistViewHolder> {

    public interface OnChecklistActionListener {
        void onToggleChecklist(TripChecklistItem item, boolean isChecked);
        void onDeleteChecklist(TripChecklistItem item);
    }

    private List<TripChecklistItem> items;
    private final OnChecklistActionListener listener;

    public TripChecklistAdapter(List<TripChecklistItem> items, OnChecklistActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<TripChecklistItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChecklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_checklist, parent, false);
        return new ChecklistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistViewHolder holder, int position) {
        TripChecklistItem item = items.get(position);
        holder.tvChecklistTitle.setText(item.getTitle());

        // Reset check change listener to avoid triggers during bind
        holder.cbChecklist.setOnCheckedChangeListener(null);
        holder.cbChecklist.setChecked(item.isCheckedBoolean());

        // Apply visual feedback for checked state (strikethrough & color change)
        if (item.isCheckedBoolean()) {
            holder.tvChecklistTitle.setPaintFlags(holder.tvChecklistTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvChecklistTitle.setTextColor(0xFF94A3B8); // Slate 300
        } else {
            holder.tvChecklistTitle.setPaintFlags(holder.tvChecklistTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvChecklistTitle.setTextColor(0xFF0F172A); // Slate 900
        }

        // Set the listener for user toggle actions
        holder.cbChecklist.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setCheckedBoolean(isChecked);
            if (isChecked) {
                holder.tvChecklistTitle.setPaintFlags(holder.tvChecklistTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvChecklistTitle.setTextColor(0xFF94A3B8);
            } else {
                holder.tvChecklistTitle.setPaintFlags(holder.tvChecklistTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                holder.tvChecklistTitle.setTextColor(0xFF0F172A);
            }
            if (listener != null) {
                listener.onToggleChecklist(item, isChecked);
            }
        });

        holder.ivDeleteChecklist.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteChecklist(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ChecklistViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbChecklist;
        TextView tvChecklistTitle;
        ImageView ivDeleteChecklist;

        public ChecklistViewHolder(@NonNull View itemView) {
            super(itemView);
            cbChecklist = itemView.findViewById(R.id.cbChecklist);
            tvChecklistTitle = itemView.findViewById(R.id.tvChecklistTitle);
            ivDeleteChecklist = itemView.findViewById(R.id.ivDeleteChecklist);
        }
    }
}
