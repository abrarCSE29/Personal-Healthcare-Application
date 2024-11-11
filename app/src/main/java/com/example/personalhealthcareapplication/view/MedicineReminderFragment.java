package com.example.personalhealthcareapplication.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.MedicineReminder;
import com.example.personalhealthcareapplication.model.ReminderEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MedicineReminderFragment extends Fragment implements MedicineReminderAdapter.OnMedicineClickListener {

    private RecyclerView recyclerView;
    private MedicineReminderAdapter adapter;
    private List<MedicineReminder> medicineReminders;
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicine_reminder, container, false);
        recyclerView = view.findViewById(R.id.rvMedicineReminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set up RecyclerView adapter
        medicineReminders = new ArrayList<>();
        adapter = new MedicineReminderAdapter(medicineReminders, this);
        recyclerView.setAdapter(adapter);

        // Load medicine reminders from Firestore
        loadMedicineReminders();

        // Floating Action Button to add new medicine reminder
        FloatingActionButton fabAddReminder = view.findViewById(R.id.fabAddMedicineReminder);
        fabAddReminder.setOnClickListener(v -> openAddReminderDialog(null));

        return view;
    }

    private void loadMedicineReminders() {
        CollectionReference remindersRef = db.collection("Users")
                .document(userId)
                .collection("MedicineReminders");

        remindersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                medicineReminders.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    MedicineReminder reminder = document.toObject(MedicineReminder.class);
                    reminder.setId(document.getId()); // Set document ID
                    medicineReminders.add(reminder);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Failed to load reminders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMedicineClick(MedicineReminder medicineReminder) {
        // Show details of the selected medicine reminder
        showMedicineDetailsDialog(medicineReminder);
    }

    private void showMedicineDetailsDialog(MedicineReminder medicineReminder) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_medicine_details, null);

        TextView tvMedicineName = dialogView.findViewById(R.id.tvMedicineDetailsName);
        TextView tvQuantity = dialogView.findViewById(R.id.tvMedicineDetailsQuantity);
        RecyclerView rvReminderTimes = dialogView.findViewById(R.id.rvMedicineReminderDetails);

        // Populate fields with medicine details
        tvMedicineName.setText(medicineReminder.getMedicineName());
        tvQuantity.setText(getFormattedQuantities(medicineReminder)); // Use a method to format quantities

        // Set up RecyclerView for reminder times
        rvReminderTimes.setLayoutManager(new LinearLayoutManager(getContext()));
        ReminderTimesAdapter timesAdapter = new ReminderTimesAdapter(medicineReminder.getReminders());
        rvReminderTimes.setAdapter(timesAdapter);

        // Show the details dialog
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Medicine Details")
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getFormattedQuantities(MedicineReminder medicineReminder) {
        StringBuilder formattedQuantities = new StringBuilder();
        for (ReminderEntry entry : medicineReminder.getReminders()) {
            formattedQuantities.append(entry.getQuantity()).append(" at ").append(entry.getFormattedTime()).append("\n");
        }
        return formattedQuantities.toString();
    }

    private void openAddReminderDialog(MedicineReminder medicineReminder) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_medicine_reminder, null);

        EditText etMedicineName = dialogView.findViewById(R.id.etMedicineName);
        // No longer using quantity input here since it's now part of ReminderEntry

        // Populate fields if editing
        if (medicineReminder != null) {
            etMedicineName.setText(medicineReminder.getMedicineName());
        }

        // Show the Add/Edit Medicine Reminder Dialog
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(medicineReminder == null ? "Add New Medicine Reminder" : "Edit Medicine Reminder")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String medicineName = etMedicineName.getText().toString();

                    if (TextUtils.isEmpty(medicineName)) {
                        Toast.makeText(getContext(), "Please fill the medicine name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (medicineReminder == null) {
                        saveMedicineReminder(new MedicineReminder(medicineName, new ArrayList<>())); // No reminders initially
                    } else {
                        medicineReminder.setMedicineName(medicineName);
                        updateMedicineReminder(medicineReminder);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveMedicineReminder(MedicineReminder medicineReminder) {
        db.collection("Users")
                .document(userId)
                .collection("MedicineReminders")
                .add(medicineReminder)
                .addOnSuccessListener(documentReference -> {
                    medicineReminder.setId(documentReference.getId());
                    medicineReminders.add(medicineReminder);
                    adapter.notifyItemInserted(medicineReminders.size() - 1);
                    Toast.makeText(getContext(), "Medicine reminder added", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add medicine reminder", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateMedicineReminder(MedicineReminder medicineReminder) {
        db.collection("Users")
                .document(userId)
                .collection("MedicineReminders")
                .document(medicineReminder.getId())
                .set(medicineReminder)
                .addOnSuccessListener(aVoid -> {
                    // Reload and update reminders in RecyclerView
                    loadMedicineReminders();
                    Toast.makeText(getContext(), "Medicine reminder updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update medicine reminder", Toast.LENGTH_SHORT).show());
    }
}
