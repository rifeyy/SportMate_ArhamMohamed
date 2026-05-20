package com.example.sportmate_arhammohamed.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.sportmate_arhammohamed.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class NearbyFieldsActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private LinearLayout fieldsContainer;
    private TextView txtCurrentLocation;
    private Button btnGetLocation, btnOpenMap;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_fields);

        fieldsContainer = findViewById(R.id.fieldsContainer);
        txtCurrentLocation = findViewById(R.id.txtCurrentLocation);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnOpenMap = findViewById(R.id.btnOpenMap);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnGetLocation.setOnClickListener(v -> checkLocationPermission());

        btnOpenMap.setOnClickListener(v -> {
            Intent intent = new Intent(NearbyFieldsActivity.this, MapActivity.class);
            startActivity(intent);
        });

        addField("Terrain Maarif", "Football", "Maarif, Casablanca", "Available today");
        addField("Salle Oasis", "Basketball", "Oasis, Casablanca", "Open until 22:00");
        addField("Parc Anfa", "Running", "Anfa, Casablanca", "Open public space");
        addField("Club Anfa", "Tennis", "Anfa, Casablanca", "Reservation required");
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );

        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        String locationText = "Your current location:"
                                + "\nLatitude: " + latitude
                                + "\nLongitude: " + longitude;

                        txtCurrentLocation.setText(locationText);

                        Toast.makeText(
                                NearbyFieldsActivity.this,
                                "Location detected successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {
                        txtCurrentLocation.setText(
                                "Location not available. Please enable GPS and try again."
                        );

                        Toast.makeText(
                                NearbyFieldsActivity.this,
                                "Location not available. Enable GPS.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            NearbyFieldsActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void addField(String name, String type, String address, String status) {
        TextView textView = new TextView(this);

        String text = name
                + "\nType: " + type
                + "\nAddress: " + address
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

        fieldsContainer.addView(textView);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getCurrentLocation();

            } else {
                Toast.makeText(
                        this,
                        "Location permission denied",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}