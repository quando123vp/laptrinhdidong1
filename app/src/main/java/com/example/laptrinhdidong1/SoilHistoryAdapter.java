package com.example.laptrinhdidong1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SoilHistoryAdapter extends RecyclerView.Adapter<SoilHistoryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<SoilHistoryItem> list;

    public SoilHistoryAdapter(Context context, ArrayList<SoilHistoryItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_soil_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SoilHistoryItem item = list.get(position);
        holder.tvTime.setText(item.time);
        holder.tvPercent.setText(item.phanTram + "%");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvPercent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvPercent = itemView.findViewById(R.id.tv_percent);
        }
    }
}
