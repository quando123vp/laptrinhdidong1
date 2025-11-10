package com.example.laptrinhdidong1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RainHistoryAdapter extends RecyclerView.Adapter<RainHistoryAdapter.VH> {

    private final List<RainHistoryItem> items;
    private final LayoutInflater inflater;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final Context ctx;

    /**
     * @param context context
     * @param items list item RainHistoryItem
     */
    public RainHistoryAdapter(Context context, List<RainHistoryItem> items) {
        this.items = items;
        this.inflater = LayoutInflater.from(context);
        this.ctx = context;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // NOTE: Adapter expects the layout you provided to be saved as res/layout/list_item_history.xml
        // If you named it differently (e.g. list_item_rain.xml) change the resource below accordingly.
        View v = inflater.inflate(R.layout.list_item_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        RainHistoryItem it = items.get(position);

        // Time: prefer timestamp (epoch seconds) -> human readable, else use timeKey
        if (it.timestamp > 0) {
            holder.tvTimestamp.setText(sdf.format(new Date(it.timestamp * 1000L)));
        } else if (it.timeKey != null) {
            holder.tvTimestamp.setText(it.timeKey.replace('_', ' '));
        } else {
            holder.tvTimestamp.setText("");
        }

        // Show rain status in tv_temp_value (repurposed field)
        String status = it.trangThai != null ? it.trangThai : "--";
        holder.tvRainStatus.setText(status);

        // Show analog / digital in tv_humid_value
        String extra = "";
        if (it.analog != null) extra += "Analog: " + it.analog;
        if (it.digital != null) {
            if (!extra.isEmpty()) extra += "  ";
            extra += "Digital: " + it.digital;
        }
        holder.tvAnalogDigital.setText(extra);

        // Icon: choose drawable & tint based on status (replace drawable names with yours if different)
        if (status.toLowerCase().contains("mưa") || status.toLowerCase().contains("có")) {
            // raining
            holder.ivIcon.setImageResource(R.drawable.ic_rain_status); // make sure this drawable exists
            holder.ivIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.primaryBlue));
        } else {
            // not raining
            holder.ivIcon.setImageResource(R.drawable.ic_cloud); // fallback drawable
            holder.ivIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.text_secondary));
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTimestamp;
        TextView tvRainStatus;    // mapped to tv_temp_value in your layout
        TextView tvAnalogDigital; // mapped to tv_humid_value in your layout

        VH(@NonNull View itemView) {
            super(itemView);
            // ids from the layout you gave
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvRainStatus = itemView.findViewById(R.id.tv_temp_value);
            tvAnalogDigital = itemView.findViewById(R.id.tv_humid_value);
        }
    }
}
