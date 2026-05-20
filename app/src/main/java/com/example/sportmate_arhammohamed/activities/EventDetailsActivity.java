package com.example.sportmate_arhammohamed.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportmate_arhammohamed.R;
import com.example.sportmate_arhammohamed.network.ApiClient;
import com.example.sportmate_arhammohamed.network.ApiService;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsActivity extends AppCompatActivity {

    private TextView txtTitle, txtSport, txtLocation, txtDateTime, txtLevel, txtPlayers, txtQrInfo;
    private Button btnJoinEvent, btnGenerateQr;
    private ImageView imgEventQr;

    private ApiService apiService;

    private String eventId;
    private String title;
    private String sport;
    private String location;
    private String date;
    private String time;
    private String level;
    private int maxPlayers;
    private int currentPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        apiService = ApiClient.getClient().create(ApiService.class);

        txtTitle = findViewById(R.id.txtTitle);
        txtSport = findViewById(R.id.txtSport);
        txtLocation = findViewById(R.id.txtLocation);
        txtDateTime = findViewById(R.id.txtDateTime);
        txtLevel = findViewById(R.id.txtLevel);
        txtPlayers = findViewById(R.id.txtPlayers);
        txtQrInfo = findViewById(R.id.txtQrInfo);

        btnJoinEvent = findViewById(R.id.btnJoinEvent);
        btnGenerateQr = findViewById(R.id.btnGenerateQr);
        imgEventQr = findViewById(R.id.imgEventQr);

        eventId = getIntent().getStringExtra("eventId");
        title = getIntent().getStringExtra("title");
        sport = getIntent().getStringExtra("sport");
        location = getIntent().getStringExtra("location");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        level = getIntent().getStringExtra("level");
        maxPlayers = getIntent().getIntExtra("maxPlayers", 0);
        currentPlayers = getIntent().getIntExtra("currentPlayers", 0);

        txtTitle.setText(title);
        txtSport.setText("Sport: " + sport);
        txtLocation.setText("Location: " + location);
        txtDateTime.setText("Date: " + date + " at " + time);
        txtLevel.setText("Level: " + level);
        txtPlayers.setText("Players: " + currentPlayers + "/" + maxPlayers);

        btnJoinEvent.setOnClickListener(v -> joinEventWithBackend());

        btnGenerateQr.setOnClickListener(v -> generateEventQrCode());
    }

    private void joinEventWithBackend() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentPlayers >= maxPlayers) {
            Toast.makeText(this, "This event is already full", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.joinEvent(eventId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    Toast.makeText(
                            EventDetailsActivity.this,
                            "You joined this event successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(EventDetailsActivity.this, EventsActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(
                            EventDetailsActivity.this,
                            "Server error while joining event",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(
                        EventDetailsActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void generateEventQrCode() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

            Bitmap bitmap = barcodeEncoder.encodeBitmap(
                    eventId,
                    BarcodeFormat.QR_CODE,
                    500,
                    500
            );

            imgEventQr.setImageBitmap(bitmap);
            imgEventQr.setVisibility(View.VISIBLE);

            txtQrInfo.setText("QR Code contains Event ID:\n" + eventId);

            Toast.makeText(
                    this,
                    "Event QR Code generated",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "QR generation error: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
