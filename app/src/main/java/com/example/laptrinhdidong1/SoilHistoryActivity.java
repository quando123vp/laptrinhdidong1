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

public class SoilHistoryActivity extends AppCompatActivity {

    private RecyclerView rv;
    private SoilHistoryAdapter adapter;
    private ArrayList<SoilHistoryItem> list = new ArrayList<>();

    private TextView tvNoData;
    private ImageView btnBack;

    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_history);

        // 1. Ánh xạ View
        rv = findViewById(R.id.rv_soil_history);
        tvNoData = findViewById(R.id.tv_no_data);
        btnBack = findViewById(R.id.btnBack);

        // 2. Xử lý nút Back
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 3. Cài đặt RecyclerView
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SoilHistoryAdapter(list);
        rv.setAdapter(adapter);

        // 4. Kết nối Firebase (Node tổng "LichSu")
        db = FirebaseDatabase.getInstance().getReference("LichSu");

        // 5. Tải dữ liệu
        loadData();
    }

    private void loadData() {
        // Dùng SingleValueEvent để tải 1 lần lúc mở (nếu muốn tự động cập nhật thì dùng addValueEventListener)
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                if (!snapshot.exists()) {
                    updateUI();
                    return;
                }

                // Duyệt qua từng mốc thời gian trong LichSu
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String timeStr = snap.getKey(); // Ví dụ: 2023-10-25 10:00:00
                    if (timeStr == null) continue;

                    // Tìm dữ liệu Đất: LichSu -> [Time] -> Dat -> PhanTram
                    Double percent = null;

                    // Kiểm tra an toàn xem node con có tồn tại không
                    if (snap.hasChild("Dat") && snap.child("Dat").hasChild("PhanTram")) {
                        try {
                            percent = snap.child("Dat").child("PhanTram").getValue(Double.class);
                        } catch (Exception e) {
                            percent = null;
                        }
                    }

                    // Nếu không có dữ liệu độ ẩm đất ở giờ này, bỏ qua (có thể chỉ có dữ liệu Mưa)
                    if (percent == null) continue;

                    // Thêm vào danh sách
                    list.add(new SoilHistoryItem(timeStr, percent.intValue()));
                }

                // --- SẮP XẾP: MỚI NHẤT LÊN ĐẦU ---
                // So sánh chuỗi String trực tiếp (o2 so với o1 để giảm dần)
                Collections.sort(list, new Comparator<SoilHistoryItem>() {
                    @Override
                    public int compare(SoilHistoryItem o1, SoilHistoryItem o2) {
                        // o2.time so với o1.time -> Giảm dần (Mới nhất lên đầu)
                        // o1.time so với o2.time -> Tăng dần (Cũ nhất lên đầu)
                        return o2.time.compareTo(o1.time);
                    }
                });
                // ---------------------------------

                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SoilHistoryActivity.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
                updateUI();
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
        // Cập nhật lại giao diện
        adapter.notifyDataSetChanged();
    }
}