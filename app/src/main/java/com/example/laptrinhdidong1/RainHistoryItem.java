package com.example.laptrinhdidong1;

public class RainHistoryItem {

    private String time;        // "2025-11-13 14:25:33"
    private String trangThai;   // "Có Mưa" hoặc "Không Mưa"

    public RainHistoryItem() { }

    public RainHistoryItem(String time, String trangThai) {
        this.time = time;
        this.trangThai = trangThai;
    }

    public String getTime() {
        return time;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
