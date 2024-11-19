package com.example.personalhealthcareapplication.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.MedicineReminder;
import com.example.personalhealthcareapplication.viewmodel.MedicineReminderViewModel;

import java.util.List;

public class MedicineReminderAdapter extends RecyclerView.Adapter<MedicineReminderAdapter.ViewHolder> {
    private List<MedicineReminder> reminders;
    private MedicineReminderViewModel viewModel;
    private Context context;

    public MedicineReminderAdapter(List<MedicineReminder> reminders, MedicineReminderViewModel viewModel, Context context) {
        this.reminders = reminders;
        this.viewModel = viewModel;
        this.context = context;
    }

    public void setReminders(List<MedicineReminder> newReminders) {
        this.reminders = newReminders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicineReminder reminder = reminders.get(position);
        holder.tvMedicineName.setText(reminder.getMedicineName());
        holder.tvQuantity.setText(reminder.getQuantity());
        holder.tvTime.setText(reminder.getFormattedTime());

        // Set up delete button functionality
        holder.btnDelete.setOnClickListener(v -> deleteReminder(reminder, position));
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    private void deleteReminder(MedicineReminder reminder, int position) {
        // Remove reminder from local list
        reminders.remove(position);
        notifyItemRemoved(position);

        // Remove reminder from database via ViewModel
        viewModel.deleteReminder(reminder);

        // Cancel the reminder notification
        viewModel.cancelMedicineReminder(context, reminder);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName;
        TextView tvQuantity;
        TextView tvTime;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
