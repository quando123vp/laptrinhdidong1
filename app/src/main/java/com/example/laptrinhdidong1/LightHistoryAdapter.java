package com.example.laptrinhdidong1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_light_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        LightHistoryItem it = list.get(i);

        h.tvTime.setText(it.time);
        h.tvStatus.setText(it.status);
        h.tvAnalog.setText("Analog: " + it.analog);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        TextView tvTime, tvStatus, tvAnalog;

        Holder(@NonNull View v) {
            super(v);
            tvTime = v.findViewById(R.id.tv_time);
            tvStatus = v.findViewById(R.id.tv_status);
            tvAnalog = v.findViewById(R.id.tv_analog);
        }
    }
}
