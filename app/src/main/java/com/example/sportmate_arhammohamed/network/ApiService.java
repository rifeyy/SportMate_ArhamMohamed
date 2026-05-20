package com.example.sportmate_arhammohamed.network;

import com.example.sportmate_arhammohamed.models.Event;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // =========================
    // AUTH - REGISTER / LOGIN
    // =========================

    @POST("api/auth/register")
    Call<Map<String, Object>> registerUser(@Body Map<String, String> body);

    @POST("api/auth/login")
    Call<Map<String, Object>> loginUser(@Body Map<String, String> body);

    // =========================
    // EVENTS
    // =========================

    @GET("api/events")
    Call<List<Event>> getEvents();

    @POST("api/events")
    Call<Event> createEvent(@Body Event event);

    @POST("api/events/{id}/join")
    Call<Map<String, Object>> joinEvent(@Path("id") String eventId);

    // =========================
    // ACTIVITIES
    // =========================

    @GET("api/activities")
    Call<List<Map<String, Object>>> getActivities();

    // =========================
    // AI RECOMMENDATIONS
    // =========================

    @GET("api/recommendations")
    Call<List<Map<String, Object>>> getRecommendations(
            @Query("sport") String sport,
            @Query("level") String level,
            @Query("location") String location
    );

    // =========================
    // AI CHAT CONVERSATIONS
    // =========================

    @GET("api/conversations")
    Call<List<Map<String, Object>>> getConversations();

    @POST("api/conversations")
    Call<Map<String, Object>> createConversation(@Body Map<String, String> body);

    @DELETE("api/conversations/{id}")
    Call<Map<String, Object>> deleteConversation(@Path("id") String id);

    // =========================
    // AI CHAT
    // =========================

    @POST("api/chat")
    Call<Map<String, Object>> sendChatMessage(@Body Map<String, String> body);

    @GET("api/chat/history/{conversationId}")
    Call<List<Map<String, Object>>> getChatHistory(
            @Path("conversationId") String conversationId
    );

    @DELETE("api/chat/history/{conversationId}")
    Call<Map<String, Object>> clearChatHistory(
            @Path("conversationId") String conversationId
    );
}