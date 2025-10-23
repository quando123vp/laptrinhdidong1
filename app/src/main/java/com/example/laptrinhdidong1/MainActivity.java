package com.example.laptrinhdidong1;
// Đảm bảo tên package là chính xác

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Khai báo các thành phần UI (sẽ cần thêm các TextView và Switch khác)
    private TextView tvSoilMoisture;
    private Switch swWaterPump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dòng này liên kết Activity với layout XML
        setContentView(R.layout.activity_main);

        // Liên kết các thành phần UI bằng findViewById
        tvSoilMoisture = findViewById(R.id.tv_soil_moisture);
        swWaterPump = findViewById(R.id.sw_water_pump);
        // ... Cần thực hiện findViewById cho tất cả các ID còn lại

        // THIẾT LẬP GIÁ TRỊ THỬ NGHIỆM ĐỂ XEM GIAO DIỆN CÓ CHẠY KHÔNG

        swWaterPump.setChecked(true);

        // TODO: Viết logic Firebase ở đây
    }
}