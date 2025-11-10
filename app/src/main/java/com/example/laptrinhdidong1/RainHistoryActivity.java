package com.example.laptrinhdidong1;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.view.View;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * RainHistoryActivity - hiển thị lịch sử mưa từ node "LichSu"
 * Layout: res/layout/activity_rain_history.xml (ScrollView + LinearLayout id=ll_rain_history)
 * Back button id: btnBackRain
 */
public class RainHistoryActivity extends AppCompatActivity {

    private LinearLayout llContainer;
    private ImageView btnBack;
    private DatabaseReference dbRef;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private static class RainItem {
        String timeKey;
        long timestamp;
        String trangThai;

        RainItem(String timeKey, long timestamp, String trangThai) {
            this.timeKey = timeKey;
            this.timestamp = timestamp;
            this.trangThai = trangThai;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rain_history);

        llContainer = findViewById(R.id.ll_rain_history);
        btnBack = findViewById(R.id.btnBackRain);

        if (llContainer == null) {
            Toast.makeText(this, "Layout thiếu ll_rain_history", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Back gesture support
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { finish(); }
        });

        dbRef = FirebaseDatabase.getInstance().getReference("LichSu");
        loadRainHistory();
    }

    private void loadRainHistory() {
        llContainer.removeAllViews();
        addCenteredText("Đang tải dữ liệu...", 16);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                llContainer.removeAllViews();

                if (!snapshot.exists()) {
                    addCenteredText("⚠️ Chưa có dữ liệu lịch sử thời tiết", 16);
                    return;
                }

                ArrayList<RainItem> list = new ArrayList<>();

                for (DataSnapshot timeSnap : snapshot.getChildren()) {
                    String timeKey = timeSnap.getKey();
                    long timestamp = 0;
                    if (timeSnap.child("timestamp").exists()) {
                        Object tsObj = timeSnap.child("timestamp").getValue();
                        if (tsObj instanceof Long) timestamp = (Long) tsObj;
                        else if (tsObj instanceof Integer) timestamp = ((Integer) tsObj).longValue();
                        else if (tsObj instanceof String) {
                            try { timestamp = Long.parseLong((String) tsObj); } catch (Exception ignored) {}
                        }
                    }

                    // Lấy trạng thái mưa (ưu tiên Mua/TrangThai)
                    String trangThai = null;
                    DataSnapshot mua = timeSnap.child("Mua");
                    if (mua.exists()) {
                        trangThai = mua.child("TrangThai").getValue(String.class);
                    }
                    // fallback: root key "TrangThaiMua"
                    if ((trangThai == null || trangThai.isEmpty())) {
                        String rootStatus = timeSnap.child("TrangThaiMua").getValue(String.class);
                        if (rootStatus != null && !rootStatus.isEmpty()) trangThai = rootStatus;
                    }

                    if (trangThai != null && !trangThai.isEmpty()) {
                        list.add(new RainItem(timeKey, timestamp, trangThai));
                    }
                }

                // sort newest first
                Collections.sort(list, new Comparator<RainItem>() {
                    @Override
                    public int compare(RainItem a, RainItem b) {
                        if (a.timestamp > 0 && b.timestamp > 0)
                            return Long.compare(b.timestamp, a.timestamp);
                        if (a.timestamp > 0 && b.timestamp == 0) return -1;
                        if (b.timestamp > 0 && a.timestamp == 0) return 1;
                        if (a.timeKey != null && b.timeKey != null) return b.timeKey.compareTo(a.timeKey);
                        return 0;
                    }
                });

                if (list.isEmpty()) {
                    addCenteredText("⚠️ Không tìm thấy mục Mua trong lịch sử", 16);
                    return;
                }

                // render sorted list
                for (RainItem it : list) addRainCard(it.timeKey, it.timestamp, it.trangThai);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                llContainer.removeAllViews();
                addCenteredText("❌ Lỗi tải dữ liệu", 16);
                Toast.makeText(RainHistoryActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Thêm 1 CardView cho mỗi mục mưa: icon + time + trạng thái.
     * Không hiển thị Analog.
     */
    private void addRainCard(String timeKey, long timestamp, String trangThai) {
        // CardView container
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.setMargins(dp(8), dp(8), dp(8), dp(8));
        card.setLayoutParams(cardLp);
        card.setRadius(dp(12));
        card.setCardElevation(dp(3));

        // normalize status string
        String stRaw = (trangThai == null ? "" : trangThai.trim());
        String st = stRaw.toLowerCase(Locale.ROOT);

        // chọn nền theo trạng thái chính xác
        // => kiểm tra "không/khong" TRƯỚC (để không bị trùng với "mưa")
        if (st.contains("không") || st.contains("khong")) {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.noRainGreenBackground));
        } else if (st.contains("có mưa") || st.contains("co mua") || (st.contains("mưa") && !st.contains("không") && !st.contains("khong"))) {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.rainBlueBackground));
        } else {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.lightGrayBackground));
        }

        // row: icon + texts
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(12), dp(12), dp(12), dp(12));
        row.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(row);

        // icon
        ImageView iv = new ImageView(this);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(dp(40), dp(40));
        ivLp.setMargins(0, 0, dp(12), 0);
        iv.setLayoutParams(ivLp);

        // chọn icon và tint theo trạng thái (kiểm tra "không" trước)
        if (st.contains("không") || st.contains("khong")) {
            iv.setImageResource(R.drawable.ic_sunny);
            iv.setColorFilter(ContextCompat.getColor(this, R.color.iconOrange));
        } else if (st.contains("có mưa") || st.contains("co mua") || (st.contains("mưa") && !st.contains("không") && !st.contains("khong"))) {
            iv.setImageResource(R.drawable.ic_rain);
            iv.setColorFilter(ContextCompat.getColor(this, R.color.primaryBlue));
        } else {
            iv.setImageResource(R.drawable.ic_cloud);
            iv.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));
        }
        row.addView(iv);

        // texts vertical
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.addView(col);

        // time (bold)
        TextView tvTime = new TextView(this);
        tvTime.setTypeface(Typeface.DEFAULT_BOLD);
        tvTime.setTextSize(14);
        tvTime.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        String timeStr;
        if (timestamp > 0) {
            try { timeStr = sdf.format(new Date(timestamp * 1000L)); }
            catch (Exception e) { timeStr = (timeKey != null ? timeKey.replace('_', ' ') : ""); }
        } else {
            timeStr = (timeKey != null ? timeKey.replace('_', ' ') : "");
        }
        tvTime.setText(timeStr);
        col.addView(tvTime);

        // status line
        TextView tvStatus = new TextView(this);
        tvStatus.setTextSize(15);
        tvStatus.setTextColor(ContextCompat.getColor(this, R.color.primaryBlue));
        tvStatus.setPadding(0, dp(6), 0, 0);
        tvStatus.setText("Trạng thái: " + (trangThai == null ? "--" : trangThai));
        col.addView(tvStatus);

        // add to container
        llContainer.addView(card);
    }

    private void addCenteredText(String text, int sizeSp) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(sizeSp);
        t.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(8), dp(10), dp(8), dp(10));
        t.setLayoutParams(lp);
        t.setGravity(Gravity.CENTER);
        llContainer.addView(t);
    }

    private int dp(int v) {
        float scale = getResources().getDisplayMetrics().density;
        return Math.round(v * scale);
    }
}
