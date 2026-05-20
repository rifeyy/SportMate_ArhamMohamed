package com.example.sportmate_arhammohamed.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportmate_arhammohamed.R;
import com.example.sportmate_arhammohamed.utils.FirebaseLogger;

public class HomeActivity extends AppCompatActivity {

    private Button btnFindMatch;
    private Button btnCreateEvent;
    private Button btnNearbyFields;
    private Button btnMyActivities;
    private Button btnQrScanner;
    private Button btnAiRecommendations;
    private Button btnAiChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseLogger.logScreen(this, "Home Screen");
        FirebaseLogger.backupAppAction("open_home", "User opened Home screen");

        btnFindMatch = findViewById(R.id.btnFindMatch);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnNearbyFields = findViewById(R.id.btnNearbyFields);
        btnMyActivities = findViewById(R.id.btnMyActivities);
        btnQrScanner = findViewById(R.id.btnQrScanner);
        btnAiRecommendations = findViewById(R.id.btnAiRecommendations);
        btnAiChat = findViewById(R.id.btnAiChat);

        btnFindMatch.setOnClickListener(v -> {
            startActivity(new Intent(this, EventsActivity.class));
        });

        btnCreateEvent.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateEventActivity.class));
        });

        btnNearbyFields.setOnClickListener(v -> {
            startActivity(new Intent(this, NearbyFieldsActivity.class));
        });

        btnMyActivities.setOnClickListener(v -> {
            startActivity(new Intent(this, MyActivitiesActivity.class));
        });

        btnQrScanner.setOnClickListener(v -> {
            startActivity(new Intent(this, QrScannerActivity.class));
        });

        btnAiRecommendations.setOnClickListener(v -> {
            startActivity(new Intent(this, AiRecommendationActivity.class));
        });

        // âœ… AI Chat Assistant
        // ÙŠÙØªØ­ AiChatActivity Ù…Ø¨Ø§Ø´Ø±Ø©
        // ÙˆÙÙŠÙ‡Ø§ Ø²Ø± â˜° Ù„Ø¹Ø±Ø¶ conversations Ø§Ù„Ù‚Ø¯ÙŠÙ…Ø©
        btnAiChat.setOnClickListener(v -> {
            startActivity(new Intent(this, AiChatActivity.class));
        });
    }
}

