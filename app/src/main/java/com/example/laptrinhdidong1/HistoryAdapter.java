package com.example.laptrinhdidong1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<HistoryItem> historyList;

    public HistoryAdapter(Context context, List<HistoryItem> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);

        holder.tvTimestamp.setText(item.getTimestamp());
        holder.tvTemp.setText(String.format("%.1f °C", item.getTemperature()));
        holder.tvHumid.setText(String.format("%.1f %%", item.getHumidity()));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp, tvTemp, tvHumid;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvTemp = itemView.findViewById(R.id.tv_temp_value);
            tvHumid = itemView.findViewById(R.id.tv_humid_value);
        }
    }
}