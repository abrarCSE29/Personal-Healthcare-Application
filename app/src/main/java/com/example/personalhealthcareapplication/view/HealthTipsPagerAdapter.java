package com.example.personalhealthcareapplication.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalhealthcareapplication.R;

import java.util.List;

public class HealthTipsPagerAdapter extends RecyclerView.Adapter<HealthTipsPagerAdapter.HealthTipsViewHolder> {

    private final List<String> healthTips;

    public HealthTipsPagerAdapter(List<String> healthTips) {
        this.healthTips = healthTips;
    }

    @NonNull
    @Override
    public HealthTipsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_health_tip, parent, false);
        return new HealthTipsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthTipsViewHolder holder, int position) {
        holder.tipTextView.setText(healthTips.get(position));
    }

    @Override
    public int getItemCount() {
        return healthTips.size();
    }

    static class HealthTipsViewHolder extends RecyclerView.ViewHolder {
        TextView tipTextView;

        public HealthTipsViewHolder(@NonNull View itemView) {
            super(itemView);
            tipTextView = itemView.findViewById(R.id.tip_text);
        }
    }
}
