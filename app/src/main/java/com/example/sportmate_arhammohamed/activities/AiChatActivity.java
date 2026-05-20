package com.example.sportmate_arhammohamed.activities;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportmate_arhammohamed.R;
import com.example.sportmate_arhammohamed.network.ApiClient;
import com.example.sportmate_arhammohamed.network.ApiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiChatActivity extends AppCompatActivity {

    private LinearLayout chatContainer;
    private ScrollView chatScrollView;
    private EditText edtChatMessage;

    private Button btnSendMessage;
    private Button btnOpenConversations;

    private TextView txtChatTitle;
    private TextView typingView;

    private ApiService apiService;

    private String currentConversationId = "";

    private final ArrayList<String> conversationIds = new ArrayList<>();
    private final ArrayList<String> conversationTitles = new ArrayList<>();

    private boolean isSending = false;

    private static final String DEFAULT_HEADER_TITLE = "SportMate AI Chat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        chatContainer = findViewById(R.id.chatContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        edtChatMessage = findViewById(R.id.edtChatMessage);

        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnOpenConversations = findViewById(R.id.btnOpenConversations);

        txtChatTitle = findViewById(R.id.txtChatTitle);
        txtChatTitle.setText(DEFAULT_HEADER_TITLE);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnSendMessage.setOnClickListener(v -> sendMessageToAI());
        btnOpenConversations.setOnClickListener(v -> showMenuDialog());

        loadConversationsAtStart();
    }

    private void loadConversationsAtStart() {
        chatContainer.removeAllViews();
        addSystemMessage("Loading conversations...");

        apiService.getConversations().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(
                    Call<List<Map<String, Object>>> call,
                    Response<List<Map<String, Object>>> response
            ) {
                chatContainer.removeAllViews();

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    updateInternalLists(response.body());

                    currentConversationId = conversationIds.get(0);

                    // Header يبقى professional، ماشي title ديال conversation بحال slm
                    txtChatTitle.setText(DEFAULT_HEADER_TITLE);

                    loadChatHistory(currentConversationId);
                } else {
                    createNewConversation(null);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                chatContainer.removeAllViews();
                addAiBubble("Connection error while loading conversations. Check backend connection.");
            }
        });
    }

    private void updateInternalLists(List<Map<String, Object>> conversations) {
        conversationIds.clear();
        conversationTitles.clear();

        for (Map<String, Object> conversation : conversations) {
            String id = getId(conversation);
            String title = getValue(conversation, "title");

            if (title.isEmpty()) {
                title = DEFAULT_HEADER_TITLE;
            }

            conversationIds.add(id);
            conversationTitles.add(title);
        }
    }

    private void showMenuDialog() {
        apiService.getConversations().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(
                    Call<List<Map<String, Object>>> call,
                    Response<List<Map<String, Object>>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    updateInternalLists(response.body());

                    ArrayList<String> menuOptions = new ArrayList<>();
                    menuOptions.add("+ Start New Chat");
                    menuOptions.addAll(conversationTitles);
                    menuOptions.add("Delete Current Chat");

                    String[] optionsArray = menuOptions.toArray(new String[0]);

                    new AlertDialog.Builder(AiChatActivity.this)
                            .setTitle("Chat Menu")
                            .setItems(optionsArray, (dialog, which) -> {
                                if (which == 0) {
                                    createNewConversation(null);
                                } else if (which == optionsArray.length - 1) {
                                    confirmDeleteCurrentChat();
                                } else {
                                    int conversationIndex = which - 1;

                                    if (conversationIndex >= 0 && conversationIndex < conversationIds.size()) {
                                        currentConversationId = conversationIds.get(conversationIndex);

                                        // Header يبقى ثابت professional
                                        txtChatTitle.setText(DEFAULT_HEADER_TITLE);

                                        loadChatHistory(currentConversationId);
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();

                } else {
                    Toast.makeText(AiChatActivity.this, "Could not load conversations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(AiChatActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewConversation(String firstMessage) {
        Map<String, String> body = new HashMap<>();
        body.put("title", firstMessage == null ? DEFAULT_HEADER_TITLE : createTitleFromMessage(firstMessage));

        apiService.createConversation(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(
                    Call<Map<String, Object>> call,
                    Response<Map<String, Object>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    currentConversationId = getId(response.body());

                    txtChatTitle.setText(DEFAULT_HEADER_TITLE);
                    chatContainer.removeAllViews();

                    if (firstMessage == null) {
                        addAiBubble("Hello! I am SportMate AI Assistant. Ask me anything about SportMate.");
                        scrollToBottom();
                    } else {
                        performSendMessage(firstMessage);
                    }

                } else {
                    Toast.makeText(AiChatActivity.this, "Failed to create chat", Toast.LENGTH_SHORT).show();
                    isSending = false;
                    btnSendMessage.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AiChatActivity.this, "Connection error while creating chat", Toast.LENGTH_SHORT).show();
                isSending = false;
                btnSendMessage.setEnabled(true);
            }
        });
    }

    private void loadChatHistory(String conversationId) {
        chatContainer.removeAllViews();
        addSystemMessage("Loading chat...");

        apiService.getChatHistory(conversationId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(
                    Call<List<Map<String, Object>>> call,
                    Response<List<Map<String, Object>>> response
            ) {
                chatContainer.removeAllViews();

                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> history = response.body();

                    if (history.isEmpty()) {
                        addAiBubble("Hello! I am SportMate AI Assistant. Ask me anything about SportMate.");
                    } else {
                        for (Map<String, Object> msg : history) {
                            String sender = getValue(msg, "sender");
                            String text = getValue(msg, "message");

                            if ("user".equalsIgnoreCase(sender)) {
                                addUserBubble(text);
                            } else {
                                addAiBubble(text);
                            }
                        }
                    }

                    scrollToBottom();
                } else {
                    addAiBubble("Could not load this conversation.");
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                chatContainer.removeAllViews();
                addAiBubble("Connection error while loading conversation.");
            }
        });
    }

    private void sendMessageToAI() {
        String message = edtChatMessage.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            edtChatMessage.setError("Type a message");
            return;
        }

        if (isSending) {
            return;
        }

        isSending = true;
        btnSendMessage.setEnabled(false);

        edtChatMessage.setText("");

        if (TextUtils.isEmpty(currentConversationId)) {
            createNewConversation(message);
        } else {
            performSendMessage(message);
        }
    }

    private void performSendMessage(String message) {
        addUserBubble(message);
        showTypingIndicator();
        scrollToBottom();

        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        body.put("conversationId", currentConversationId);

        apiService.sendChatMessage(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(
                    Call<Map<String, Object>> call,
                    Response<Map<String, Object>> response
            ) {
                removeTypingIndicator();

                isSending = false;
                btnSendMessage.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    String reply = extractAiMessage(response.body());

                    if (reply == null || reply.trim().isEmpty()) {
                        reply = localFallback(message);
                    }

                    addAiBubble(reply);
                } else {
                    addAiBubble(localFallback(message));
                }

                txtChatTitle.setText(DEFAULT_HEADER_TITLE);
                scrollToBottom();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                removeTypingIndicator();

                isSending = false;
                btnSendMessage.setEnabled(true);

                addAiBubble(localFallback(message));
                txtChatTitle.setText(DEFAULT_HEADER_TITLE);
                scrollToBottom();
            }
        });
    }

    private void confirmDeleteCurrentChat() {
        if (TextUtils.isEmpty(currentConversationId)) {
            Toast.makeText(this, "No current chat to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Conversation")
                .setMessage("Delete this conversation permanently?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCurrentChat())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCurrentChat() {
        apiService.deleteConversation(currentConversationId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(
                    Call<Map<String, Object>> call,
                    Response<Map<String, Object>> response
            ) {
                if (response.isSuccessful()) {
                    Toast.makeText(AiChatActivity.this, "Conversation deleted", Toast.LENGTH_SHORT).show();

                    currentConversationId = "";
                    txtChatTitle.setText(DEFAULT_HEADER_TITLE);
                    chatContainer.removeAllViews();

                    loadConversationsAtStart();
                } else {
                    Toast.makeText(AiChatActivity.this, "Could not delete conversation", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AiChatActivity.this, "Connection error while deleting", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addUserBubble(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(14);
        tv.setPadding(dp(16), dp(12), dp(16), dp(12));
        tv.setBackground(createBubble(0xFF0B6623, dp(18)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.gravity = Gravity.END;
        params.setMargins(dp(70), dp(8), dp(4), dp(8));
        tv.setLayoutParams(params);

        chatContainer.addView(tv);
        scrollToBottom();
    }

    private void addAiBubble(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(14);
        tv.setPadding(dp(16), dp(12), dp(16), dp(12));
        tv.setBackground(createBubble(0xFFE0E0E0, dp(18)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.gravity = Gravity.START;
        params.setMargins(dp(4), dp(8), dp(70), dp(8));
        tv.setLayoutParams(params);

        chatContainer.addView(tv);
        scrollToBottom();
    }

    private void addSystemMessage(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextColor(0xFF777777);
        tv.setTextSize(12);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(null, Typeface.ITALIC);
        tv.setPadding(dp(10), dp(10), dp(10), dp(10));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, dp(8), 0, dp(8));
        tv.setLayoutParams(params);

        chatContainer.addView(tv);
    }

    private void showTypingIndicator() {
        removeTypingIndicator();

        typingView = new TextView(this);
        typingView.setText("SportMate AI is typing...");
        typingView.setTextSize(13);
        typingView.setTextColor(0xFF777777);
        typingView.setTypeface(null, Typeface.ITALIC);
        typingView.setPadding(dp(16), dp(10), dp(16), dp(10));
        typingView.setBackground(createBubble(0xFFEFEFEF, dp(18)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.gravity = Gravity.START;
        params.setMargins(dp(4), dp(8), dp(70), dp(8));
        typingView.setLayoutParams(params);

        chatContainer.addView(typingView);
        scrollToBottom();
    }

    private void removeTypingIndicator() {
        if (typingView != null) {
            chatContainer.removeView(typingView);
            typingView = null;
        }
    }

    private GradientDrawable createBubble(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    @SuppressWarnings("unchecked")
    private String extractAiMessage(Map<String, Object> body) {
        if (body == null) {
            return "";
        }

        Object aiResponseObject = body.get("aiResponse");

        if (aiResponseObject instanceof Map) {
            Map<String, Object> aiMap = (Map<String, Object>) aiResponseObject;

            Object messageObject = aiMap.get("message");
            if (messageObject != null && !String.valueOf(messageObject).trim().isEmpty()) {
                return String.valueOf(messageObject);
            }
        }

        Object replyObject = body.get("reply");
        if (replyObject != null && !String.valueOf(replyObject).trim().isEmpty()) {
            return String.valueOf(replyObject);
        }

        Object messageObject = body.get("message");
        if (messageObject != null && !String.valueOf(messageObject).trim().isEmpty()) {
            return String.valueOf(messageObject);
        }

        Object responseObject = body.get("response");
        if (responseObject != null && !String.valueOf(responseObject).trim().isEmpty()) {
            return String.valueOf(responseObject);
        }

        return "";
    }

    private String getValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String getId(Map<String, Object> map) {
        Object value = map.get("_id");
        return value == null ? "" : String.valueOf(value);
    }

    private String createTitleFromMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return DEFAULT_HEADER_TITLE;
        }

        String msg = message.trim().toLowerCase();

        if (
                msg.equals("hi") ||
                        msg.equals("hello") ||
                        msg.equals("slm") ||
                        msg.equals("salam") ||
                        msg.equals("bonjour") ||
                        msg.equals("salut")
        ) {
            return DEFAULT_HEADER_TITLE;
        }

        if (msg.contains("join") || msg.contains("rejoindre") || msg.contains("joini")) {
            return "Joining Events";
        }

        if (msg.contains("qr") || msg.contains("scan")) {
            return "QR Scanner Help";
        }

        if (msg.contains("gps") || msg.contains("location")) {
            return "GPS Location Help";
        }

        if (msg.contains("map") || msg.contains("maps")) {
            return "Google Maps Help";
        }

        if (msg.contains("backend") || msg.contains("mongodb")) {
            return "Backend Support";
        }

        String title = message.trim();

        if (title.length() > 28) {
            title = title.substring(0, 28) + "...";
        }

        return title;
    }

    private void scrollToBottom() {
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String localFallback(String message) {
        String msg = message.toLowerCase();

        if (msg.contains("event") || msg.contains("join")) {
            return "To join an event, open Find Match, select an event, then click Join Event.";
        }

        if (msg.contains("qr") || msg.contains("scan")) {
            return "Use QR Scanner to scan an event QR code and confirm attendance.";
        }

        if (msg.contains("gps") || msg.contains("location")) {
            return "GPS Location is available in Nearby Fields.";
        }

        if (msg.contains("map") || msg.contains("maps")) {
            return "Google Maps is available in Nearby Fields.";
        }

        if (msg.contains("backend") || msg.contains("mongodb")) {
            return "SportMate uses Node.js, Express, MongoDB, and Retrofit.";
        }

        return "I am SportMate AI Assistant. I can help you with SportMate features.";
    }
}