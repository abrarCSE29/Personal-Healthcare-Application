package com.example.personalhealthcareapplication.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.adapter.ChatAdapter;
import com.example.personalhealthcareapplication.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private EditText userInput;
    private ImageButton sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Initialize views
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        userInput = findViewById(R.id.user_input);
        sendButton = findViewById(R.id.send_button);

        // Initialize chat message list and adapter
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Set click listener for the send button
        sendButton.setOnClickListener(v -> {
            String message = userInput.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                addMessage(message, true); // User message
                generateBotResponse(message);
                userInput.setText("");
            } else {
                Toast.makeText(ChatbotActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMessage(String message, boolean isUser) {
        chatMessageList.add(new ChatMessage(message, isUser));
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        chatRecyclerView.smoothScrollToPosition(chatMessageList.size() - 1);
    }

    private void generateBotResponse(String userMessage) {
        // Basic response logic
        String botResponse;
        if (userMessage.toLowerCase().contains("appointment")) {
            botResponse = "To set an appointment reminder, navigate to the Appointments section.";
        } else if (userMessage.toLowerCase().contains("medication")) {
            botResponse = "To manage your medications, navigate to the Medication section.";
        } else if (userMessage.toLowerCase().contains("emergency")) {
            botResponse = "In case of emergencies, dial 999 or visit the nearest hospital.";
        } else {
            botResponse = "I'm sorry, I couldn't understand that. Please try rephrasing your question.";
        }
        addMessage(botResponse, false); // Bot message
    }
}
