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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RainHistoryActivity extends AppCompatActivity {

    private LinearLayout llContainer;
    private ImageView btnBack;
    private DatabaseReference dbRef;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    static class RainItem {
        String timeStr;
        long timeMillis;
        String trangThai;

        RainItem(String ts, long ms, String tt) {
            this.timeStr = ts;
            this.timeMillis = ms;
            this.trangThai = tt;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rain_history);

        llContainer = findViewById(R.id.ll_rain_history);
        btnBack = findViewById(R.id.btnBackRain);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { finish(); }
        });

        dbRef = FirebaseDatabase.getInstance().getReference("LichSu");
        loadHistory();
    }

    private void loadHistory() {
        llContainer.removeAllViews();
        addCenteredText("Đang tải dữ liệu...");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {

                llContainer.removeAllViews();
                if (!snapshot.exists()) {
                    addCenteredText("⚠️ Chưa có dữ liệu lịch sử!");
                    return;
                }

                ArrayList<RainItem> list = new ArrayList<>();

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String timeStr = snap.getKey();
                    if (timeStr == null) continue;

                    long millis = parseTime(timeStr);

                    // lấy trạng thái từ đúng cấu trúc ESP32 gửi
                    String tt = snap.child("Mua/TrangThai").getValue(String.class);

                    if (tt == null) continue;

                    list.add(new RainItem(timeStr, millis, tt));
                }

                if (list.isEmpty()) {
                    addCenteredText("⚠️ Không có dữ liệu Mưa!");
                    return;
                }

                // sort mới nhất trên đầu
                Collections.sort(list, (a, b) -> Long.compare(b.timeMillis, a.timeMillis));

                for (RainItem it : list) addCard(it);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                llContainer.removeAllViews();
                addCenteredText("❌ Lỗi tải dữ liệu!");
            }
        });
    }

    private long parseTime(String s) {
        try { return sdf.parse(s).getTime(); }
        catch (Exception e) { return 0; }
    }

    private void addCard(RainItem it) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(8), dp(8), dp(8), dp(8));
        card.setLayoutParams(lp);
        card.setRadius(dp(12));
        card.setCardElevation(dp(3));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.addView(box);

        ImageView icon = new ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(40)));
        icon.setPadding(0, 0, dp(12), 0);

        // icon chọn theo trạng thái
        String st = it.trangThai.toLowerCase();

        if (st.contains("không")) {
            icon.setImageResource(R.drawable.ic_sunny);
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.noRainGreenBackground));
        } else {
            icon.setImageResource(R.drawable.ic_rain);
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.rainBlueBackground));
        }

        box.addView(icon);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        box.addView(col);

        TextView tvTime = new TextView(this);
        tvTime.setText(it.timeStr);
        tvTime.setTypeface(Typeface.DEFAULT_BOLD);
        tvTime.setTextSize(15);
        col.addView(tvTime);

        TextView tvStatus = new TextView(this);
        tvStatus.setText("Trạng thái: " + it.trangThai);
        tvStatus.setTextSize(16);
        tvStatus.setPadding(0, dp(4), 0, 0);
        col.addView(tvStatus);

        llContainer.addView(card);
    }

    private void addCenteredText(String msg) {
        TextView t = new TextView(this);
        t.setGravity(Gravity.CENTER);
        t.setText(msg);
        t.setTextSize(16);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(8), dp(8), dp(8), dp(8));
        t.setLayoutParams(lp);
        llContainer.addView(t);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
