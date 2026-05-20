package com.example.sportmate_arhammohamed.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportmate_arhammohamed.R;
import com.example.sportmate_arhammohamed.network.ApiClient;
import com.example.sportmate_arhammohamed.network.ApiService;
import com.example.sportmate_arhammohamed.utils.FirebaseLogger;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView txtCreateAccount;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtCreateAccount = findViewById(R.id.txtCreateAccount);

        apiService = ApiClient.getClient().create(ApiService.class);

        FirebaseLogger.logScreen(this, "Login Screen");

        FirebaseLogger.logScreen(this, "Login Screen");

        btnLogin.setOnClickListener(v -> loginUser());

        txtCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Enter your email");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Enter your password");
            edtPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        apiService.loginUser(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(
                    Call<Map<String, Object>> call,
                    Response<Map<String, Object>> response
            ) {
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    saveUserSession(response.body());

                    FirebaseLogger.logLoginSuccess(LoginActivity.this);
                    FirebaseLogger.backupAppAction("login", "User logged in");

                    FirebaseLogger.logLoginSuccess(LoginActivity.this);
                    FirebaseLogger.backupAppAction("login", "User logged in");

                    Toast.makeText(
                            LoginActivity.this,
                            "Login successful",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();

                } else if (response.code() == 401) {
                    Toast.makeText(
                            LoginActivity.this,
                            "Incorrect email or password",
                            Toast.LENGTH_LONG
                    ).show();

                } else {
                    Toast.makeText(
                            LoginActivity.this,
                            "Login failed",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnLogin.setEnabled(true);

                Toast.makeText(
                        LoginActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void saveUserSession(Map<String, Object> responseBody) {
        Object userObject = responseBody.get("user");

        String id = "";
        String name = "";
        String email = "";

        if (userObject instanceof Map) {
            Map<String, Object> userMap = (Map<String, Object>) userObject;

            Object idObject = userMap.get("id");
            Object nameObject = userMap.get("name");
            Object emailObject = userMap.get("email");

            if (idObject != null) {
                id = String.valueOf(idObject);
            }

            if (nameObject != null) {
                name = String.valueOf(nameObject);
            }

            if (emailObject != null) {
                email = String.valueOf(emailObject);
            }
        }

        SharedPreferences preferences = getSharedPreferences("SportMateSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("isLoggedIn", true);
        editor.putString("userId", id);
        editor.putString("name", name);
        editor.putString("email", email);
        editor.apply();
    }
}

