package com.example.personalhealthcareapplication.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextInputEditText etName, etMobile, etEmail, etDob, etWeight, etHeight;
    private MaterialButton btnSave;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String uid; // Assuming the UID is passed from another fragment or activity
    private DocumentReference userRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        etName = view.findViewById(R.id.etName);
        etMobile = view.findViewById(R.id.etMobile);
        etEmail = view.findViewById(R.id.etEmail);
        etDob = view.findViewById(R.id.etDob);
        etWeight = view.findViewById(R.id.etWeight);
        etHeight = view.findViewById(R.id.etHeight);
        btnSave = view.findViewById(R.id.btnSave);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        System.out.println("The curent usr is  "+ user.getUid());
        // Initialize userRef with the user ID (uid) passed to this fragment
//        if (getArguments() != null) {
//            uid = getArguments().getString("uid");
//            System.out.println(uid);
//        }

        if (uid != null) {
            userRef = db.collection("Users").document(uid);
        } else {
            // Handle error, user ID is missing
            Toast.makeText(getContext(), "User ID not found", Toast.LENGTH_SHORT).show();
        }

        fetchUserData();

        btnSave.setOnClickListener(v -> saveUserData());

        return view;
    }

    private void fetchUserData() {
        if (userRef != null) {
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            etName.setText(user.name);
                            etMobile.setText(user.mobile);
                            etEmail.setText(user.email);
                            etDob.setText(user.dob);
                            etWeight.setText(user.weight);
                            etHeight.setText(user.height);
                        }
                    } else {
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
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

        User user = new User(name, mobile, email, dob, weight, height, null);

        if (userRef != null) {
            userRef.set(user)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show());
        }
    }
}
