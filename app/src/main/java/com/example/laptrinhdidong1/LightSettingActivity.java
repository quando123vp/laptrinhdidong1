package com.example.laptrinhdidong1;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial; // ✅ dùng SwitchMaterial của Material Design

import java.util.Calendar;

public class LightSettingActivity extends AppCompatActivity {

    private SwitchMaterial swLight, swAutoMode; // ✅ Sửa kiểu SwitchMaterial
    private TextView tvStatusLabel, tvTimeOn, tvTimeOff;
    private ImageView btnBackLight;

    private int hourOn = 6, minuteOn = 0;
    private int hourOff = 22, minuteOff = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_setting);

        // ====== ÁNH XẠ VIEW ======
        swLight = findViewById(R.id.swLight);
        swAutoMode = findViewById(R.id.swAutoMode);
        tvStatusLabel = findViewById(R.id.tvStatusLabel);
        tvTimeOn = findViewById(R.id.tvTimeOn);
        tvTimeOff = findViewById(R.id.tvTimeOff);
        btnBackLight = findViewById(R.id.btnBackLight);

        // ====== NÚT QUAY LẠI ======
        btnBackLight.setOnClickListener(v -> {
            onBackPressed(); // quay lại nhanh, không tạo Activity mới
        });

        // ====== BẬT / TẮT ĐÈN THỦ CÔNG ======
        swLight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvStatusLabel.setText("💡 Đèn đang BẬT");
            } else {
                tvStatusLabel.setText("🌑 Đèn đang TẮT");
            }
        });

        // ====== CHẾ ĐỘ TỰ ĐỘNG ======
        swAutoMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                swLight.setEnabled(false);
                tvStatusLabel.setText("⚙️ Đang ở chế độ TỰ ĐỘNG");
                Toast.makeText(this, "Đã bật chế độ tự động", Toast.LENGTH_SHORT).show();
            } else {
                swLight.setEnabled(true);
                tvStatusLabel.setText("✋ Đang ở chế độ THỦ CÔNG");
                Toast.makeText(this, "Đã tắt chế độ tự động", Toast.LENGTH_SHORT).show();
            }
        });

        // ====== CHỌN THỜI GIAN BẬT / TẮT ======
        tvTimeOn.setOnClickListener(v -> showTimePicker(true));
        tvTimeOff.setOnClickListener(v -> showTimePicker(false));
    }

    // =============================
    // 🕒 HỘP CHỌN GIỜ
    // =============================
    private void showTimePicker(boolean isTimeOn) {
        final Calendar c = Calendar.getInstance();
        int hour = isTimeOn ? hourOn : hourOff;
        int minute = isTimeOn ? minuteOn : minuteOff;

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute1) -> {
                    String timeText = String.format("%02d:%02d", hourOfDay, minute1);

                    if (isTimeOn) {
                        hourOn = hourOfDay;
                        minuteOn = minute1;
                        tvTimeOn.setText("Bật: " + timeText);
                    } else {
                        hourOff = hourOfDay;
                        minuteOff = minute1;
                        tvTimeOff.setText("Tắt: " + timeText);
                    }
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        // ✅ Quay về MainActivity không tạo stack mới
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
