package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RainHistoryActivity extends AppCompatActivity {

    private LinearLayout llContainer;
    private ImageView btnBack;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rain_history);

        // 🌧️ Ánh xạ giao diện
        llContainer = findViewById(R.id.ll_rain_history);
        btnBack = findViewById(R.id.btnBackRain);

        // 🔥 Kết nối tới node gốc "LichSu"
        db = FirebaseDatabase.getInstance().getReference("LichSu");

        // 🔙 Nút quay lại
        btnBack.setOnClickListener(v -> onBackPressed());

        // 📜 Tải dữ liệu lịch sử
        loadHistory();
    }

    private void loadHistory() {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                llContainer.removeAllViews();

                if (!snapshot.exists()) {
                    addText("⚠️ Chưa có dữ liệu lịch sử thời tiết");
                    return;
                }

                // Duyệt qua từng bản ghi thời gian trong "LichSu"
                for (DataSnapshot timeSnap : snapshot.getChildren()) {
                    String time = timeSnap.getKey();
                    String trangThai = timeSnap.child("TrangThaiMua").getValue(String.class);

                    if (trangThai != null) {
                        addText("⏱ " + time + " → " + trangThai);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RainHistoryActivity.this, "❌ Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addText(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(16);
        t.setPadding(16, 10, 16, 10);
        llContainer.addView(t);
    }
}
