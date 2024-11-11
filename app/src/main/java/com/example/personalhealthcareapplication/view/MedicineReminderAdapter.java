package com.example.personalhealthcareapplication.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.MedicineReminder;
import com.example.personalhealthcareapplication.model.ReminderEntry;

import java.util.ArrayList;
import java.util.List;

public class MedicineReminderAdapter extends RecyclerView.Adapter<MedicineReminderAdapter.ViewHolder> {

    private final List<MedicineReminder> medicineReminders;
    private final OnMedicineClickListener listener;

    public interface OnMedicineClickListener {
        void onMedicineClick(MedicineReminder medicineReminder);
    }

    public MedicineReminderAdapter(List<MedicineReminder> medicineReminders, OnMedicineClickListener listener) {
        this.medicineReminders = medicineReminders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicineReminder medicineReminder = medicineReminders.get(position);
        holder.bind(medicineReminder, listener);
    }

    @Override
    public int getItemCount() {
        return medicineReminders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMedicineName;
        private final TextView tvQuantity;
        private final TextView tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(MedicineReminder medicineReminder, OnMedicineClickListener listener) {
            tvMedicineName.setText(medicineReminder.getMedicineName());
            // Check for null reminders list
            if (medicineReminder.getReminders() == null) {
                // Initialize it to avoid NullPointerException
                medicineReminder.setReminders(new ArrayList<>());
            }
            // Show first reminder as example; you might want to handle this differently
            if (!medicineReminder.getReminders().isEmpty()) {
                ReminderEntry firstReminder = medicineReminder.getReminders().get(0);
                tvQuantity.setText(firstReminder.getQuantity());
                tvTime.setText(firstReminder.getFormattedTime());
            }
            itemView.setOnClickListener(v -> listener.onMedicineClick(medicineReminder));
        }
    }
}
