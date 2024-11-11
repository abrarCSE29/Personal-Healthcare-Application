package com.example.personalhealthcareapplication.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.ReminderEntry;

import java.util.List;

public class ReminderTimesAdapter extends RecyclerView.Adapter<ReminderTimesAdapter.ReminderViewHolder> {
    private List<ReminderEntry> reminderTimes;

    public ReminderTimesAdapter(List<ReminderEntry> reminderTimes) {
        this.reminderTimes = reminderTimes;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each reminder time
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        // Bind the reminder time data to the view holder
        ReminderEntry reminder = reminderTimes.get(position);
        holder.tvReminderTime.setText(reminder.getFormattedTime()); // Set the formatted time
    }

    @Override
    public int getItemCount() {
        return reminderTimes.size(); // Return the number of reminders
    }

    // ViewHolder class for individual reminder items
    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvReminderTime;

        ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReminderTime = itemView.findViewById(R.id.tvTime); // Ensure this ID matches the TextView in item_reminder_time.xml
        }
    }
}
