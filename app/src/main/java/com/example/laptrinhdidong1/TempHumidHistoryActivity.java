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

public class TempHumidHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private TempHumidHistoryAdapter adapter;
    private ArrayList<TempHumidHistoryItem> list = new ArrayList<>();
    private TextView tvNoData;
    private ImageView btnBack;

    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_humid_history);

        rvHistory = findViewById(R.id.rv_temp_humid_history);
        tvNoData = findViewById(R.id.tv_no_data);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TempHumidHistoryAdapter(list);
        rvHistory.setAdapter(adapter);

        db = FirebaseDatabase.getInstance().getReference("LichSu");

        loadHistory();
    }

    private void loadHistory() {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                if (!snapshot.exists()) {
                    tvNoData.setVisibility(View.VISIBLE);
                    rvHistory.setVisibility(View.GONE);
                    return;
                }

                tvNoData.setVisibility(View.GONE);
                rvHistory.setVisibility(View.VISIBLE);

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String time = snap.getKey();

                    Double t = snap.child("NhietDo").getValue(Double.class);
                    Double h = snap.child("DoAm").getValue(Double.class);

                    if (t != null && h != null) {
                        list.add(new TempHumidHistoryItem(time, t.floatValue(), h.floatValue()));
                    }
                }

                Collections.reverse(list);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TempHumidHistoryActivity.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
