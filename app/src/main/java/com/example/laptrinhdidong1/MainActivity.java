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

    // Firebase
    private DatabaseReference mDatabase;

    // Cảm biến
    private TextView tvSoilMoisture;
    private TextView tvTempHumid;
    private TextView tvLightIntensity;
    private TextView tvRainStatus;

    // CardView điều hướng
    private CardView cardPump;
    private CardView cardLight;
    private CardView cardRoof;

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

        cardPump = findViewById(R.id.card_pump);
        cardLight = findViewById(R.id.card_light);
        cardRoof = findViewById(R.id.card_roof);

        // 📡 Đọc dữ liệu cảm biến từ Firebase
        setupSensorDataListener();

        // 🧭 Thiết lập điều hướng khi nhấn các CardView
        setupNavigationCards();
    }

    /**
     * 📡 Đọc dữ liệu cảm biến từ Firebase
     */
    private void setupSensorDataListener() {
        mDatabase.child("CamBien").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                // 🔸 Đọc nhiệt độ & độ ẩm
                Float nhietDo = snapshot.child("NhietDo").getValue(Float.class);
                Float doAm = snapshot.child("DoAm").getValue(Float.class);
                if (nhietDo != null && doAm != null) {
                    tvTempHumid.setText(nhietDo + "°C | " + doAm + "%");
                }

                // 🔸 Đọc ánh sáng
                DataSnapshot lightSnap = snapshot.child("AnhSang");
                if (lightSnap.exists()) {
                    String trangThai = lightSnap.child("TrangThai").getValue(String.class);
                    tvLightIntensity.setText(trangThai != null ? "" + trangThai : "N/A");
                }

                // 🔸 Đọc độ ẩm đất
                Long doAmDat = snapshot.child("DoAmDat").getValue(Long.class);
                if (doAmDat != null) {
                    tvSoilMoisture.setText(doAmDat + "%");
                }

                // 🔸 Đọc trạng thái mưa
                String trangThaiMua = snapshot.child("TrangThaiMua").getValue(String.class);
                if (trangThaiMua != null) {
                    tvRainStatus.setText(trangThaiMua);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "❌ Lỗi đọc Firebase: ", error.toException());
            }
        });
    }

    /**
     * 🧭 Khi nhấn vào CardView → chuyển sang Activity tương ứng
     */
    private void setupNavigationCards() {
        cardPump.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PumpSettingActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        cardLight.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LightSettingActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        cardRoof.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RoofSettingActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }
}
