package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DatabaseReference mDatabase;

    private TextView tvSoilMoisture, tvTempHumid, tvLightIntensity, tvRainStatus;
    private CardView cardSoil, cardTempHumid, cardLight, cardRain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔥 Kết nối Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 🧩 Liên kết UI
        tvSoilMoisture = findViewById(R.id.tv_soil_moisture);
        tvTempHumid = findViewById(R.id.tv_temp_humid);
        tvLightIntensity = findViewById(R.id.tv_light_intensity);
        tvRainStatus = findViewById(R.id.tv_rain_status);

        cardSoil = findViewById(R.id.card_soil);
        cardTempHumid = findViewById(R.id.card_temp_humid);
        cardLight = findViewById(R.id.card_light_sensor);
        cardRain = findViewById(R.id.card_rain);

        // 📡 Đọc dữ liệu cảm biến
        setupSensorListener();

        // 🧭 Mở từng lịch sử riêng
        cardSoil.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SoilHistoryActivity.class)));

        cardTempHumid.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TempHumidHistoryActivity.class)));

        cardLight.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LightHistoryActivity.class)));

        cardRain.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RainHistoryActivity.class)));
    }

    private void setupSensorListener() {
        mDatabase.child("CamBien").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Float nhietDo = snapshot.child("NhietDo").getValue(Float.class);
                Float doAm = snapshot.child("DoAm").getValue(Float.class);
                Long doAmDat = snapshot.child("DoAmDat").getValue(Long.class);
                String mua = snapshot.child("TrangThaiMua").getValue(String.class);
                String sang = snapshot.child("AnhSang/TrangThai").getValue(String.class);

                tvTempHumid.setText((nhietDo != null && doAm != null)
                        ? String.format("%.1f°C | %.1f%%", nhietDo, doAm)
                        : "--°C | --%");
                tvSoilMoisture.setText(doAmDat != null ? doAmDat + "%" : "--%");
                tvLightIntensity.setText(sang != null ? sang : "--");
                tvRainStatus.setText(mua != null ? mua : "--");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi đọc Firebase", error.toException());
            }
        });
    }
}
