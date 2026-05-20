package com.example.sportmate_arhammohamed.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportmate_arhammohamed.R;
import com.example.sportmate_arhammohamed.network.ApiClient;
import com.example.sportmate_arhammohamed.network.ApiService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationsActivity extends AppCompatActivity {

    private LinearLayout conversationsContainer;
    private Button btnNewChat;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        conversationsContainer = findViewById(R.id.conversationsContainer);
        btnNewChat = findViewById(R.id.btnNewChat);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnNewChat.setOnClickListener(v -> createNewConversation());

        loadConversations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
    }

    private void loadConversations() {
        conversationsContainer.removeAllViews();

        apiService.getConversations().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call,
                                   Response<List<Map<String, Object>>> response) {

                conversationsContainer.removeAllViews();

                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> conversations = response.body();

                    if (conversations.isEmpty()) {
                        addEmptyMessage();
                        return;
                    }

                    for (Map<String, Object> conversation : conversations) {
                        String id = getId(conversation);
                        String title = getValue(conversation, "title");

                        if (title.isEmpty()) {
                            title = "New Chat";
                        }

                        addConversationItem(id, title);
                    }

                } else {
                    Toast.makeText(
                            ConversationsActivity.this,
                            "Could not load conversations",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                conversationsContainer.removeAllViews();
                addErrorMessage("Connection error: " + t.getMessage());
            }
        });
    }

    private void createNewConversation() {
        Map<String, String> body = new HashMap<>();
        body.put("title", "New Chat");

        apiService.createConversation(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    String id = getId(response.body());

                    Intent intent = new Intent(
                            ConversationsActivity.this,
                            AiChatActivity.class
                    );
                    intent.putExtra("conversationId", id);
                    intent.putExtra("conversationTitle", "New Chat");
                    startActivity(intent);
                } else {
                    Toast.makeText(
                            ConversationsActivity.this,
                            "Could not create chat",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(
                        ConversationsActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void addConversationItem(String conversationId, String title) {
        TextView textView = new TextView(this);

        textView.setText(title);
        textView.setTextColor(0xFFFFFFFF);
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(dp(18), dp(16), dp(18), dp(16));
        textView.setBackgroundColor(0xFF1F1F1F);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, dp(2));
        textView.setLayoutParams(params);

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ConversationsActivity.this,
                    AiChatActivity.class
            );
            intent.putExtra("conversationId", conversationId);
            intent.putExtra("conversationTitle", title);
            startActivity(intent);
        });

        conversationsContainer.addView(textView);
    }

    private void addEmptyMessage() {
        TextView textView = new TextView(this);
        textView.setText("No conversations yet. Click New Chat.");
        textView.setTextColor(0xFFAAAAAA);
        textView.setTextSize(14);
        textView.setPadding(dp(18), dp(18), dp(18), dp(18));
        conversationsContainer.addView(textView);
    }

    private void addErrorMessage(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(0xFFFF6666);
        textView.setTextSize(14);
        textView.setPadding(dp(18), dp(18), dp(18), dp(18));
        conversationsContainer.addView(textView);
    }

    private String getValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String getId(Map<String, Object> map) {
        Object value = map.get("_id");
        return value == null ? "" : String.valueOf(value);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
