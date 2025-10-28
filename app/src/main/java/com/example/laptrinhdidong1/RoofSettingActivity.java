package com.example.laptrinhdidong1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RoofSettingActivity extends AppCompatActivity {

    private Switch swRoof, swAutoModeRoof;
    private TextView tvRoofStatusLabel, tvTempThreshold, tvLightThreshold;
    private SeekBar seekTemp, seekLight;
    private ImageView btnBack;
    private int tempThreshold = 30;
    private int lightThreshold = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roof_setting);

        // 🔹 Ánh xạ View
        btnBack = findViewById(R.id.btnBack);
        swRoof = findViewById(R.id.swRoof);
        swAutoModeRoof = findViewById(R.id.swAutoModeRoof);
        tvRoofStatusLabel = findViewById(R.id.tvRoofStatusLabel);
        tvTempThreshold = findViewById(R.id.tvTempThreshold);
        tvLightThreshold = findViewById(R.id.tvLightThreshold);
        seekTemp = findViewById(R.id.seekTemp);
        seekLight = findViewById(R.id.seekLight);

        // 🔙 Quay lại MainActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RoofSettingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 🚪 Mở / Đóng thủ công
        swRoof.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                swRoof.setText("ĐÓNG");
                tvRoofStatusLabel.setText("Trạng thái mái che: ĐANG MỞ 🌤️");
            } else {
                swRoof.setText("MỞ");
                tvRoofStatusLabel.setText("Trạng thái mái che: ĐANG ĐÓNG 🌧️");
            }
        });

        // 🤖 Chế độ tự động
        swAutoModeRoof.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                swAutoModeRoof.setText("BẬT");
                swRoof.setEnabled(false);
                seekTemp.setEnabled(true);
                seekLight.setEnabled(true);
                tvRoofStatusLabel.setText("Trạng thái: TỰ ĐỘNG ⚙️");
            } else {
                swAutoModeRoof.setText("TẮT");
                swRoof.setEnabled(true);
                seekTemp.setEnabled(false);
                seekLight.setEnabled(false);
                tvRoofStatusLabel.setText("Trạng thái: THỦ CÔNG ✋");
            }
        });

        // 🌡️ Điều chỉnh nhiệt độ
        seekTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tempThreshold = progress;
                tvTempThreshold.setText("Ngưỡng nhiệt độ: " + tempThreshold + "°C");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // ☀️ Điều chỉnh ánh sáng
        seekLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lightThreshold = progress;
                tvLightThreshold.setText("Ngưỡng ánh sáng: " + lightThreshold + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 🔒 Mặc định: khóa thanh khi chưa bật tự động
        seekTemp.setEnabled(false);
        seekLight.setEnabled(false);
    }
}
