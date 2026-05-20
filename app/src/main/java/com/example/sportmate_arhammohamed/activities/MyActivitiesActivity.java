package com.example.sportmate_arhammohamed.activities;

import android.graphics.Typeface;
import android.os.Bundle;
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

public class MyActivitiesActivity extends AppCompatActivity {

    private LinearLayout activitiesContainer;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_activities);

        activitiesContainer = findViewById(R.id.activitiesContainer);
        apiService = ApiClient.getClient().create(ApiService.class);

        loadActivitiesFromBackend();
    }

    private void loadActivitiesFromBackend() {
        activitiesContainer.removeAllViews();

        TextView titleView = new TextView(this);
        titleView.setText("My Activities");
        titleView.setTextSize(24);
        titleView.setTextColor(0xFF0B6623);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setPadding(0, 24, 0, 20);
        activitiesContainer.addView(titleView);

        apiService.getActivities().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(
                    Call<List<Map<String, Object>>> call,
                    Response<List<Map<String, Object>>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> activities = response.body();

                    if (activities.isEmpty()) {
                        showEmptyMessage();
                    } else {
                        displayActivities(activities);
                    }
                } else {
                    Toast.makeText(
                            MyActivitiesActivity.this,
                            "Server response error",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<List<Map<String, Object>>> call,
                    Throwable t
            ) {
                Toast.makeText(
                        MyActivitiesActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showEmptyMessage() {
        TextView emptyView = new TextView(this);
        emptyView.setText("No joined activities yet.");
        emptyView.setTextSize(16);
        emptyView.setTextColor(0xFF555555);
        emptyView.setPadding(24, 24, 24, 24);
        activitiesContainer.addView(emptyView);
    }

    private void displayActivities(List<Map<String, Object>> activities) {
        for (Map<String, Object> activity : activities) {

            String title = getStringValue(activity, "title");
            String sport = getStringValue(activity, "sport");
            String location = getStringValue(activity, "location");
            String date = getStringValue(activity, "date");
            String time = getStringValue(activity, "time");
            String level = getStringValue(activity, "level");
            String status = getStringValue(activity, "status");

            addActivityCard(title, sport, location, date, time, level, status);
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString();
    }

    private void addActivityCard(
            String title,
            String sport,
            String location,
            String date,
            String time,
            String level,
            String status
    ) {

        TextView textView = new TextView(this);

        String text = title
                + "\nSport: " + sport
                + "\nLocation: " + location
                + "\nDate: " + date + " at " + time
                + "\nLevel: " + level
                + "\nStatus: " + status;

        textView.setText(text);
        textView.setTextSize(16);
        textView.setTextColor(0xFF212121);
        textView.setPadding(24, 24, 24, 24);
        textView.setBackgroundColor(0xFFE8F5E9);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        textView.setLayoutParams(params);

        activitiesContainer.addView(textView);
    }
}