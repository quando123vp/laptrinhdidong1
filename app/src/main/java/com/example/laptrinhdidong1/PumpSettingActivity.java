package com.example.laptrinhdidong1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private DatabaseReference commandRef;

    private boolean isPumping = false;
    private ValueEventListener camBienListener;
    private ValueEventListener pumpStatusListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pump_setting);

        // 1. Xin quyền Thông báo (Notification) cho Android 13+
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // 2. Xin quyền Báo thức chính xác (Alarm) cho Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        initViews();
        setupFirebase();
        setupEvents();
    }

    private void initViews() {
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
    }

    private void setupFirebase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        camBienRef = mDatabase.child("CamBien");
        commandRef = mDatabase.child("Bom").child("Command");

        attachCamBienListener();
        attachPumpStatusListener();
    }

    private void setupEvents() {
        btnManualPump.setOnClickListener(v -> startManualPump());
        btnStopPump.setOnClickListener(v -> stopManualPump());
        btnAddTime.setOnClickListener(v -> showTimePickerDialog());

        tvClearAll.setOnClickListener(v -> {
            llScheduledTimesContainer.removeAllViews();
            Toast.makeText(this, "Đã xóa giao diện", Toast.LENGTH_SHORT).show();
        });

        btnBackPump.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                btnBackPump.performClick();
            }
        });
    }

    // ==========================================================
    // QUY TRÌNH CÀI ĐẶT 3 BƯỚC: GIỜ -> LẶP -> ĐỘ ẨM
    // ==========================================================

    // Bước 1: Chọn Giờ
    private void showTimePickerDialog() {
        Calendar c = Calendar.getInstance();
        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    showRepeatDialog(selectedTime, hourOfDay, minute);
                },
                nowHour, nowMinute, true
        );
        timePickerDialog.setTitle("Chọn giờ bơm");
        timePickerDialog.show();
    }

    // Bước 2: Chọn Lặp lại
    private void showRepeatDialog(String selectedTime, int hour, int minute) {
        String[] repeatOptions = {"Một lần", "Mỗi ngày"};

        new AlertDialog.Builder(this)
                .setTitle("Lặp lại lịch bơm")
                .setItems(repeatOptions, (dialog, which) -> {
                    boolean isOneTime = (which == 0);
                    String repeatText = repeatOptions[which];
                    showMoistureInputDialog(selectedTime, repeatText, hour, minute, isOneTime);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Bước 3: Nhập Độ ẩm
    private void showMoistureInputDialog(String timeText, String repeatText, int hour, int minute, boolean isOneTime) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Ví dụ: 70");
        input.setGravity(Gravity.CENTER);

        new AlertDialog.Builder(this)
                .setTitle("Độ ẩm mục tiêu (%)")
                .setMessage("Nhập độ ẩm đất muốn đạt được khi bơm:")
                .setView(input)
                .setPositiveButton("Hoàn tất", (dialog, which) -> {
                    String val = input.getText().toString().trim();
                    int targetMoisture = 70; // Mặc định
                    if (!val.isEmpty()) {
                        try {
                            targetMoisture = Integer.parseInt(val);
                            if (targetMoisture > 100) targetMoisture = 100;
                            if (targetMoisture < 0) targetMoisture = 0;
                        } catch (Exception e) {}
                    }

                    // Gài báo thức hệ thống
                    setSystemAlarm(hour, minute, isOneTime, targetMoisture);

                    // Hiển thị lên màn hình
                    String displayText = timeText + " (" + repeatText + ") - Tới " + targetMoisture + "%";
                    addTimeRow(displayText, hour, minute);

                    Toast.makeText(this, "Đã hẹn giờ: " + timeText, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ==========================================================
    // LOGIC ALARM MANAGER
    // ==========================================================

    @SuppressLint("ScheduleExactAlarm")
    private void setSystemAlarm(int hour, int minute, boolean isOneTime, int targetMoisture) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, PumpAlarmReceiver.class);

        int requestCode = hour * 60 + minute;
        intent.putExtra("IS_ONE_TIME", isOneTime);
        intent.putExtra("REQUEST_CODE", requestCode);
        intent.putExtra("TARGET_MOISTURE", targetMoisture);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

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

    // ==========================================================
    // UI MANAGEMENT
    // ==========================================================

    private void addTimeRow(String displayText, int hour, int minute) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 10, 0, 10);

        int tagId = hour * 60 + minute;
        row.setTag(tagId);

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
        delete.setOnClickListener(v -> {
            llScheduledTimesContainer.removeView(row);
            cancelSpecificAlarm(hour, minute);
            Toast.makeText(this, "Đã hủy lịch", Toast.LENGTH_SHORT).show();
        });

        row.addView(icon);
        row.addView(tv);
        row.addView(delete);

        llScheduledTimesContainer.addView(row);
    }

    private void removeViewByTag(int tagId) {
        View viewToRemove = llScheduledTimesContainer.findViewWithTag(tagId);
        if (viewToRemove != null) {
            llScheduledTimesContainer.removeView(viewToRemove);
        }
    }

    // ==========================================================
    // FIREBASE LISTENERS & AUTO UPDATE
    // ==========================================================

    private void attachPumpStatusListener() {
        pumpStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.child("TrangThai").getValue(String.class);

                if ("On".equalsIgnoreCase(status)) {
                    isPumping = true;
                    tvPumpStatus.setVisibility(View.VISIBLE);
                    tvPumpStatus.setText("🌊 ĐANG BƠM TỰ ĐỘNG...");
                    tvPumpStatus.setTextColor(0xFF0000FF);
                    updateUIState(true);
                } else {
                    isPumping = false;
                    tvPumpStatus.setVisibility(View.GONE);
                    updateUIState(false);
                    checkAndRemoveFinishedAlarm();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        commandRef.addValueEventListener(pumpStatusListener);
    }

    private void checkAndRemoveFinishedAlarm() {
        SharedPreferences prefs = getSharedPreferences("PumpPrefs", MODE_PRIVATE);
        int pendingDeleteId = prefs.getInt("PENDING_DELETE_ID", -1);

        if (pendingDeleteId != -1) {
            removeViewByTag(pendingDeleteId);
            prefs.edit().remove("PENDING_DELETE_ID").apply();
            Toast.makeText(PumpSettingActivity.this, "✅ Đã bơm xong và xóa lịch!", Toast.LENGTH_LONG).show();
        }
    }

    private void attachCamBienListener() {
        camBienListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) {
                    Float ph = snap.child("Dat").child("PhanTram").getValue(Float.class);
                    if (ph != null) {
                        tvCurrentMoisture.setText(String.format("%.0f %%", ph));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        camBienRef.addValueEventListener(camBienListener);
    }

    // ==========================================================
    // MANUAL PUMP CONTROL
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

        DatabaseReference cmdRef = mDatabase.child("Bom").child("Command");
        cmdRef.child("TrangThai").setValue("On");
        cmdRef.child("TargetMoisture").setValue(target);
        cmdRef.child("ThoiGian").setValue(0);
    }

    private void stopManualPump() {
        DatabaseReference cmdRef = mDatabase.child("Bom").child("Command");
        cmdRef.child("TrangThai").setValue("Off");
        cmdRef.child("TargetMoisture").setValue(0);
        cmdRef.child("ThoiGian").setValue(0);
    }

    private void updateUIState(boolean isRunning) {
        btnManualPump.setEnabled(!isRunning);
        etWaterAmount.setEnabled(!isRunning);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commandRef != null && pumpStatusListener != null) {
            commandRef.removeEventListener(pumpStatusListener);
        }
        if (camBienRef != null && camBienListener != null) {
            camBienRef.removeEventListener(camBienListener);
        }
    }
}