package com.example.laptrinhdidong1;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class LightSettingActivity extends AppCompatActivity {

    private Switch swLight, swAutoMode;
    private TextView tvStatusLabel, tvTimeOn, tvTimeOff;
    private int hourOn = 6, minuteOn = 0;
    private int hourOff = 22, minuteOff = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_setting);

        swLight = findViewById(R.id.swLight);
        swAutoMode = findViewById(R.id.swAutoMode);
        tvStatusLabel = findViewById(R.id.tvStatusLabel);
        tvTimeOn = findViewById(R.id.tvTimeOn);
        tvTimeOff = findViewById(R.id.tvTimeOff);

        // ⚡ Bật / Tắt đèn thủ công
        swLight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                swLight.setText("TẮT");
                tvStatusLabel.setText("Trạng thái đèn: ĐANG BẬT 💡");
            } else {
                swLight.setText("BẬT");
                tvStatusLabel.setText("Trạng thái đèn: ĐANG TẮT 🌑");
            }
        });

        // 🌙 Chế độ tự động
        swAutoMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                swAutoMode.setText("BẬT");
                swLight.setEnabled(false); // vô hiệu công tắc thủ công
                tvStatusLabel.setText("Trạng thái: TỰ ĐỘNG ⚙️");
            } else {
                swAutoMode.setText("TẮT");
                swLight.setEnabled(true);
                tvStatusLabel.setText("Trạng thái: THỦ CÔNG ✋");
            }
        });

        // 🕒 Chọn thời gian bật / tắt
        tvTimeOn.setOnClickListener(v -> showTimePicker(true));
        tvTimeOff.setOnClickListener(v -> showTimePicker(false));
    }

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
                        tvTimeOn.setText("Thời gian bật: " + timeText);
                    } else {
                        hourOff = hourOfDay;
                        minuteOff = minute1;
                        tvTimeOff.setText("Thời gian tắt: " + timeText);
                    }
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }
}
