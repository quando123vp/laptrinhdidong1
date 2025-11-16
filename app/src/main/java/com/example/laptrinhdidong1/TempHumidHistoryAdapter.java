package com.example.laptrinhdidong1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TempHumidHistoryAdapter extends RecyclerView.Adapter<TempHumidHistoryAdapter.Holder> {

    private ArrayList<TempHumidHistoryItem> list;

    public TempHumidHistoryAdapter(ArrayList<TempHumidHistoryItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sensor_temp_humid, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        TempHumidHistoryItem it = list.get(i);

        h.tvTime.setText(it.time);
        h.tvTemp.setText(String.format("%.1f°C", it.temp));
        h.tvHumid.setText(String.format("%.1f%%", it.humid));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        TextView tvTime, tvTemp, tvHumid;

        Holder(@NonNull View v) {
            super(v);
            tvTime = v.findViewById(R.id.tv_temp_time);
            tvTemp = v.findViewById(R.id.tv_temp);
            tvHumid = v.findViewById(R.id.tv_humid);
        }
    }
}
