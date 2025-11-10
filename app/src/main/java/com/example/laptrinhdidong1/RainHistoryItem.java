package com.example.laptrinhdidong1;

public class RainHistoryItem {
    public String timeKey;     // ví dụ "2025-11-09_12-34-56"
    public long timestamp;     // epoch seconds (0 nếu không có)
    public String trangThai;   // "Có mưa" / "Không mưa"
    public Integer analog;     // optional
    public Integer digital;    // optional

    public RainHistoryItem() {}

    public RainHistoryItem(String timeKey, long timestamp, String trangThai, Integer analog, Integer digital) {
        this.timeKey = timeKey;
        this.timestamp = timestamp;
        this.trangThai = trangThai;
        this.analog = analog;
        this.digital = digital;
    }
}
