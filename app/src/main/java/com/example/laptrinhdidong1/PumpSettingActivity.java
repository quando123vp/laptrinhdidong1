package com.example.laptrinhdidong1;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.*;

import java.util.Locale;

public class PumpSettingActivity extends AppCompatActivity {

    private static final String TAG = "PumpSettingActivity";

    private MaterialButton btnAddTime, btnManualPump, btnStopPump;
    private LinearLayout llScheduledTimesContainer;
    private TextView tvClearAll, tvPumpStatus, tvCurrentMoisture;
    private EditText etWaterAmount;
    private ImageView btnBackPump;

    private Handler handler = new Handler();

    // Firebase
    private DatabaseReference dbRoot, camBienRef, actuatorPumpRef;

    private float currentMoisture = 0f;
    private boolean isPumping = false;

    private ValueEventListener camBienListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pump_setting);

        // ===== ÁNH XẠ VIEW =====
        btnAddTime = findViewById(R.id.btn_add_time);
        llScheduledTimesContainer = findViewById(R.id.ll_scheduled_times_container);
        tvClearAll = findViewById(R.id.tv_clear_all);
        btnManualPump = findViewById(R.id.btn_manual_pump);
        btnStopPump = findViewById(R.id.btn_stop_pump);
        etWaterAmount = findViewById(R.id.et_water_amount);
        tvPumpStatus = findViewById(R.id.tv_pump_status);
        btnBackPump = findViewById(R.id.btnBackPump);
        tvCurrentMoisture = findViewById(R.id.tv_current_moisture);

        // ===== FIREBASE =====
        dbRoot = FirebaseDatabase.getInstance().getReference();
        camBienRef = dbRoot.child("CamBien");
        actuatorPumpRef = dbRoot.child("Bom").child("Command");

        // ===== SỰ KIỆN UI =====

        btnBackPump.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        btnAddTime.setOnClickListener(v -> showTimePickerDialog());
        tvClearAll.setOnClickListener(v -> {
            llScheduledTimesContainer.removeAllViews();
            Toast.makeText(this, "Đã xóa tất cả giờ bơm!", Toast.LENGTH_SHORT).show();
        });

        btnManualPump.setOnClickListener(v -> startManualPump());
        btnStopPump.setOnClickListener(v -> stopManualPump());

        attachCamBienListener();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(PumpSettingActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camBienRef != null && camBienListener != null) {
            camBienRef.removeEventListener(camBienListener);
        }
        handler.removeCallbacksAndMessages(null);
    }

    // ==========================================================
    // 🔥 BẬT BƠM THỦ CÔNG — ĐÃ FIX TARGET GIỚI HẠN 0–100
    // ==========================================================
    private void startManualPump() {

        if (isPumping) {
            Toast.makeText(this, "Đang bơm rồi!", Toast.LENGTH_SHORT).show();
            return;
        }

        String targetStr = etWaterAmount.getText().toString().trim();
        if (targetStr.isEmpty()) {
            Toast.makeText(this, "Nhập độ ẩm mục tiêu (0 - 100%)", Toast.LENGTH_SHORT).show();
            return;
        }

        int target;
        try {
            target = Integer.parseInt(targetStr);
        } catch (Exception e) {
            Toast.makeText(this, "Giá trị không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ===== GIỚI HẠN 0–100 =====
        if (target > 100) {
            Toast.makeText(this, "Độ ẩm tối đa là 100%. Đã chỉnh về 100%", Toast.LENGTH_SHORT).show();
            target = 100;
        }

        if (target < 0) {
            Toast.makeText(this, "Độ ẩm nhỏ nhất là 0%", Toast.LENGTH_SHORT).show();
            target = 0;
        }

        // ===== KIỂM TRA ĐỘ ẨM HIỆN TẠI =====
        if (target <= currentMoisture) {
            Toast.makeText(this, "Độ ẩm hiện tại đã cao hơn mục tiêu!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ===== GỬI FIREBASE =====
        actuatorPumpRef.child("TrangThai").setValue("On");
        actuatorPumpRef.child("TargetMoisture").setValue(target);
        actuatorPumpRef.child("ThoiGian").setValue(0);

        isPumping = true;
        tvPumpStatus.setVisibility(TextView.VISIBLE);
        tvPumpStatus.setText("💧 Đang bơm... mục tiêu: " + target + "%");

        btnManualPump.setEnabled(false);
        btnStopPump.setEnabled(true);
        etWaterAmount.setEnabled(false);

        Toast.makeText(this, "Đã gửi lệnh bơm tới ESP32", Toast.LENGTH_SHORT).show();
    }

    // ==========================================================
    // ❌ TẮT BƠM
    // ==========================================================
    private void stopManualPump() {
        actuatorPumpRef.child("TrangThai").setValue("Off");

        isPumping = false;
        tvPumpStatus.setVisibility(TextView.VISIBLE);
        tvPumpStatus.setText("⛔ Bơm đã dừng");

        btnManualPump.setEnabled(true);
        btnStopPump.setEnabled(false);
        etWaterAmount.setEnabled(true);

        Toast.makeText(this, "Đã gửi lệnh TẮT bơm tới ESP32", Toast.LENGTH_SHORT).show();
    }

    // ==========================================================
    // 🔁 THEO DÕI CAMBIEN / RESET UI KHI ESP TẮT BƠM
    // ==========================================================
    private void attachCamBienListener() {
        camBienListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Float soil = snapshot.child("Dat").child("PhanTram").getValue(Float.class);
                    if (soil == null) soil = snapshot.child("DoAmDat").getValue(Float.class);
                    if (soil == null) soil = 0f;

                    currentMoisture = Math.max(0, Math.min(100, soil));

                    tvCurrentMoisture.setText(String.format(Locale.getDefault(), "%.0f %%", currentMoisture));

                    // Kiểm tra Pump từ ESP
                    dbRoot.child("Bom").child("TrangThai").get()
                            .addOnSuccessListener(snap -> {
                                String val = snap.getValue(String.class);
                                if (val != null && val.equalsIgnoreCase("Off")) {
                                    isPumping = false;
                                    btnManualPump.setEnabled(true);
                                    btnStopPump.setEnabled(false);
                                    etWaterAmount.setEnabled(true);
                                    tvPumpStatus.setVisibility(TextView.GONE);
                                }
                            });

                } catch (Exception e) {
                    Log.e(TAG, "onDataChange: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Firebase cancelled", error.toException());
            }
        };

        camBienRef.addValueEventListener(camBienListener);
    }

    // ==========================================================
    // 🕒 LỊCH BƠM (UI)
    // ==========================================================
    private void showTimePickerDialog() {
        TimePickerDialog picker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            addTimeRow(time);
        }, 8, 0, true);

        picker.setTitle("Chọn giờ bơm");
        picker.show();
    }

    private void addTimeRow(String time) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 8, 0, 8);

        ImageView icon = new ImageView(this);
        icon.setImageResource(android.R.drawable.ic_lock_idle_alarm);
        icon.setLayoutParams(new LinearLayout.LayoutParams(60, 60));

        TextView tvTime = new TextView(this);
        tvTime.setText(time);
        tvTime.setTextSize(16);
        tvTime.setTextColor(getResources().getColor(android.R.color.black));
        tvTime.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView delete = new TextView(this);
        delete.setText("❌");
        delete.setTextSize(18);
        delete.setPadding(16, 0, 0, 0);
        delete.setOnClickListener(v -> {
            llScheduledTimesContainer.removeView(row);
            Toast.makeText(this, "Đã xóa " + time, Toast.LENGTH_SHORT).show();
        });

        row.addView(icon);
        row.addView(tvTime);
        row.addView(delete);
        llScheduledTimesContainer.addView(row);
    }
}
