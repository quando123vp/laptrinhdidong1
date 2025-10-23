package com.example.laptrinhdidong1;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class PumpSettingActivity extends AppCompatActivity {

    private Button btnAddTime, btnManualPump;
    private LinearLayout llScheduledTimesContainer;
    private TextView tvClearAll, tvPumpStatus;
    private EditText etWaterAmount;
    private Handler handler = new Handler();

    // Giả lập độ ẩm đất hiện tại
    private int currentMoisture = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pump_setting);

        // Bơm tự động
        btnAddTime = findViewById(R.id.btn_add_time);
        llScheduledTimesContainer = findViewById(R.id.ll_scheduled_times_container);
        tvClearAll = findViewById(R.id.tv_clear_all);

        // Bơm thủ công
        btnManualPump = findViewById(R.id.btn_manual_pump);
        etWaterAmount = findViewById(R.id.et_water_amount);
        tvPumpStatus = findViewById(R.id.tv_pump_status);

        // Ẩn trạng thái bơm khi mới mở
        tvPumpStatus.setVisibility(View.GONE);

        // ====== Xử lý nút thêm giờ bơm ======
        btnAddTime.setOnClickListener(v -> showTimePickerDialog());

        // ====== Xử lý nút xóa tất cả giờ ======
        tvClearAll.setOnClickListener(v -> {
            llScheduledTimesContainer.removeAllViews();
            Toast.makeText(this, "Đã xóa tất cả giờ bơm!", Toast.LENGTH_SHORT).show();
        });

        // ====== Xử lý nút BƠM thủ công ======
        btnManualPump.setOnClickListener(v -> startManualPump());
    }

    // ---------------- BƠM THỦ CÔNG ---------------- //
    private void startManualPump() {
        String targetStr = etWaterAmount.getText().toString().trim();
        if (targetStr.isEmpty()) {
            Toast.makeText(this, "Nhập độ ẩm muốn bơm!", Toast.LENGTH_SHORT).show();
            return;
        }

        int target = Integer.parseInt(targetStr);
        if (target <= currentMoisture) {
            Toast.makeText(this, "Độ ẩm đã đủ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiện thông báo khi bắt đầu bơm
        tvPumpStatus.setVisibility(View.VISIBLE);
        tvPumpStatus.setText("💧 Đang bơm...");

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

                    // Ẩn sau 2 giây
                    handler.postDelayed(() -> tvPumpStatus.setVisibility(View.GONE), 2000);
                }
            }
        }, 150);
    }

    // ---------------- BƠM TỰ ĐỘNG ---------------- //
    private void showTimePickerDialog() {
        // Hiển thị dạng nhập số (bỏ đồng hồ tròn)
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,
                (view, hourOfDay, minute) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    showRepeatDialog(selectedTime);
                },
                8, 0, true
        );

        timePickerDialog.setTitle("Chọn giờ bơm");
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();
    }

    // Dialog chọn kiểu lặp lại (Một lần / Mỗi ngày / Theo thứ)
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

    // Dialog chọn thứ cụ thể
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

    // ---------------- HIỂN THỊ DANH SÁCH GIỜ ---------------- //
    private void addTimeRow(String time) {
        // Tạo một hàng ngang
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 8, 0, 8);

        // Icon đồng hồ hoặc lịch lặp
        ImageView clockIcon = new ImageView(this);
        if (time.contains("Mỗi ngày") || time.contains("Thứ"))
            clockIcon.setImageResource(android.R.drawable.ic_popup_sync);
        else
            clockIcon.setImageResource(android.R.drawable.ic_lock_idle_alarm);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(60, 60);
        clockIcon.setLayoutParams(iconParams);

        // Text hiển thị giờ (cho phép xuống dòng)
        TextView tvTime = new TextView(this);
        tvTime.setText(time);
        tvTime.setTextSize(17);
        tvTime.setPadding(16, 0, 0, 0);
        tvTime.setSingleLine(false);
        tvTime.setMaxLines(3);
        tvTime.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)); // chiếm phần còn lại

        // Nút ❌ xóa
        TextView tvDelete = new TextView(this);
        tvDelete.setText("❌");
        tvDelete.setTextSize(20);
        tvDelete.setPadding(16, 0, 0, 0);
        tvDelete.setOnClickListener(v -> {
            llScheduledTimesContainer.removeView(row);
            Toast.makeText(this, "Đã xóa " + time, Toast.LENGTH_SHORT).show();
        });

        // Thêm tất cả vào hàng
        row.addView(clockIcon);
        row.addView(tvTime);
        row.addView(tvDelete);

        // Thêm hàng vào container
        llScheduledTimesContainer.addView(row);
    }
}
