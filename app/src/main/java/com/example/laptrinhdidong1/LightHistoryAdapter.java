package com.example.laptrinhdidong1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LightHistoryAdapter extends RecyclerView.Adapter<LightHistoryAdapter.Holder> {

    private ArrayList<LightHistoryItem> list;

    public LightHistoryAdapter(ArrayList<LightHistoryItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_light_history, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        LightHistoryItem it = list.get(i);

        h.tvTime.setText(it.time);
        h.tvStatus.setText("Trạng thái: " + it.status);

        if (it.status.equalsIgnoreCase("Sáng")) {
            h.icon.setImageResource(R.drawable.ic_sun);
        } else {
            h.icon.setImageResource(R.drawable.ic_moon);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView tvTime, tvStatus;
        ImageView icon;

        Holder(@NonNull View v) {
            super(v);
            icon = v.findViewById(R.id.icon_light);
            tvTime = v.findViewById(R.id.tv_light_time);
            tvStatus = v.findViewById(R.id.tv_light_status);
        }
    }
}
