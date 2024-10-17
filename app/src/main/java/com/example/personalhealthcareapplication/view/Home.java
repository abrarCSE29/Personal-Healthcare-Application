package com.example.personalhealthcareapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.personalhealthcareapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class Home extends AppCompatActivity {

    private TextView tvWelcome, tvEmail, tvMobile, tvDOB, tvWeight, tvHeight, tvMedicalCondition,tvDrawerName,tvDrawerWelcome;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        // Access the header views
        View headerView = navigationView.getHeaderView(0);
        tvDrawerName = headerView.findViewById(R.id.tvDrawerName);
        tvDrawerWelcome = headerView.findViewById(R.id.tvDrawerWelcome);

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


        // Set up the AppBar with a navigation icon
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Set up the navigation item selection listener
        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationItemSelected(item);
            return true;
        });

        // Fetch user data if the user is logged in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            fetchUserInfo(user.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        }
    }

    private void handleNavigationItemSelected(@NonNull MenuItem item) {
        // Handle different menu item clicks using if-else
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (itemId == R.id.nav_profile) {
            Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (itemId == R.id.nav_logout) {
            logoutUser();
        } else {
            Toast.makeText(this, "Unknown item selected", Toast.LENGTH_SHORT).show();
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
                            tvDrawerName.setText(name);
                            tvDrawerWelcome.setText("Welcome");
                            //tvWelcome.setText("Welcome, " + name);
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

    private void logoutUser() {
        mAuth.signOut();
        navigateToLogin();
    }
}
