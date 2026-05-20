package com.example.sportmate_arhammohamed.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportmate_arhammohamed.R;
import com.example.sportmate_arhammohamed.network.ApiClient;
import com.example.sportmate_arhammohamed.network.ApiService;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiRecommendationActivity extends AppCompatActivity {

    private EditText edtSport, edtLevel, edtLocation;
    private Button btnGetRecommendations;
    private LinearLayout resultsContainer;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_recommendation);

        edtSport = findViewById(R.id.edtSport);
        edtLevel = findViewById(R.id.edtLevel);
        edtLocation = findViewById(R.id.edtLocation);
        btnGetRecommendations = findViewById(R.id.btnGetRecommendations);
        resultsContainer = findViewById(R.id.resultsContainer);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnGetRecommendations.setOnClickListener(v -> getAiRecommendations());
    }

    private void getAiRecommendations() {
        String sport = edtSport.getText().toString().trim();
        String level = edtLevel.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();

        if (TextUtils.isEmpty(sport)) {
            edtSport.setError("Enter sport");
            edtSport.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(level)) {
            edtLevel.setError("Enter level");
            edtLevel.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(location)) {
            edtLocation.setError("Enter location");
            edtLocation.requestFocus();
            return;
        }

        resultsContainer.removeAllViews();

        TextView loadingView = new TextView(this);
        loadingView.setText("AI is analyzing sport events...");
        loadingView.setTextSize(16);
        loadingView.setTextColor(0xFF555555);
        loadingView.setPadding(16, 16, 16, 16);
        resultsContainer.addView(loadingView);

        apiService.getRecommendations(sport, level, location)
                .enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(
                            Call<List<Map<String, Object>>> call,
                            Response<List<Map<String, Object>>> response
                    ) {
                        resultsContainer.removeAllViews();

                        if (response.isSuccessful() && response.body() != null) {
                            List<Map<String, Object>> recommendations = response.body();

                            if (recommendations.isEmpty()) {
                                showNoRecommendations();
                            } else {
                                displayRecommendations(recommendations);
                            }

                        } else {
                            Toast.makeText(
                                    AiRecommendationActivity.this,
                                    "Server response error",
                                    Toast.LENGTH_LONG
                            ).show();

                            showErrorMessage();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        resultsContainer.removeAllViews();

                        Toast.makeText(
                                AiRecommendationActivity.this,
                                "Connection error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        showErrorMessage();
                    }
                });
    }

    private void showNoRecommendations() {
        TextView textView = new TextView(this);
        textView.setText("No recommended events found for these preferences.");
        textView.setTextSize(16);
        textView.setTextColor(0xFF555555);
        textView.setPadding(16, 16, 16, 16);
        resultsContainer.addView(textView);
    }

    private void showErrorMessage() {
        TextView textView = new TextView(this);
        textView.setText("Could not load AI recommendations. Check backend connection.");
        textView.setTextSize(16);
        textView.setTextColor(0xFFB00020);
        textView.setPadding(16, 16, 16, 16);
        resultsContainer.addView(textView);
    }

    private void displayRecommendations(List<Map<String, Object>> recommendations) {
        TextView titleView = new TextView(this);
        titleView.setText("Recommended Events");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF0B6623);
        titleView.setPadding(0, 8, 0, 16);
        titleView.setTypeface(null, Typeface.BOLD);
        resultsContainer.addView(titleView);

        for (Map<String, Object> event : recommendations) {
            addRecommendationCard(event);
        }
    }

    private void addRecommendationCard(Map<String, Object> event) {
        String title = getValue(event, "title");
        String sport = getValue(event, "sport");
        String location = getValue(event, "location");
        String date = getValue(event, "date");
        String time = getValue(event, "time");
        String level = getValue(event, "level");
        String score = getValue(event, "score");
        String maxPlayers = getValue(event, "maxPlayers");
        String currentPlayers = getValue(event, "currentPlayers");

        if (title.isEmpty()) {
            title = "Untitled Event";
        }

        if (score.isEmpty()) {
            score = "N/A";
        }

        if (currentPlayers.isEmpty()) {
            currentPlayers = "0";
        }

        if (maxPlayers.isEmpty()) {
            maxPlayers = "0";
        }

        TextView textView = new TextView(this);

        String text = title
                + "\nSport: " + sport
                + "\nLocation: " + location
                + "\nDate: " + date + " at " + time
                + "\nLevel: " + level
                + "\nPlayers: " + currentPlayers + "/" + maxPlayers
                + "\nAI Score: " + score + "%";

        textView.setText(text);
        textView.setTextSize(16);
        textView.setTextColor(0xFF212121);
        textView.setPadding(24, 24, 24, 24);
        textView.setBackgroundColor(0xFFE8F5E9);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 20);
        textView.setLayoutParams(params);

        resultsContainer.addView(textView);
    }

    private String getValue(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value == null) {
            return "";
        }

        if (value instanceof Double) {
            double d = (Double) value;

            if (d == Math.floor(d)) {
                return String.valueOf((int) d);
            }
        }

        return String.valueOf(value);
    }
}