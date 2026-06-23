package com.mrdeveloper.mytourplan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
        double amountPaid = contributor.getAmountPaid();
        double totalDue = contributor.getTotalDue();
        double totalCost = amountPaid + totalDue;

        holder.tvInitials.setText(contributor.getInitials());
        holder.tvName.setText(contributor.getName());
        
        holder.tvAmount.setText(String.format(java.util.Locale.US, "৳ %.2f / ৳ %.2f", amountPaid, totalCost));
        
        int actualPercentage = totalCost > 0 ? (int) ((amountPaid / totalCost) * 100) : 0;
        
        holder.tvPercentage.setText(actualPercentage + "%");
        holder.progressBar.setProgress(Math.min(actualPercentage, 100));

        // Format status text and colors
        if ("Paid".equalsIgnoreCase(contributor.getStatus())) {
            holder.tvStatus.setText("সম্পূর্ণ পরিশোধিত");
            holder.tvStatus.setTextColor(0xFF4CAF50); // Green
            holder.tvPercentage.setTextColor(0xFF4CAF50);
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
            holder.tvPersonalDueRefund.setText("পরিশোধিত (কোনো দেনা-পাওনা নেই)");
            holder.tvPersonalDueRefund.setTextColor(0xFF4CAF50);
        } else if ("Refund".equalsIgnoreCase(contributor.getStatus())) {
            holder.tvStatus.setText(String.format(java.util.Locale.US, "ফেরত: ৳ %.2f", Math.abs(totalDue)));
            holder.tvStatus.setTextColor(0xFF4CAF50); // Green
            holder.tvPercentage.setTextColor(0xFF4CAF50);
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
            holder.tvPersonalDueRefund.setText(String.format(java.util.Locale.US, "ফেরত পাবেন: ৳ %,.2f", Math.abs(totalDue)));
            holder.tvPersonalDueRefund.setTextColor(0xFF4CAF50);
        } else {
            holder.tvStatus.setText(String.format(java.util.Locale.US, "বাকি: ৳ %.2f", totalDue));
            holder.tvStatus.setTextColor(0xFFE65100); // Orange
            holder.tvPercentage.setTextColor(0xFFE65100);
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFE65100));
            holder.tvPersonalDueRefund.setText(String.format(java.util.Locale.US, "পরিশোধ করতে হবে: ৳ %,.2f", totalDue));
            holder.tvPersonalDueRefund.setTextColor(0xFFDC2626); // Red
        }
    }

    @Override
    public int getItemCount() {
        return contributors != null ? contributors.size() : 0;
    }

    static class ContributorViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvAmount, tvStatus, tvPercentage, tvPersonalDueRefund;
        ProgressBar progressBar;

        public ContributorViewHolder(@NonNull View itemView) {
            super(itemView);
            // Assuming IDs based on standard naming convention
            tvInitials = itemView.findViewById(R.id.tvInitials);
            tvName = itemView.findViewById(R.id.tvName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvPersonalDueRefund = itemView.findViewById(R.id.tvPersonalDueRefund);
        }
    }
}
