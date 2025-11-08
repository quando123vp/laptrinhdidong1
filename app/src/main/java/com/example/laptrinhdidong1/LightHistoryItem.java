package com.example.laptrinhdidong1;

public class LightHistoryItem {
    private String timestamp;
    private String status;
    private Integer percentage; // Dùng Integer để có thể là null

    public LightHistoryItem(String timestamp, String status, Integer percentage) {
        this.timestamp = timestamp;
        this.status = status;
        this.percentage = percentage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public Integer getPercentage() {
        return percentage;
    }
}