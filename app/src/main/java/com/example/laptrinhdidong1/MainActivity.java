package com.example.laptrinhdidong1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Khai báo các TextView tương ứng với các mục trong giao diện
    private TextView tvPumpSettings;
    private TextView tvLightSettings;
    private TextView tvRoofSettings;
    private TextView tvSoilMoisture; // ví dụ: hiển thị độ ẩm đất

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ các View với ID trong XML
        tvPumpSettings = findViewById(R.id.tv_pump_settings);
        tvLightSettings = findViewById(R.id.tv_light_settings);
        tvRoofSettings = findViewById(R.id.tv_roof_settings);
        tvSoilMoisture = findViewById(R.id.tv_soil_moisture);

        // ====== SỰ KIỆN MỞ TRANG CÀI ĐẶT BƠM NƯỚC ======
        tvPumpSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PumpSettingActivity.class);
                startActivity(intent);
            }
        });

        // ====== SỰ KIỆN MỞ TRANG CÀI ĐẶT ĐÈN ======
        tvLightSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LightSettingActivity.class);
                startActivity(intent);
            }
        });

        // ====== SỰ KIỆN MỞ TRANG CÀI ĐẶT MÁI CHE ======
        tvRoofSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RoofSettingActivity.class);
                startActivity(intent);
            }
        });

        // ====== GỢI Ý: Ở ĐÂY BẠN CÓ THỂ VIẾT LOGIC FIREBASE HOẶC CẢM BIẾN ======
        // Ví dụ:
        // updateSoilMoistureFromFirebase();
    }

    // Ví dụ hàm cập nhật độ ẩm đất (tuỳ chọn)
    private void updateSoilMoistureFromFirebase() {
        // TODO: Lấy dữ liệu từ Firebase và cập nhật vào tvSoilMoisture
        // tvSoilMoisture.setText("Độ ẩm: 65%");
    }
}
