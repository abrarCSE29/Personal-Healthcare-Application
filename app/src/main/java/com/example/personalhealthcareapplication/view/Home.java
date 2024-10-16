package com.example.personalhealthcareapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.personalhealthcareapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class Home extends AppCompatActivity {

    private TextView tvWelcome, tvEmail, tvMobile, tvDOB, tvWeight, tvHeight, tvMedicalCondition;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase and Views
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvWelcome = findViewById(R.id.tvWelcome);
        tvEmail = findViewById(R.id.tvEmail);
        tvMobile = findViewById(R.id.tvMobile);
        tvDOB = findViewById(R.id.tvDOB);
        tvWeight = findViewById(R.id.tvWeight);
        tvHeight = findViewById(R.id.tvHeight);
        tvMedicalCondition = findViewById(R.id.tvMedicalCondition);

        // Fetch user data if the user is logged in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            fetchUserInfo(user.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        }
    }

    private void fetchUserInfo(String uid) {
        db.collection("Users").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String name = document.getString("name");
                            String email = document.getString("email");
                            String mobile = document.getString("mobile");
                            String dob = document.getString("dob");
                            String weight = document.getString("weight");
                            String height = document.getString("height");
                            List<String> medicalConditions = (List<String>) document.get("medicalConditions");

                            String conditionsText = medicalConditions != null
                                    ? String.join(", ", medicalConditions)
                                    : "No medical conditions listed";

                            // Set TextViews with the fetched data
                            tvWelcome.setText("Welcome, " + name);
                            tvEmail.setText("Email: " + email);
                            tvMobile.setText("Mobile: " + mobile);
                            tvDOB.setText("DOB: " + dob);
                            tvWeight.setText("Weight: " + weight + " kg");
                            tvHeight.setText("Height: " + height + " cm");
                            tvMedicalCondition.setText("Medical Condition: " + conditionsText);
                        } else {
                            Log.d("Home", "No such document");
                            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Home", "Error getting document", task.getException());
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(Home.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close Home Activity
    }
}
