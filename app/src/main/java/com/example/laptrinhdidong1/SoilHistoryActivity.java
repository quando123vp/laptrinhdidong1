package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.*;

public class SoilHistoryActivity extends AppCompatActivity {
    private LinearLayout llContainer;
    private ImageView btnBack;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_history);

        llContainer = findViewById(R.id.ll_soil_history);
        btnBack = findViewById(R.id.btnBackSoil);
        db = FirebaseDatabase.getInstance().getReference();

        btnBack.setOnClickListener(v -> onBackPressed());
        loadHistory();
    }

    private void loadHistory() {
        db.child("LichSuCamBien").child("DoAmDat")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        llContainer.removeAllViews();
                        if (!snapshot.exists()) {
                            addText("⚠️ Chưa có dữ liệu độ ẩm đất");
                            return;
                        }
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String time = child.child("ThoiGian").getValue(String.class);
                            String value = String.valueOf(child.child("GiaTri").getValue());
                            addText("⏱ " + time + " → " + value + "%");
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SoilHistoryActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(16);
        t.setPadding(12, 8, 12, 8);
        llContainer.addView(t);
    }
}
