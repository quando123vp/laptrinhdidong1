package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.*;

public class RainHistoryActivity extends AppCompatActivity {

    private LinearLayout llContainer;
    private ImageView btnBack;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rain_history);

        llContainer = findViewById(R.id.ll_rain_history);
        btnBack = findViewById(R.id.btnBackRain);
        db = FirebaseDatabase.getInstance().getReference();

        btnBack.setOnClickListener(v -> onBackPressed());
        loadHistory();
    }

    private void loadHistory() {
        db.child("LichSuCamBien").child("TrangThaiMua")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        llContainer.removeAllViews();
                        if (!snapshot.exists()) {
                            addText("⚠️ Chưa có dữ liệu thời tiết");
                            return;
                        }

                        for (DataSnapshot child : snapshot.getChildren()) {
                            String time = child.child("ThoiGian").getValue(String.class);
                            String trangThai = String.valueOf(child.child("TrangThai").getValue());
                            addText("⏱ " + time + " → " + trangThai);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RainHistoryActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
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
