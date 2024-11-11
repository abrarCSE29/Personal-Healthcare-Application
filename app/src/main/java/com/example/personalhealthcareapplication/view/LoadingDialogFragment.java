package com.example.personalhealthcareapplication.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.personalhealthcareapplication.R;

public class LoadingDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";
    private String message;

    // Static factory method to create a new instance with a custom message
    public static LoadingDialogFragment newInstance(String message) {
        LoadingDialogFragment fragment = new LoadingDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the message from arguments if available
        if (getArguments() != null) {
            message = getArguments().getString(ARG_MESSAGE, "Please wait");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_loading, container, false);
        TextView tvLoadingMessage = view.findViewById(R.id.tvLoadingMessage);
        tvLoadingMessage.setText(message);  // Set the message here
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().setCancelable(false);
        }
    }
}
