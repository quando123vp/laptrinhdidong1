package com.example.laptrinhdidong1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RoofSettingActivity extends AppCompatActivity {

    private SwitchMaterial swRoof, swAutoModeRoof;
    private TextView tvRoofStatusLabel, tvTempThreshold, tvLightThreshold;
    private SeekBar seekTemp, seekLight;
    private ImageView btnBack;

    private int tempThreshold = 30;
    private int lightThreshold = 60;

    // 🔥 Firebase reference
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roof_setting);

        // ========== ÁNH XẠ VIEW ==========
        btnBack = findViewById(R.id.btnBack);
        swRoof = findViewById(R.id.swRoof);
        swAutoModeRoof = findViewById(R.id.swAutoModeRoof);
        tvRoofStatusLabel = findViewById(R.id.tvRoofStatusLabel);
        tvTempThreshold = findViewById(R.id.tvTempThreshold);
        tvLightThreshold = findViewById(R.id.tvLightThreshold);
        seekTemp = findViewById(R.id.seekTemp);
        seekLight = findViewById(R.id.seekLight);

        // ========== KẾT NỐI FIREBASE ==========
        dbRef = FirebaseDatabase.getInstance().getReference("HeThongMaiChe");

        // 🔙 Nút quay lại
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RoofSettingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 🚪 Bật / Tắt mái che THỦ CÔNG
        swRoof.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvRoofStatusLabel.setText("Trạng thái: ĐANG MỞ 🌤️");
                dbRef.child("TrangThai").setValue("Mo");
            } else {
                tvRoofStatusLabel.setText("Trạng thái: ĐANG ĐÓNG 🌧️");
                dbRef.child("TrangThai").setValue("Dong");
            }
        });

        // ⚙️ Bật / Tắt chế độ tự động
        swAutoModeRoof.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbRef.child("AutoMode").setValue(isChecked);

            if (isChecked) {
                swRoof.setEnabled(false);
                seekTemp.setEnabled(true);
                seekLight.setEnabled(true);
                tvRoofStatusLabel.setText("TỰ ĐỘNG ⚙️");
            } else {
                swRoof.setEnabled(true);
                seekTemp.setEnabled(false);
                seekLight.setEnabled(false);
                tvRoofStatusLabel.setText("THỦ CÔNG ✋");
            }
        });

        // 🌡️ Thanh điều chỉnh NGƯỠNG NHIỆT ĐỘ
        seekTemp.setMax(50);
        seekTemp.setProgress(tempThreshold);
        tvTempThreshold.setText("Ngưỡng nhiệt độ: " + tempThreshold + "°C");

        seekTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tempThreshold = progress;
                tvTempThreshold.setText("Ngưỡng nhiệt độ: " + tempThreshold + "°C");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                dbRef.child("NguongNhietDo").setValue(tempThreshold);
            }
        });

        // ☀️ Thanh điều chỉnh NGƯỠNG ÁNH SÁNG
        seekLight.setMax(100);
        seekLight.setProgress(lightThreshold);
        tvLightThreshold.setText("Ngưỡng ánh sáng: " + lightThreshold + "%");

        seekLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lightThreshold = progress;
                tvLightThreshold.setText("Ngưỡng ánh sáng: " + lightThreshold + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                dbRef.child("NguongAnhSang").setValue(lightThreshold);
            }
        });

        // 🔒 Khóa SeekBar khi chưa bật tự động
        seekTemp.setEnabled(false);
        seekLight.setEnabled(false);
    }
}
