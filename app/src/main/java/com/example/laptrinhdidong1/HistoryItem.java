package com.example.laptrinhdidong1;

public class HistoryItem {
    private String timestamp;
    private float temperature;
    private float humidity;

    // Cần một constructor rỗng cho Firebase (nếu cần)
    public HistoryItem() {
    }

    public HistoryItem(String timestamp, float temperature, float humidity) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getHumidity() {
        return humidity;
    }
}