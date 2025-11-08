package com.example.laptrinhdidong1;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class PumpSettingActivity extends AppCompatActivity {

    private MaterialButton btnAddTime, btnManualPump;
    private LinearLayout llScheduledTimesContainer;
    private TextView tvClearAll, tvPumpStatus, tvCurrentMoisture;
    private EditText etWaterAmount;
    private ImageView btnBackPump;
    private Handler handler = new Handler();

    // 🔹 Giả lập độ ẩm đất hiện tại
    private int currentMoisture = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pump_setting);

        // ===== ÁNH XẠ VIEW =====
        btnAddTime = findViewById(R.id.btn_add_time);
        llScheduledTimesContainer = findViewById(R.id.ll_scheduled_times_container);
        tvClearAll = findViewById(R.id.tv_clear_all);
        btnManualPump = findViewById(R.id.btn_manual_pump);
        etWaterAmount = findViewById(R.id.et_water_amount);
        tvPumpStatus = findViewById(R.id.tv_pump_status);
        btnBackPump = findViewById(R.id.btnBackPump);
        tvCurrentMoisture = findViewById(R.id.tv_current_moisture);

        // 🔙 Quay lại MainActivity
        btnBackPump.setOnClickListener(v -> onBackPressed());

        // Cập nhật độ ẩm đất ban đầu
        tvCurrentMoisture.setText(currentMoisture + " %");
        tvPumpStatus.setVisibility(TextView.GONE);

        // ➕ Thêm giờ bơm tự động
        btnAddTime.setOnClickListener(v -> showTimePickerDialog());

        // 🗑️ Xóa tất cả lịch bơm
        tvClearAll.setOnClickListener(v -> {
            llScheduledTimesContainer.removeAllViews();
            Toast.makeText(this, "Đã xóa tất cả giờ bơm!", Toast.LENGTH_SHORT).show();
        });

        // 💧 Bơm thủ công
        btnManualPump.setOnClickListener(v -> startManualPump());
    }

    // =========================
    // 💧 BƠM THỦ CÔNG
    // =========================
    private void startManualPump() {
        String targetStr = etWaterAmount.getText().toString().trim();
        if (targetStr.isEmpty()) {
            Toast.makeText(this, "⚠️ Nhập độ ẩm muốn bơm!", Toast.LENGTH_SHORT).show();
            return;
        }

        int target = Integer.parseInt(targetStr);
        if (target <= currentMoisture) {
            Toast.makeText(this, "✅ Độ ẩm đã đủ, không cần bơm!", Toast.LENGTH_SHORT).show();
            return;
        }

        tvPumpStatus.setVisibility(TextView.VISIBLE);
        tvPumpStatus.setText("💧 Đang bơm... " + currentMoisture + "%");
        simulatePumping(target);
    }

    private void simulatePumping(int target) {
        final int[] progress = {currentMoisture};

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progress[0] < target) {
                    progress[0]++;
                    tvPumpStatus.setText("💧 Đang bơm... " + progress[0] + "%");
                    handler.postDelayed(this, 150);
                } else {
                    tvPumpStatus.setText("✅ Đã đạt " + target + "% – Dừng bơm!");
                    currentMoisture = target;
                    tvCurrentMoisture.setText(currentMoisture + " %");
                    handler.postDelayed(() -> tvPumpStatus.setVisibility(TextView.GONE), 2000);
                }
            }
        }, 150);
    }

    // =========================
    // ⏰ BƠM TỰ ĐỘNG
    // =========================
    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    showRepeatDialog(selectedTime);
                },
                8, 0, true
        );
        timePickerDialog.setTitle("Chọn giờ bơm");
        timePickerDialog.show();
    }

    private void showRepeatDialog(String selectedTime) {
        String[] repeatOptions = {"Một lần", "Mỗi ngày", "Theo thứ..."};

        new AlertDialog.Builder(this)
                .setTitle("Lặp lại lịch bơm")
                .setItems(repeatOptions, (dialog, which) -> {
                    String repeatType = repeatOptions[which];
                    if (repeatType.equals("Theo thứ...")) {
                        showDayPickerDialog(selectedTime);
                    } else {
                        addTimeRow(selectedTime + " (" + repeatType + ")");
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDayPickerDialog(String selectedTime) {
        String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        boolean[] checkedDays = new boolean[days.length];

        new AlertDialog.Builder(this)
                .setTitle("Chọn ngày bơm")
                .setMultiChoiceItems(days, checkedDays, (dialog, which, isChecked) -> checkedDays[which] = isChecked)
                .setPositiveButton("OK", (dialog, which) -> {
                    StringBuilder selectedDays = new StringBuilder();
                    for (int i = 0; i < days.length; i++) {
                        if (checkedDays[i]) {
                            if (selectedDays.length() > 0) selectedDays.append(", ");
                            selectedDays.append(days[i]);
                        }
                    }
                    if (selectedDays.length() == 0)
                        selectedDays.append("Không chọn");

                    addTimeRow(selectedTime + " (" + selectedDays + ")");
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addTimeRow(String time) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 8, 0, 8);

        ImageView clockIcon = new ImageView(this);
        if (time.contains("Mỗi ngày") || time.contains("Thứ"))
            clockIcon.setImageResource(android.R.drawable.ic_popup_sync);
        else
            clockIcon.setImageResource(android.R.drawable.ic_lock_idle_alarm);

        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(60, 60);
        clockIcon.setLayoutParams(iconParams);

        TextView tvTime = new TextView(this);
        tvTime.setText(time);
        tvTime.setTextSize(16);
        tvTime.setPadding(16, 0, 0, 0);
        tvTime.setTextColor(getResources().getColor(android.R.color.black));
        tvTime.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvDelete = new TextView(this);
        tvDelete.setText("❌");
        tvDelete.setTextSize(18);
        tvDelete.setPadding(16, 0, 0, 0);
        tvDelete.setOnClickListener(v -> {
            llScheduledTimesContainer.removeView(row);
            Toast.makeText(this, "Đã xóa " + time, Toast.LENGTH_SHORT).show();
        });

        row.addView(clockIcon);
        row.addView(tvTime);
        row.addView(tvDelete);
        llScheduledTimesContainer.addView(row);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
