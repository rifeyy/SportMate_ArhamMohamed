package com.example.sportmate_arhammohamed.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseLogger {

    private static final String TAG = "FirebaseLogger";
    private static FirebaseFirestore firestore;

    private static FirebaseFirestore getFirestore() {
        if (firestore == null) {
            firestore = FirebaseFirestore.getInstance();
        }
        return firestore;
    }

    public static void logScreen(Context context, String screenName) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);

        Bundle bundle = new Bundle();
        bundle.putString("screen_name", screenName);

        analytics.logEvent("screen_view_custom", bundle);
    }

    public static void logLoginSuccess(Context context) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);

        Bundle bundle = new Bundle();
        bundle.putString("status", "success");

        analytics.logEvent("login_success", bundle);
    }

    public static void logRegisterSuccess(Context context) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);

        Bundle bundle = new Bundle();
        bundle.putString("status", "success");

        analytics.logEvent("register_success", bundle);
    }

    public static void backupUserRegistration(String name, String email) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("source", "SportMate Android");
        data.put("createdAt", System.currentTimeMillis());

        getFirestore()
                .collection("sportmate_users_backup")
                .add(data)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "User backup saved: " + documentReference.getId())
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "User backup failed: " + e.getMessage())
                );
    }

    public static void backupAppAction(String actionName, String details) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", actionName);
        data.put("details", details);
        data.put("createdAt", System.currentTimeMillis());

        getFirestore()
                .collection("sportmate_app_actions")
                .add(data)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Action backup saved: " + documentReference.getId())
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Action backup failed: " + e.getMessage())
                );
    }
}
