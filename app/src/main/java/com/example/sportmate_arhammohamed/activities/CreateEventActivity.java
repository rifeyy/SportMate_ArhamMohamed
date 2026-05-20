package com.example.sportmate_arhammohamed.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportmate_arhammohamed.R;
import com.example.sportmate_arhammohamed.models.Event;
import com.example.sportmate_arhammohamed.network.ApiClient;
import com.example.sportmate_arhammohamed.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateEventActivity extends AppCompatActivity {

    private EditText edtTitle, edtSport, edtLocation, edtDate, edtTime, edtLevel, edtMaxPlayers;
    private Button btnSaveEvent;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        apiService = ApiClient.getClient().create(ApiService.class);

        edtTitle = findViewById(R.id.edtTitle);
        edtSport = findViewById(R.id.edtSport);
        edtLocation = findViewById(R.id.edtLocation);
        edtDate = findViewById(R.id.edtDate);
        edtTime = findViewById(R.id.edtTime);
        edtLevel = findViewById(R.id.edtLevel);
        edtMaxPlayers = findViewById(R.id.edtMaxPlayers);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);

        btnSaveEvent.setOnClickListener(v -> saveEventToBackend());
    }

    private void saveEventToBackend() {
        String title = edtTitle.getText().toString().trim();
        String sport = edtSport.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String time = edtTime.getText().toString().trim();
        String level = edtLevel.getText().toString().trim();
        String maxPlayersText = edtMaxPlayers.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            edtTitle.setError("Enter event title");
            edtTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(sport)) {
            edtSport.setError("Enter sport");
            edtSport.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(location)) {
            edtLocation.setError("Enter location");
            edtLocation.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(date)) {
            edtDate.setError("Enter date");
            edtDate.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(time)) {
            edtTime.setError("Enter time");
            edtTime.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(level)) {
            edtLevel.setError("Enter level");
            edtLevel.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(maxPlayersText)) {
            edtMaxPlayers.setError("Enter max players");
            edtMaxPlayers.requestFocus();
            return;
        }

        int maxPlayers;

        try {
            maxPlayers = Integer.parseInt(maxPlayersText);
        } catch (NumberFormatException e) {
            edtMaxPlayers.setError("Enter a valid number");
            edtMaxPlayers.requestFocus();
            return;
        }

        Event event = new Event(
                title,
                sport,
                location,
                date,
                time,
                level,
                maxPlayers,
                1
        );

        apiService.createEvent(event).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(
                            CreateEventActivity.this,
                            "Event saved to MongoDB",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(CreateEventActivity.this, EventsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(
                            CreateEventActivity.this,
                            "Server error while creating event",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                Toast.makeText(
                        CreateEventActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}