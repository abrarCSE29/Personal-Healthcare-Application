package com.example.personalhealthcareapplication.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileUpdateActivity extends AppCompatActivity {

    private TextInputEditText etName, etMobile, etEmail, etDob, etWeight, etHeight;
    private MaterialButton btnSave;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String uid; // User ID from Firebase Authentication
    private DocumentReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        // Initialize views
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);
        etDob = findViewById(R.id.etDob);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        btnSave = findViewById(R.id.btnSave);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
            userRef = db.collection("Users").document(uid);
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if the user is not authenticated
        }

        // Fetch existing user data from Firestore (if any)
        fetchUserData();

        // Set the save button action
        btnSave.setOnClickListener(v -> saveUserData());
        etDob.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ProfileUpdateActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        etDob.setText(date);
                    },
                    year, month, day);

            datePickerDialog.show();
        });
    }

    private void fetchUserData() {
        if (userRef != null) {
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null) {
                            etName.setText(user.name);
                            etMobile.setText(user.mobile);
                            etEmail.setText(user.email);
                            etDob.setText(user.dob);
                            etWeight.setText(user.weight);
                            etHeight.setText(user.height);
                        }
                    } else {
                        Toast.makeText(ProfileUpdateActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileUpdateActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveUserData() {
        String name = etName.getText().toString();
        String mobile = etMobile.getText().toString();
        String email = etEmail.getText().toString();
        String dob = etDob.getText().toString();
        String weight = etWeight.getText().toString();
        String height = etHeight.getText().toString();

        // Create a new user object with the updated data
        User user = new User(name, mobile, email, dob, weight, height, null);

        // Save the user data to Firestore
        if (userRef != null) {
            userRef.set(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileUpdateActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        // Redirect to Home screen
                        Intent intent = new Intent(ProfileUpdateActivity.this, Home.class);
                        startActivity(intent);
                        finish(); // Close ProfileUpdateActivity
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ProfileUpdateActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "User reference not found", Toast.LENGTH_SHORT).show();
        }
    }
}
