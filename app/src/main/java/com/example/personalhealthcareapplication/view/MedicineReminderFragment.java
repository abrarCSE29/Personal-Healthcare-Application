package com.example.personalhealthcareapplication.view;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.MedicineReminder;
import com.example.personalhealthcareapplication.view.MedicineReminderAdapter;
import com.example.personalhealthcareapplication.viewmodel.MedicineReminderViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MedicineReminderFragment extends Fragment {
    private RecyclerView recyclerView;
    private MedicineReminderAdapter adapter;
    private MedicineReminderViewModel viewModel;
    private List<MedicineReminder> reminders;
    private TimeAdapter timeAdapter;
    private RecyclerView rvReminderTimes;
    private List<Long> reminderTimes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicine_reminder, container, false);

        recyclerView = view.findViewById(R.id.rvMedicineReminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fabAddReminder = view.findViewById(R.id.fabAddMedicineReminder);

        reminders = new ArrayList<>();
        adapter = new MedicineReminderAdapter(reminders);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MedicineReminderViewModel.class);

        fabAddReminder.setOnClickListener(v -> openAddReminderDialog());
        adapter.setOnMedicineClickListener(this::showMedicineDetailsDialog);
        loadReminders();

        return view;
    }

    private void openAddReminderDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_medicine_reminder, null);

        EditText etMedicineName = dialogView.findViewById(R.id.etMedicineName);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        Spinner spnMedicineType = dialogView.findViewById(R.id.spnMedicineType);
        Button btnAddTime = dialogView.findViewById(R.id.btnAddTime);
        rvReminderTimes = dialogView.findViewById(R.id.rvReminderTimes);

        reminderTimes.clear();
        timeAdapter = new TimeAdapter(reminderTimes); // Custom adapter implementation needed
        rvReminderTimes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReminderTimes.setAdapter(timeAdapter);

        Calendar calendar = Calendar.getInstance();

        btnAddTime.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getContext(),
                    (view, hourOfDay, minute1) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute1);
                        long timeInMillis = calendar.getTimeInMillis();
                        reminderTimes.add(timeInMillis);
                        timeAdapter.notifyDataSetChanged();
                    },
                    hour,
                    minute,
                    false
            );
            timePickerDialog.show();
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Add New Medicine Reminder")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String medicineName = etMedicineName.getText().toString();
                    String quantity = etQuantity.getText().toString();
                    String medicineType = spnMedicineType.getSelectedItem().toString();

                    if (TextUtils.isEmpty(medicineName) || TextUtils.isEmpty(quantity) || reminderTimes.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill all fields and add at least one time", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (long reminderTime : reminderTimes) {
                        MedicineReminder reminder = new MedicineReminder(medicineName, quantity + " " + medicineType, reminderTime);
                        viewModel.saveReminder(reminder);
                        viewModel.scheduleMedicineReminder(getContext(), reminder);
                    }

                    loadReminders(); // Refresh RecyclerView in fragment
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showMedicineDetailsDialog(MedicineReminder medicine) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_medicine_details, null);

        TextView tvMedicineDetailsName = dialogView.findViewById(R.id.tvMedicineDetailsName);
        TextView tvMedicineDetailsQuantity = dialogView.findViewById(R.id.tvMedicineDetailsQuantity);
        RecyclerView rvReminderTimes = dialogView.findViewById(R.id.rvMedicineReminderDetails);
        Button btnAddReminderTime = dialogView.findViewById(R.id.btnAddReminderTime);

        tvMedicineDetailsName.setText(medicine.getMedicineName());
        tvMedicineDetailsQuantity.setText(medicine.getQuantity());

        List<Long> reminderTimes = new ArrayList<>(/* Get associated times for the medicine */);
        TimeAdapter timeAdapter = new TimeAdapter(reminderTimes);
        rvReminderTimes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReminderTimes.setAdapter(timeAdapter);

        btnAddReminderTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getContext(),
                    (view, hourOfDay, minute1) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute1);
                        long timeInMillis = calendar.getTimeInMillis();
                        reminderTimes.add(timeInMillis);
                        timeAdapter.notifyDataSetChanged();

                        // Save the new reminder to the database
                        MedicineReminder newReminder = new MedicineReminder(
                                medicine.getMedicineName(),
                                medicine.getQuantity(),
                                timeInMillis
                        );
                        viewModel.saveReminder(newReminder);
                        viewModel.scheduleMedicineReminder(getContext(), newReminder);
                    },
                    hour,
                    minute,
                    false
            );
            timePickerDialog.show();
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Medicine Details")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void loadReminders() {
        viewModel.getReminders().addOnSuccessListener(reminderList -> {
            if (reminderList != null) {
                reminders.clear();
                reminders.addAll(reminderList);
                adapter.setReminders(reminders);
                Log.d("MedicineReminderFragment", "Reminders loaded and adapter notified. Size: " + reminders.size());
            }
        }).addOnFailureListener(e -> Log.e("MedicineReminderFragment", "Error loading reminders", e));
    }
}
