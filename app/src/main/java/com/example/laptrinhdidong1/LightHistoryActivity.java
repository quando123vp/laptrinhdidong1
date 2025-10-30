package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.*;

public class LightHistoryActivity extends AppCompatActivity {

    private LinearLayout llContainer;
    private ImageView btnBack;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_history);

        // 🔗 Ánh xạ view
        llContainer = findViewById(R.id.ll_light_history);
        btnBack = findViewById(R.id.btnBackLight);

        // 🔥 Kết nối đúng node "LichSu"
        db = FirebaseDatabase.getInstance().getReference("LichSu");

        // 🔙 Nút quay lại
        btnBack.setOnClickListener(v -> onBackPressed());

        // 📜 Tải lịch sử
        loadHistory();
    }

    private void loadHistory() {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                llContainer.removeAllViews();

                if (!snapshot.exists()) {
                    addText("⚠️ Chưa có dữ liệu ánh sáng");
                    return;
                }

                // Duyệt qua từng bản ghi thời gian
                for (DataSnapshot timeSnap : snapshot.getChildren()) {
                    String time = timeSnap.getKey();

                    String trangThai = timeSnap.child("AnhSang/TrangThai").getValue(String.class);
                    Integer phanTram = timeSnap.child("AnhSang/PhanTram").getValue(Integer.class);

                    if (trangThai != null) {
                        String text = "⏱ " + time + " → " + trangThai;
                        if (phanTram != null) text += " (" + phanTram + "%)";
                        addText(text);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LightHistoryActivity.this, "❌ Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addText(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(16);
        t.setPadding(12, 8, 12, 8);
        llContainer.addView(t);
    }
}
