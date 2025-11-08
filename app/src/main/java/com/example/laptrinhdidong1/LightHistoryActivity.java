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

    // 🔹 THAY ĐỔI: Sử dụng RecyclerView
    private RecyclerView rvHistory;
    private TextView tvNoData;
    private LightHistoryAdapter adapter;
    private ArrayList<LightHistoryItem> historyList = new ArrayList<>();
    // 🔹 KẾT THÚC THAY ĐỔI

    private ImageView btnBack;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_history);

        // 🔗 Ánh xạ view
        // 🔹 THAY ĐỔI: Ánh xạ view mới
        rvHistory = findViewById(R.id.rv_light_history);
        tvNoData = findViewById(R.id.tv_no_data);
        btnBack = findViewById(R.id.btnBackLight);
        // 🔹 KẾT THÚC THAY ĐỔI

        // 🔥 Kết nối đúng node "LichSu"
        db = FirebaseDatabase.getInstance().getReference("LichSu");

        // 🔙 Nút quay lại
        btnBack.setOnClickListener(v -> onBackPressed());

        // 🚀 Setup RecyclerView
        setupRecyclerView();

        // 📜 Tải lịch sử
        loadHistory();
    }

    private void setupRecyclerView() {
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LightHistoryAdapter(this, historyList);
        rvHistory.setAdapter(adapter);
    }

    private void loadHistory() {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();

                if (!snapshot.exists()) {
                    // 🔹 THAY ĐỔI: Hiển thị thông báo "No Data"
                    tvNoData.setVisibility(View.VISIBLE);
                    rvHistory.setVisibility(View.GONE);
                    // 🔹 KẾT THÚC THAY ĐỔI
                    return;
                }

                // 🔹 THAY ĐỔI: Ẩn thông báo "No Data"
                tvNoData.setVisibility(View.GONE);
                rvHistory.setVisibility(View.VISIBLE);
                // 🔹 KẾT THÚC THAY ĐỔI

                // Duyệt qua từng bản ghi thời gian
                for (DataSnapshot timeSnap : snapshot.getChildren()) {
                    String time = timeSnap.getKey();

                    // Lấy dữ liệu từ sub-node "AnhSang"
                    String trangThai = timeSnap.child("AnhSang/TrangThai").getValue(String.class);
                    Integer phanTram = timeSnap.child("AnhSang/PhanTram").getValue(Integer.class);

                    if (trangThai != null) {
                        // 🔹 THAY ĐỔI: Thêm vào list cho RecyclerView
                        historyList.add(new LightHistoryItem(time, trangThai, phanTram));
                        // 🔹 KẾT THÚC THAY ĐỔI
                    }
                }

                // 🔹 THAY ĐỔI: Đảo ngược list để hiển thị mục mới nhất lên đầu
                Collections.reverse(historyList);
                // Thông báo cho adapter biết dữ liệu đã thay đổi
                adapter.notifyDataSetChanged();
                // 🔹 KẾT THÚC THAY ĐỔI
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LightHistoryActivity.this, "❌ Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                tvNoData.setText("❌ Lỗi tải dữ liệu");
                tvNoData.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
            }
        });
    }

    // ⛔ KHÔNG CẦN HÀM NÀY NỮA ⛔
    // private void addText(String text) { ... }
}