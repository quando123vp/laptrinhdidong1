package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LightHistoryActivity extends AppCompatActivity {

    private RecyclerView rv;
    private LightHistoryAdapter adapter;
    private ArrayList<LightHistoryItem> list = new ArrayList<>();

    private TextView tvNoData;
    private ImageView btnBack;

    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_history);

        rv = findViewById(R.id.rv_light_history);
        tvNoData = findViewById(R.id.tv_no_data);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LightHistoryAdapter(list);
        rv.setAdapter(adapter);

        // --- SỬA LẠI: Trỏ vào node gốc LichSu (Giống Mưa và Đất) ---
        db = FirebaseDatabase.getInstance().getReference("LichSu");

        loadData();
    }

    private void loadData() {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                if (!snapshot.exists()) {
                    updateUI();
                    return;
                }

                // Duyệt qua từng mốc thời gian (key là thời gian)
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String time = snap.getKey(); // Ví dụ: 2025-10-30_15-41-24

                    if (time == null) continue;

                    // --- SỬA LẠI: Tìm node AnhSang bên trong mốc thời gian ---
                    // Cấu trúc mong đợi: LichSu -> [Time] -> AnhSang -> TrangThai

                    String status = null;

                    if (snap.hasChild("AnhSang") && snap.child("AnhSang").hasChild("TrangThai")) {
                        status = snap.child("AnhSang").child("TrangThai").getValue(String.class);
                    }

                    // Nếu không có dữ liệu ánh sáng ở mốc giờ này thì bỏ qua
                    if (status == null) continue;

                    list.add(new LightHistoryItem(time, status));
                }

                // --- SẮP XẾP: Mới nhất lên đầu ---
                Collections.sort(list, new Comparator<LightHistoryItem>() {
                    @Override
                    public int compare(LightHistoryItem o1, LightHistoryItem o2) {
                        // So sánh chuỗi thời gian giảm dần
                        return o2.time.compareTo(o1.time);
                    }
                });

                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LightHistoryActivity.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (list.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }
}