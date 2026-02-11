package com.example.bookme;

public class Appointment {
    private String clientName;
    private String clientPhone;
    private String date;
    private String time;
    private String barberId;
    private String status;
    private String type;
    private boolean isBlocked;
    private String docId;

    public Appointment() {}

    // Getters
    public String getClientName() { return clientName; }
    public String getClientPhone() { return clientPhone; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getBarberId() { return barberId; }
    public String getStatus() { return status; }
    public String getType() { return type; }
    public boolean isBlocked() { return isBlocked; }
    public String getDocId() { return docId; }

    // Setters

    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setBarberId(String barberId) { this.barberId = barberId; }
    public void setStatus(String status) { this.status = status; }
    public void setType(String type) { this.type = type; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }
    public void setDocId(String docId) { this.docId = docId; }
}