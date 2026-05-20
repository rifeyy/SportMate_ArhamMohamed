package com.example.sportmate_arhammohamed.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.sportmate_arhammohamed.R;
import com.example.sportmate_arhammohamed.network.ApiClient;
import com.example.sportmate_arhammohamed.network.ApiService;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrScannerActivity extends AppCompatActivity {

    private TextView txtResult;
    private Button btnScan;
    private DecoratedBarcodeView barcodeScannerView;

    private ApiService apiService;

    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        txtResult = findViewById(R.id.txtResult);
        btnScan = findViewById(R.id.btnScan);
        barcodeScannerView = findViewById(R.id.barcodeScannerView);

        apiService = ApiClient.getClient().create(ApiService.class);

        barcodeScannerView.setStatusText("Scan SportMate Event QR Code");

        btnScan.setOnClickListener(v -> checkCameraPermission());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            startScanner();

        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startScanner();
                        } else {
                            Toast.makeText(
                                    QrScannerActivity.this,
                                    "Camera permission denied",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    private void startScanner() {
        if (isScanning) {
            return;
        }

        isScanning = true;
        btnScan.setText("Scanning...");
        txtResult.setText("Place the event QR code inside the square");

        barcodeScannerView.resume();

        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result == null || result.getText() == null) {
                    return;
                }

                String scannedEventId = result.getText().trim();

                isScanning = false;
                barcodeScannerView.pause();
                btnScan.setText("Start QR Scan");

                txtResult.setText(
                        "QR Code Result:\n" + scannedEventId +
                                "\n\nSending attendance to backend..."
                );

                joinEventFromQr(scannedEventId);
            }
        });
    }

    private void joinEventFromQr(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.joinEvent(eventId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    txtResult.setText(
                            "QR Code Result:\n" + eventId +
                                    "\n\nAttendance confirmed and saved to backend ✅"
                    );

                    Toast.makeText(
                            QrScannerActivity.this,
                            "Attendance confirmed successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                } else {
                    txtResult.setText(
                            "QR Code Result:\n" + eventId +
                                    "\n\nServer error. Event may be full or invalid."
                    );

                    Toast.makeText(
                            QrScannerActivity.this,
                            "Server error while confirming attendance",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                txtResult.setText(
                        "QR Code Result:\n" + eventId +
                                "\n\nConnection error: " + t.getMessage()
                );

                Toast.makeText(
                        QrScannerActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isScanning) {
            barcodeScannerView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        barcodeScannerView.pause();
    }
}