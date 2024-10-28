package com.example.personalhealthcareapplication.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.Appointment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AppointmentFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private List<Appointment> appointments;
    private FirebaseFirestore db;
    private String userId;

    public AppointmentFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fabAddAppointment = view.findViewById(R.id.fabAddAppointment);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set up RecyclerView adapter
        appointments = new ArrayList<>();
        adapter = new AppointmentAdapter(appointments);
        recyclerView.setAdapter(adapter);

        // Load appointments from Firestore
        loadAppointments();

        // Add new appointment when FAB is clicked
        fabAddAppointment.setOnClickListener(v -> openAddAppointmentDialog());

        return view;
    }

    private void loadAppointments() {
        CollectionReference appointmentsRef = db.collection("Users")
                .document(userId)
                .collection("Appointments");

        appointmentsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                appointments.clear();
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Appointment appointment = document.toObject(Appointment.class);
                        appointments.add(appointment);
                    }
                    adapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(getContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAddAppointmentDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_appointment, null);

        EditText etDoctorName = dialogView.findViewById(R.id.etDoctorName);
        EditText etDate = dialogView.findViewById(R.id.etDate);
        EditText etTime = dialogView.findViewById(R.id.etTime);

        Calendar calendar = Calendar.getInstance();

        // Set Date Picker
        etDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year1, month1, dayOfMonth) -> {
                        String date = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        etDate.setText(date);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        // Set Time Picker
        etTime.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getContext(),
                    (view, hourOfDay, minute1) -> {
                        String time = String.format("%02d:%02d", hourOfDay, minute1);
                        etTime.setText(time);
                    },
                    hour, minute, true);
            timePickerDialog.show();
        });

        // Show the Add Appointment Dialog
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Add New Appointment")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String doctorName = etDoctorName.getText().toString();
                    String date = etDate.getText().toString();
                    String time = etTime.getText().toString();

                    if (TextUtils.isEmpty(doctorName) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
                        Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Save appointment to Firestore
                    saveAppointment(new Appointment(doctorName, date, time));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveAppointment(Appointment appointment) {
        db.collection("Users")
                .document(userId)
                .collection("Appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    appointments.add(appointment);
                    adapter.notifyItemInserted(appointments.size() - 1);
                    Toast.makeText(getContext(), "Appointment added", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add appointment", Toast.LENGTH_SHORT).show();
                });
    }
}
