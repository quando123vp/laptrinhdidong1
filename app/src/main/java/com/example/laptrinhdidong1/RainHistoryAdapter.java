package com.example.laptrinhdidong1;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RainHistoryAdapter extends RecyclerView.Adapter<RainHistoryAdapter.RainViewHolder> {

    private Context context;
    private ArrayList<RainHistoryItem> list;

    public RainHistoryAdapter(Context context, ArrayList<RainHistoryItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_rain_history, parent, false);
        return new RainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RainViewHolder h, int position) {
        RainHistoryItem item = list.get(position);

        h.tvTime.setText(item.getTime());
        h.tvStatus.setText(item.getTrangThai());

        String st = item.getTrangThai().toLowerCase();

        // ---- chọn icon + màu nền ----
        if (st.contains("không")) {
            h.ivIcon.setImageResource(R.drawable.ic_sunny);
            h.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.iconOrange));
            h.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.noRainGreenBackground));
        } else {
            h.ivIcon.setImageResource(R.drawable.ic_rain);
            h.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.primaryBlue));
            h.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.rainBlueBackground));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class RainViewHolder extends RecyclerView.ViewHolder {

        CardView card;
        ImageView ivIcon;
        TextView tvTime, tvStatus;

        public RainViewHolder(@NonNull View v) {
            super(v);
            card = v.findViewById(R.id.cardRain);
            ivIcon = v.findViewById(R.id.ivRainIcon);
            tvTime = v.findViewById(R.id.tvRainTime);
            tvStatus = v.findViewById(R.id.tvRainStatus);
        }
    }
}
