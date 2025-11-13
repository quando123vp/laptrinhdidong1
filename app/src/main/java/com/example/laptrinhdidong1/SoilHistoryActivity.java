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

public class SoilHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private TextView tvNoData;
    private ImageView btnBack;

    private ArrayList<SoilHistoryItem> historyList = new ArrayList<>();
    private SoilHistoryAdapter adapter;

    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_history);

        rvHistory = findViewById(R.id.rv_soil_history);
        tvNoData = findViewById(R.id.tv_no_data_soil);
        btnBack = findViewById(R.id.btnBackSoil);

        db = FirebaseDatabase.getInstance().getReference("LichSu");

        btnBack.setOnClickListener(v -> onBackPressed());

        setupRecyclerView();
        loadHistory();
    }

    private void setupRecyclerView() {
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SoilHistoryAdapter(this, historyList);
        rvHistory.setAdapter(adapter);
    }

    private void loadHistory() {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                historyList.clear();

                if (!snapshot.exists()) {
                    tvNoData.setVisibility(View.VISIBLE);
                    rvHistory.setVisibility(View.GONE);
                    return;
                }

                tvNoData.setVisibility(View.GONE);
                rvHistory.setVisibility(View.VISIBLE);

                for (DataSnapshot timeSnap : snapshot.getChildren()) {

                    String time = timeSnap.getKey();

                    Integer phanTram = timeSnap.child("DoAmDat/PhanTram").getValue(Integer.class);

                    if (phanTram != null) {
                        historyList.add(new SoilHistoryItem(time, phanTram));
                    }
                }

                Collections.reverse(historyList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SoilHistoryActivity.this, "❌ Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
