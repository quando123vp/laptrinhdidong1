package com.example.laptrinhdidong1;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class LightSettingActivity extends AppCompatActivity {

    private Switch swLight, swAutoMode;
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
            Intent intent = new Intent(LightSettingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // ====== BẬT / TẮT ĐÈN THỦ CÔNG ======
        swLight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                swLight.setText("TẮT");
                tvStatusLabel.setText("Trạng thái đèn: ĐANG BẬT 💡");
            } else {
                swLight.setText("BẬT");
                tvStatusLabel.setText("Trạng thái đèn: ĐANG TẮT 🌑");
            }
        });

        // ====== CHẾ ĐỘ TỰ ĐỘNG ======
        swAutoMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                swAutoMode.setText("BẬT");
                swLight.setEnabled(false); // Khóa công tắc thủ công
                tvStatusLabel.setText("Trạng thái: TỰ ĐỘNG ⚙️");
            } else {
                swAutoMode.setText("TẮT");
                swLight.setEnabled(true);
                tvStatusLabel.setText("Trạng thái: THỦ CÔNG ✋");
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
                        tvTimeOn.setText(timeText);
                    } else {
                        hourOff = hourOfDay;
                        minuteOff = minute1;
                        tvTimeOff.setText(timeText);
                    }
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }
}
