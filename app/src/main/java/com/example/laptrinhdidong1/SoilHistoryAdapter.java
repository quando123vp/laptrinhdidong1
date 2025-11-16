package com.example.laptrinhdidong1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SoilHistoryAdapter extends RecyclerView.Adapter<SoilHistoryAdapter.Holder> {

    private ArrayList<SoilHistoryItem> list;

    public SoilHistoryAdapter(ArrayList<SoilHistoryItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_soil_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        SoilHistoryItem it = list.get(i);

        h.tvTime.setText(it.time);
        h.tvPercent.setText(it.percent + "%");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView tvTime, tvPercent;

        Holder(@NonNull View v) {
            super(v);
            tvTime = v.findViewById(R.id.tv_soil_time);
            tvPercent = v.findViewById(R.id.tv_soil_percent);
        }
    }
}
