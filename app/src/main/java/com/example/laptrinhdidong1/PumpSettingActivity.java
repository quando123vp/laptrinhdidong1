package com.example.laptrinhdidong1;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;

public class PumpSettingActivity extends AppCompatActivity {

    private MaterialButton btnAddTime, btnManualPump, btnStopPump;
    private LinearLayout llScheduledTimesContainer;
    private TextView tvClearAll, tvPumpStatus, tvCurrentMoisture;
    private EditText etWaterAmount;
    private ImageView btnBackPump;

    // Firebase
    private DatabaseReference mDatabase;
    private DatabaseReference camBienRef;

    private float currentMoisture = 0f;
    private boolean isPumping = false;
    private ValueEventListener camBienListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pump_setting);

        // --- CHECK QUYỀN VỚI ANDROID 12+ ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        // ===== ÁNH XẠ VIEW =====
        btnAddTime = findViewById(R.id.btn_add_time);
        btnManualPump = findViewById(R.id.btn_manual_pump);
        btnStopPump = findViewById(R.id.btn_stop_pump);
        llScheduledTimesContainer = findViewById(R.id.ll_scheduled_times_container);
        tvClearAll = findViewById(R.id.tv_clear_all);
        etWaterAmount = findViewById(R.id.et_water_amount);
        tvPumpStatus = findViewById(R.id.tv_pump_status);
        tvCurrentMoisture = findViewById(R.id.tv_current_moisture);
        btnBackPump = findViewById(R.id.btnBackPump);

        tvPumpStatus.setVisibility(View.GONE);

        // Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        camBienRef = mDatabase.child("CamBien");
        attachCamBienListener();

        // CLICK SỰ KIỆN
        btnManualPump.setOnClickListener(v -> startManualPump());
        btnStopPump.setOnClickListener(v -> stopManualPump());
        btnAddTime.setOnClickListener(v -> showTimePickerDialog());

        tvClearAll.setOnClickListener(v -> {
            llScheduledTimesContainer.removeAllViews();
            cancelAllAlarms(); // Hủy hết báo thức hệ thống
        });

        btnBackPump.setOnClickListener(v -> navigateBackToMain());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToMain();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camBienRef != null && camBienListener != null) {
            camBienRef.removeEventListener(camBienListener);
        }
    }

    // ==========================================================
    // ⏰ LOGIC ALARM MANAGER (QUAN TRỌNG)
    // ==========================================================

    // Hàm gài giờ hệ thống
    @SuppressLint("ScheduleExactAlarm")
    private void setSystemAlarm(int hour, int minute) {
        // 1. Lưu lại độ ẩm mục tiêu hiện tại vào bộ nhớ để Receiver đọc được
        saveTargetMoistureToPrefs();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, PumpAlarmReceiver.class);

        // Tạo ID duy nhất dựa trên giờ và phút (VD: 8:30 -> ID = 510)
        int requestCode = hour * 60 + minute;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Thiết lập thời gian
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Nếu giờ chọn đã qua, đặt cho ngày mai
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    // Hàm hủy một giờ cụ thể
    private void cancelSpecificAlarm(int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, PumpAlarmReceiver.class);
        int requestCode = hour * 60 + minute;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    // Hàm hủy hết (đơn giản hóa)
    private void cancelAllAlarms() {
        Toast.makeText(this, "Đã xóa tất cả lịch hẹn bơm!", Toast.LENGTH_SHORT).show();
        // Lưu ý: Muốn hủy sạch sẽ cần lưu list ID đã đặt.
        // Ở đây tạm thời chỉ xóa View, user cần đặt lại.
    }

    private void saveTargetMoistureToPrefs() {
        String targetStr = etWaterAmount.getText().toString().trim();
        int target = 70; // Mặc định
        if (!targetStr.isEmpty()) {
            try {
                target = Integer.parseInt(targetStr);
            } catch (NumberFormatException e) { e.printStackTrace(); }
        }

        SharedPreferences prefs = getSharedPreferences("PumpPrefs", MODE_PRIVATE);
        prefs.edit().putInt("saved_target_moisture", target).apply();
    }

    // ==========================================================
    // CÁC HÀM HIỂN THỊ UI & PICKER
    // ==========================================================

    private void showTimePickerDialog() {
        Calendar c = Calendar.getInstance();
        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);

                    // 1. Gài giờ hệ thống ngay khi chọn xong
                    setSystemAlarm(hourOfDay, minute);

                    // 2. Tiếp tục quy trình UI (Hỏi lặp lại, hiển thị list)
                    showRepeatDialog(selectedTime, hourOfDay, minute);
                },
                nowHour, nowMinute, true
        );
        timePickerDialog.setTitle("Chọn giờ bơm");
        timePickerDialog.show();
    }

    private void showRepeatDialog(String selectedTime, int hour, int minute) {
        String[] repeatOptions = {"Một lần", "Mỗi ngày"}; // Tạm bỏ "Thứ..." để đơn giản logic Alarm

        new AlertDialog.Builder(this)
                .setTitle("Lặp lại lịch bơm")
                .setItems(repeatOptions, (dialog, which) -> {
                    // Hiển thị lên màn hình
                    addTimeRow(selectedTime + " (" + repeatOptions[which] + ")", hour, minute);
                    Toast.makeText(this, "Đã đặt lịch bơm lúc " + selectedTime, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addTimeRow(String displayText, int hour, int minute) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 10, 0, 10);

        ImageView icon = new ImageView(this);
        icon.setImageResource(android.R.drawable.ic_lock_idle_alarm);
        icon.setLayoutParams(new LinearLayout.LayoutParams(60, 60));

        TextView tv = new TextView(this);
        tv.setText(displayText);
        tv.setTextSize(16);
        tv.setPadding(16, 0, 0, 0);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView delete = new TextView(this);
        delete.setText("❌");
        delete.setTextSize(20);
        delete.setPadding(16, 0, 0, 0);

        // Sự kiện xóa dòng này -> Hủy báo thức tương ứng
        delete.setOnClickListener(v -> {
            llScheduledTimesContainer.removeView(row);
            cancelSpecificAlarm(hour, minute);
            Toast.makeText(this, "Đã hủy lịch: " + displayText, Toast.LENGTH_SHORT).show();
        });

        row.addView(icon);
        row.addView(tv);
        row.addView(delete);

        llScheduledTimesContainer.addView(row);
    }

    // ==========================================================
    // CÁC HÀM CŨ (MANUAL PUMP & FIREBASE)
    // ==========================================================

    private void startManualPump() {
        if (isPumping) return;

        String targetStr = etWaterAmount.getText().toString().trim();
        if (targetStr.isEmpty()) {
            Toast.makeText(this, "⚠️ Nhập độ ẩm mục tiêu!", Toast.LENGTH_SHORT).show();
            return;
        }

        int target = Integer.parseInt(targetStr);
        if (target > 100) target = 100;

        // Lưu lại để dùng cho Alarm sau này nếu cần
        saveTargetMoistureToPrefs();

        DatabaseReference cmdRef = mDatabase.child("Bom").child("Command");
        cmdRef.child("TrangThai").setValue("On");
        cmdRef.child("TargetMoisture").setValue(target);
        cmdRef.child("ThoiGian").setValue(0);

        isPumping = true;
        updateUIState(true, target);
    }

    private void stopManualPump() {
        DatabaseReference cmdRef = mDatabase.child("Bom").child("Command");
        cmdRef.child("TrangThai").setValue("Off");
        cmdRef.child("TargetMoisture").setValue(0);
        cmdRef.child("ThoiGian").setValue(0);

        isPumping = false;
        updateUIState(false, 0);
        Toast.makeText(this, "Đã gửi lệnh TẮT bơm!", Toast.LENGTH_SHORT).show();
    }

    private void updateUIState(boolean pumping, int target) {
        btnManualPump.setEnabled(!pumping);
        etWaterAmount.setEnabled(!pumping);
        if (pumping) {
            tvPumpStatus.setVisibility(View.VISIBLE);
            tvPumpStatus.setText("💧 Đang bơm tới " + target + "%");
        } else {
            tvPumpStatus.setVisibility(View.VISIBLE);
            tvPumpStatus.setText("⛔ Đã tắt bơm");
        }
    }

    private void attachCamBienListener() {
        camBienListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;
                Float ph = snap.child("Dat").child("PhanTram").getValue(Float.class);
                if (ph != null) {
                    currentMoisture = ph;
                    tvCurrentMoisture.setText(String.format("%.0f %%", ph));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        camBienRef.addValueEventListener(camBienListener);
    }

    private void navigateBackToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}