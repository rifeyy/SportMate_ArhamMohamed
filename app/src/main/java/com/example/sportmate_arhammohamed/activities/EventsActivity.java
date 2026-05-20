package com.example.sportmate_arhammohamed.activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.sportmate_arhammohamed.R;
import com.example.sportmate_arhammohamed.models.Event;
import com.example.sportmate_arhammohamed.network.ApiClient;
import com.example.sportmate_arhammohamed.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsActivity extends AppCompatActivity {

    private LinearLayout eventsContainer;
    private Button btnVoiceSearch;
    private ApiService apiService;

    private List<Event> allEvents = new ArrayList<>();

    private final ActivityResultLauncher<String> audioPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startVoiceSearch();
                        } else {
                            Toast.makeText(
                                    EventsActivity.this,
                                    "Microphone permission denied",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> voiceSearchLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            ArrayList<String> voiceResults =
                                    result.getData().getStringArrayListExtra(
                                            RecognizerIntent.EXTRA_RESULTS
                                    );

                            if (voiceResults != null && !voiceResults.isEmpty()) {
                                String spokenText = voiceResults.get(0);
                                filterEventsByVoice(spokenText);
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        eventsContainer = findViewById(R.id.eventsContainer);
        btnVoiceSearch = findViewById(R.id.btnVoiceSearch);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnVoiceSearch.setOnClickListener(v -> checkAudioPermission());

        loadEventsFromBackend();
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED) {

            startVoiceSearch();

        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Say a sport or event name"
        );

        try {
            voiceSearchLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    this,
                    "Voice recognition is not available on this device",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void loadEventsFromBackend() {
        eventsContainer.removeAllViews();

        TextView loadingView = new TextView(this);
        loadingView.setText("Loading events...");
        loadingView.setTextSize(16);
        loadingView.setTextColor(0xFF555555);
        loadingView.setPadding(24, 24, 24, 24);
        eventsContainer.addView(loadingView);

        apiService.getEvents().enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                eventsContainer.removeAllViews();

                if (response.isSuccessful() && response.body() != null) {
                    allEvents = response.body();

                    if (allEvents.isEmpty()) {
                        showEmptyMessage();
                    } else {
                        displayEvents(allEvents);
                    }

                } else {
                    Toast.makeText(
                            EventsActivity.this,
                            "Server response error",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                eventsContainer.removeAllViews();

                Toast.makeText(
                        EventsActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();

                showErrorMessage();
            }
        });
    }

    private void showEmptyMessage() {
        TextView emptyView = new TextView(this);
        emptyView.setText("No events found from backend.");
        emptyView.setTextSize(16);
        emptyView.setTextColor(0xFF555555);
        emptyView.setPadding(24, 24, 24, 24);
        eventsContainer.addView(emptyView);
    }

    private void showErrorMessage() {
        TextView errorView = new TextView(this);
        errorView.setText("Could not load events. Check backend connection.");
        errorView.setTextSize(16);
        errorView.setTextColor(0xFFB00020);
        errorView.setPadding(24, 24, 24, 24);
        eventsContainer.addView(errorView);
    }

    private void filterEventsByVoice(String query) {
        if (query == null || query.trim().isEmpty()) {
            displayEvents(allEvents);
            return;
        }

        String searchText = query.toLowerCase(Locale.ROOT).trim();

        if (searchText.equals("all")
                || searchText.equals("show all")
                || searchText.equals("reset")) {

            Toast.makeText(
                    this,
                    "Showing all events",
                    Toast.LENGTH_SHORT
            ).show();

            displayEvents(allEvents);
            return;
        }

        List<Event> filteredEvents = new ArrayList<>();

        for (Event event : allEvents) {
            String title = safeLower(event.getTitle());
            String sport = safeLower(event.getSport());
            String location = safeLower(event.getLocation());
            String level = safeLower(event.getLevel());

            if (title.contains(searchText)
                    || sport.contains(searchText)
                    || location.contains(searchText)
                    || level.contains(searchText)) {

                filteredEvents.add(event);
            }
        }

        eventsContainer.removeAllViews();

        TextView resultView = new TextView(this);
        resultView.setText("Voice search: " + query);
        resultView.setTextSize(16);
        resultView.setTypeface(null, Typeface.BOLD);
        resultView.setTextColor(0xFF0B6623);
        resultView.setPadding(0, 0, 0, 16);
        eventsContainer.addView(resultView);

        if (filteredEvents.isEmpty()) {
            TextView noResultView = new TextView(this);
            noResultView.setText("No events match your voice search.");
            noResultView.setTextSize(16);
            noResultView.setTextColor(0xFF555555);
            noResultView.setPadding(24, 24, 24, 24);
            eventsContainer.addView(noResultView);
        } else {
            displayEvents(filteredEvents);
        }
    }

    private String safeLower(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase(Locale.ROOT);
    }

    private void displayEvents(List<Event> events) {
        eventsContainer.removeAllViews();

        for (Event event : events) {
            TextView textView = new TextView(this);

            String text = event.getTitle()
                    + "\nSport: " + event.getSport()
                    + "\nLocation: " + event.getLocation()
                    + "\nDate: " + event.getDate() + " at " + event.getTime()
                    + "\nPlayers: " + event.getCurrentPlayers() + "/" + event.getMaxPlayers()
                    + "\nLevel: " + event.getLevel();

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

            textView.setOnClickListener(v -> {
                Intent detailsIntent = new Intent(
                        EventsActivity.this,
                        EventDetailsActivity.class
                );

                detailsIntent.putExtra("eventId", event.get_id());
                detailsIntent.putExtra("title", event.getTitle());
                detailsIntent.putExtra("sport", event.getSport());
                detailsIntent.putExtra("location", event.getLocation());
                detailsIntent.putExtra("date", event.getDate());
                detailsIntent.putExtra("time", event.getTime());
                detailsIntent.putExtra("level", event.getLevel());
                detailsIntent.putExtra("maxPlayers", event.getMaxPlayers());
                detailsIntent.putExtra("currentPlayers", event.getCurrentPlayers());

                startActivity(detailsIntent);
            });

            eventsContainer.addView(textView);
        }
    }
}