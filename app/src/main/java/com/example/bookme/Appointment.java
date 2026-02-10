package com.example.bookme;

public class Appointment {
    private String clientName;
    private String clientPhone;
    private String date;
    private String time;
    private String barberId;
    private String status;
    private String type;

    public Appointment() {}

    public String getClientName() { return clientName; }
    public String getClientPhone() { return clientPhone; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getBarberId() { return barberId; }
    public String getStatus() { return status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
