package com.example.sportmate_arhammohamed.models;

public class SportEvent {

    private String id;
    private String title;
    private String sport;
    private String location;
    private String date;
    private String time;
    private String level;
    private int maxPlayers;
    private int currentPlayers;

    public SportEvent() {
        // Required empty constructor for Firebase
    }

    public SportEvent(String title, String sport, String location, String date, String time,
                      String level, int maxPlayers, int currentPlayers) {
        this.title = title;
        this.sport = sport;
        this.location = location;
        this.date = date;
        this.time = time;
        this.level = level;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = currentPlayers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getSport() {
        return sport;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getLevel() {
        return level;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }
}