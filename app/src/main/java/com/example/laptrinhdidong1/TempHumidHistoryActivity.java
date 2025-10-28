package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.*;

public class TempHumidHistoryActivity extends AppCompatActivity {

    private LinearLayout llContainer;
    private ImageView btnBack;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_humid_history);

        llContainer = findViewById(R.id.ll_temp_humid_history);
        btnBack = findViewById(R.id.btnBackTempHumid);
        db = FirebaseDatabase.getInstance().getReference();

        btnBack.setOnClickListener(v -> onBackPressed());
        loadHistory();
    }

    private void loadHistory() {
        db.child("LichSuCamBien").child("NhietDo_DoAm")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        llContainer.removeAllViews();
                        if (!snapshot.exists()) {
                            addText("⚠️ Chưa có dữ liệu nhiệt độ / độ ẩm");
                            return;
                        }

                        for (DataSnapshot child : snapshot.getChildren()) {
                            String time = child.child("ThoiGian").getValue(String.class);
                            String nhietDo = String.valueOf(child.child("NhietDo").getValue());
                            String doAm = String.valueOf(child.child("DoAm").getValue());
                            addText("⏱ " + time + " → " + nhietDo + "°C | " + doAm + "%");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TempHumidHistoryActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
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
