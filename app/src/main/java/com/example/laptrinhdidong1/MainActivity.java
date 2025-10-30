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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DatabaseReference mDatabase;

    private TextView tvSoilMoisture, tvTempHumid, tvLightIntensity, tvRainStatus;
    private CardView cardSoil, cardTempHumid, cardLightSensor, cardRain;
    private CardView cardPump, cardLight, cardRoof;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔥 Kết nối Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 🧩 Ánh xạ cảm biến
        tvSoilMoisture = findViewById(R.id.tv_soil_moisture);
        tvTempHumid = findViewById(R.id.tv_temp_humid);
        tvLightIntensity = findViewById(R.id.tv_light_intensity);
        tvRainStatus = findViewById(R.id.tv_rain_status);

        cardSoil = findViewById(R.id.card_soil);
        cardTempHumid = findViewById(R.id.card_temp_humid);
        cardLightSensor = findViewById(R.id.card_light_sensor);
        cardRain = findViewById(R.id.card_rain);

        // 🧩 Ánh xạ phần điều khiển
        cardPump = findViewById(R.id.card_pump);
        cardLight = findViewById(R.id.card_light);
        cardRoof = findViewById(R.id.card_roof);

        // 📡 Cập nhật dữ liệu cảm biến realtime
        setupSensorListener();

        // 🧭 Chuyển sang các màn hình khác
        cardSoil.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SoilHistoryActivity.class)));
        cardTempHumid.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TempHumidHistoryActivity.class)));
        cardLightSensor.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LightHistoryActivity.class)));
        cardRain.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RainHistoryActivity.class)));

        // ⚙️ Điều khiển thiết bị
        cardPump.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PumpSettingActivity.class)));
        cardLight.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LightSettingActivity.class)));
        cardRoof.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RoofSettingActivity.class)));
    }

    // ==========================
    // 📡 LẮNG NGHE & GHI LỊCH SỬ
    // ==========================
    private void setupSensorListener() {
        mDatabase.child("CamBien").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                // ✅ Đọc giá trị từ Firebase
                Float nhietDo = snapshot.child("NhietDo").getValue(Float.class);
                Float doAm = snapshot.child("DoAm").getValue(Float.class);
                Long doAmDat = snapshot.child("DoAmDat").getValue(Long.class);
                String sang = snapshot.child("AnhSang/TrangThai").getValue(String.class);
                Long phanTramSang = snapshot.child("AnhSang/PhanTram").getValue(Long.class);

                // 🌧️ Cảm biến mưa (đọc từ nhánh mới)
                String rainStatus = snapshot.child("Mua/TrangThai").getValue(String.class);
                Long rainAnalog = snapshot.child("Mua/Analog").getValue(Long.class);
                Long rainDigital = snapshot.child("Mua/Digital").getValue(Long.class);

                // 🔹 Hiển thị realtime lên UI
                tvTempHumid.setText((nhietDo != null && doAm != null)
                        ? String.format(Locale.getDefault(), "%.1f°C | %.1f%%", nhietDo, doAm)
                        : "--°C | --%");
                tvSoilMoisture.setText(doAmDat != null ? doAmDat + "%" : "--%");
                tvLightIntensity.setText(sang != null ? sang : "--");
                tvRainStatus.setText(rainStatus != null ? rainStatus : "--");

                // 🕒 Ghi thời gian thực
                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                        .format(new Date());

                // 🌿 Ghi lịch sử cảm biến
                DatabaseReference lichSuRef = mDatabase.child("LichSuApp");

                // 🌡️ Lưu lịch sử nhiệt độ & độ ẩm không khí
                if (nhietDo != null && doAm != null) {
                    DatabaseReference node = lichSuRef.child("NhietDo_DoAm").child(timestamp);
                    node.child("NhietDo").setValue(nhietDo);
                    node.child("DoAm").setValue(doAm);
                }

                // ☀️ Lưu lịch sử ánh sáng
                if (sang != null && phanTramSang != null) {
                    DatabaseReference node = lichSuRef.child("AnhSang").child(timestamp);
                    node.child("TrangThai").setValue(sang);
                    node.child("PhanTram").setValue(phanTramSang);
                }

                // 🌧️ Lưu lịch sử cảm biến mưa
                if (rainStatus != null) {
                    DatabaseReference node = lichSuRef.child("Mua").child(timestamp);
                    node.child("TrangThai").setValue(rainStatus);
                    if (rainAnalog != null) node.child("Analog").setValue(rainAnalog);
                    if (rainDigital != null) node.child("Digital").setValue(rainDigital);
                }

                Log.d(TAG, "📜 Ghi lịch sử thành công tại " + timestamp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Lỗi đọc Firebase: ", error.toException());
            }
        });
    }
}
