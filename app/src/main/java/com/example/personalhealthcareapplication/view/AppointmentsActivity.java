package com.example.personalhealthcareapplication.view;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.Appointment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentsActivity extends AppCompatActivity {

    private RecyclerView rvAppointments;
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        rvAppointments = findViewById(R.id.rvAppointments);
        Button btnAddAppointment = findViewById(R.id.btnAddAppointment);
        db = FirebaseFirestore.getInstance();

        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(appointmentList);
        rvAppointments.setAdapter(adapter);

        btnAddAppointment.setOnClickListener(v -> showNewAppointmentDialog());

        loadAppointments();
    }

    private void loadAppointments() {
        db.collection("Appointments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        appointmentList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Appointment appointment = document.toObject(Appointment.class);
                            appointmentList.add(appointment);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("AppointmentsActivity", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void showNewAppointmentDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_new_appointment);

        EditText etDoctorName = dialog.findViewById(R.id.etDoctorName);
        EditText etAppointmentDate = dialog.findViewById(R.id.etAppointmentDate);
        EditText etAppointmentTime = dialog.findViewById(R.id.etAppointmentTime);
        Button btnSaveAppointment = dialog.findViewById(R.id.btnSaveAppointment);

        btnSaveAppointment.setOnClickListener(v -> {
            String doctorName = etDoctorName.getText().toString();
            String date = etAppointmentDate.getText().toString();
            String time = etAppointmentTime.getText().toString();

            if (!doctorName.isEmpty() && !date.isEmpty() && !time.isEmpty()) {
                saveAppointment(doctorName, date, time);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void saveAppointment(String doctorName, String date, String time) {
        Map<String, Object> appointment = new HashMap<>();
        appointment.put("doctorName", doctorName);
        appointment.put("date", date);
        appointment.put("time", time);

        db.collection("Appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Appointment added", Toast.LENGTH_SHORT).show();
                    loadAppointments();
                })
                .addOnFailureListener(e -> Log.w("AppointmentsActivity", "Error adding document", e));
    }
}
