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

        btnBack.setOnClickListener(v -> finish());

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LightHistoryAdapter(list);
        rv.setAdapter(adapter);

        // 🔥 CHỈ LẤY LichSu/AnhSang
        db = FirebaseDatabase.getInstance().getReference("LichSu").child("AnhSang");

        loadData();
    }

    private void loadData() {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                if (!snapshot.exists()) {
                    tvNoData.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                    return;
                }

                tvNoData.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);

                // 🟢 Firebase dạng:
                // LichSu
                //   └── AnhSang
                //          └── 2025-10-30_15-41-24
                //                ├── PhanTram: 100
                //                └── TrangThai: "Sáng"

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String time = snap.getKey();
                    String status = snap.child("TrangThai").getValue(String.class);

                    if (status != null && time != null) {
                        list.add(new LightHistoryItem(time, status));
                    }
                }

                // Đảo ngược: mới nhất lên đầu
                Collections.reverse(list);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LightHistoryActivity.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
