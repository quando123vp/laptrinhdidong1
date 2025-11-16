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

        rv = findViewById(R.id.rv_soil_history);
        tvNoData = findViewById(R.id.tv_no_data);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SoilHistoryAdapter(list);
        rv.setAdapter(adapter);

        db = FirebaseDatabase.getInstance().getReference("LichSu");

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

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String time = snap.getKey();
                    Long percent = snap.child("Dat").getValue(Long.class);

                    if (percent != null) {
                        list.add(new SoilHistoryItem(time, percent.intValue()));
                    }
                }

                Collections.reverse(list);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SoilHistoryActivity.this, "Lỗi tải!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
