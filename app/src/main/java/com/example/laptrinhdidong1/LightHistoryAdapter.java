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

import java.util.List;

// 🔹 LỚP CHA BẮT ĐẦU TẠI ĐÂY
public class LightHistoryAdapter extends RecyclerView.Adapter<LightHistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<LightHistoryItem> historyList;

    public LightHistoryAdapter(Context context, List<LightHistoryItem> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_light_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        LightHistoryItem item = historyList.get(position);

        holder.tvTimestamp.setText(item.getTimestamp());
        Integer percentage = item.getPercentage(); // Lấy %

        // Tạo chuỗi chi tiết
        String details = item.getStatus();
        if (percentage != null) {
            details += " (" + percentage + "%)";
        }
        holder.tvLightDetails.setText(details);

        // --- Logic đổi màu icon ---
        int iconColor;
        if (percentage != null && percentage == 0) {
            // Nếu là 0% (Tối), dùng màu đen (text_primary)
            iconColor = ContextCompat.getColor(context, R.color.text_primary);
        } else {
            // Mặc định hoặc 100% (Sáng), dùng màu vàng (iconYellow)
            iconColor = ContextCompat.getColor(context, R.color.iconYellow);
        }
        holder.ivLightIcon.setColorFilter(iconColor);
        // --- Kết thúc logic đổi màu ---
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    // 🔹 LỚP NỘI BỘ (INNER CLASS) NẰM BÊN TRONG LỚP CHA
    // Lỗi của bạn là do đặt lớp này BÊN NGOÀI
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp, tvLightDetails;
        ImageView ivLightIcon;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvLightDetails = itemView.findViewById(R.id.tv_light_details);
            ivLightIcon = itemView.findViewById(R.id.iv_icon_light);
        }
    }

} // 🔹 DẤU NGOẶC NHỌN ĐÓNG CỦA LỚP CHA 'LightHistoryAdapter'