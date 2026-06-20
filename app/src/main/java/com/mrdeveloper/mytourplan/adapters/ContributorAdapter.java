package com.mrdeveloper.mytourplan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.models.Contributor;

import java.util.List;

public class ContributorAdapter extends RecyclerView.Adapter<ContributorAdapter.ContributorViewHolder> {

    private List<Contributor> contributors;

    public ContributorAdapter(List<Contributor> contributors) {
        this.contributors = contributors;
    }

    public void setContributors(List<Contributor> contributors) {
        this.contributors = contributors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContributorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_traveler_contribution, parent, false);
        return new ContributorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContributorViewHolder holder, int position) {
        Contributor contributor = contributors.get(position);
        holder.tvInitials.setText(contributor.getInitials());
        holder.tvName.setText(contributor.getName());
        holder.tvAmount.setText("৳ " + contributor.getAmountPaid());
        
        // For simplicity, just use status
        if ("Paid".equalsIgnoreCase(contributor.getStatus())) {
            holder.tvStatus.setText("Fully Paid");
            holder.tvStatus.setTextColor(0xFF4CAF50); // Green
        } else {
            holder.tvStatus.setText("Due: ৳ " + contributor.getTotalDue());
            holder.tvStatus.setTextColor(0xFFE65100); // Orange
        }
    }

    @Override
    public int getItemCount() {
        return contributors != null ? contributors.size() : 0;
    }

    static class ContributorViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvAmount, tvStatus;

        public ContributorViewHolder(@NonNull View itemView) {
            super(itemView);
            // Assuming IDs based on standard naming convention
            tvInitials = itemView.findViewById(R.id.tvInitials);
            tvName = itemView.findViewById(R.id.tvName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
