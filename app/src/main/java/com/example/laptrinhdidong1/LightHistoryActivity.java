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

                    String st = snap.child("AnhSang/TrangThai").getValue(String.class);
                    Long analog = snap.child("AnhSang/Analog").getValue(Long.class);

                    if (st != null && analog != null) {
                        list.add(new LightHistoryItem(time, st, analog.intValue()));
                    }
                }

                Collections.reverse(list);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LightHistoryActivity.this, "Lỗi!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
