package com.mrdeveloper.mytourplan.models;

public class ItineraryItem {
    private int id;
    private String tripId;
    private int day;
    private String time;
    private String activity;
    private String location;

    public ItineraryItem() {}

    public ItineraryItem(int id, String tripId, int day, String time, String activity, String location) {
        this.id = id;
        this.tripId = tripId;
        this.day = day;
        this.time = time;
        this.activity = activity;
        this.location = location;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
