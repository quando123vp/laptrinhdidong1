package com.example.laptrinhdidong1;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
    private DatabaseReference mDatabase;
    private DatabaseReference camBienRef;
    private DatabaseReference camActuatorPumpRef;

    private float currentMoisture = 0f;
    private boolean isPumping = false;

    private com.google.firebase.database.ValueEventListener camBienListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pump_setting);

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
        camActuatorPumpRef = mDatabase.child("CamActuator").child("Bom");

        attachCamBienListener();

        // CLICK SỰ KIỆN
        btnManualPump.setOnClickListener(v -> startManualPump());
        btnStopPump.setOnClickListener(v -> stopManualPump());
        btnAddTime.setOnClickListener(v -> showTimePickerDialog());
        tvClearAll.setOnClickListener(v -> llScheduledTimesContainer.removeAllViews());
        btnBackPump.setOnClickListener(v -> navigateBackToMain());

        // BACK GESTURE
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
        handler.removeCallbacksAndMessages(null);
    }

    // ==========================================================
    // 💧 GỬI LỆNH BƠM THỦ CÔNG (ON)
    // ==========================================================
    private void startManualPump() {

        if (isPumping) {
            Toast.makeText(this, "Đang bơm. Vui lòng chờ...", Toast.LENGTH_SHORT).show();
            return;
        }

        String targetStr = etWaterAmount.getText().toString().trim();
        if (targetStr.isEmpty()) {
            Toast.makeText(this, "⚠️ Nhập độ ẩm mục tiêu!", Toast.LENGTH_SHORT).show();
            return;
        }

        int target;
        try {
            target = Integer.parseInt(targetStr);
        } catch (Exception e) {
            Toast.makeText(this, "⚠️ Giá trị không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (target < 0) target = 0;
        if (target > 100) target = 100;

        if (target <= currentMoisture) {
            Toast.makeText(this, "Độ ẩm hiện tại đã >= mục tiêu!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference cmdRef = mDatabase.child("Bom").child("Command");
        cmdRef.child("TrangThai").setValue("On");
        cmdRef.child("TargetMoisture").setValue(target);
        cmdRef.child("ThoiGian").setValue(0);

        isPumping = true;
        btnManualPump.setEnabled(false);
        etWaterAmount.setEnabled(false);

        tvPumpStatus.setVisibility(View.VISIBLE);
        tvPumpStatus.setText("💧 Đang bơm tới " + target + "%");
    }

    // ==========================================================
    // ❌ GỬI LỆNH TẮT BƠM (OFF)
    // ==========================================================
    private void stopManualPump() {

        DatabaseReference cmdRef = mDatabase.child("Bom").child("Command");
        cmdRef.child("TrangThai").setValue("Off");
        cmdRef.child("TargetMoisture").setValue(0);
        cmdRef.child("ThoiGian").setValue(0);

        isPumping = false;

        btnManualPump.setEnabled(true);
        etWaterAmount.setEnabled(true);

        tvPumpStatus.setVisibility(View.VISIBLE);
        tvPumpStatus.setText("⛔ Đã tắt bơm");

        Toast.makeText(this, "Đã gửi lệnh TẮT bơm!", Toast.LENGTH_SHORT).show();
    }

    // ==========================================================
    // ⏰ Chọn giờ bơm tự động
    // ==========================================================
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
                    if (which == 2) {
                        showDayPickerDialog(selectedTime);
                    } else {
                        addTimeRow(selectedTime + " (" + repeatOptions[which] + ")");
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDayPickerDialog(String selectedTime) {

        String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        boolean[] checkedDays = new boolean[days.length];

        new AlertDialog.Builder(this)
                .setTitle("Chọn ngày")
                .setMultiChoiceItems(days, checkedDays, (dialog, which, isChecked) -> checkedDays[which] = isChecked)
                .setPositiveButton("OK", (dialog, which) -> {
                    StringBuilder selected = new StringBuilder();
                    for (int i = 0; i < checkedDays.length; i++) {
                        if (checkedDays[i]) {
                            if (selected.length() > 0) selected.append(", ");
                            selected.append(days[i]);
                        }
                    }
                    addTimeRow(selectedTime + " (" + selected + ")");
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addTimeRow(String time) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 10, 0, 10);

        ImageView icon = new ImageView(this);
        icon.setImageResource(android.R.drawable.ic_lock_idle_alarm);
        icon.setLayoutParams(new LinearLayout.LayoutParams(60, 60));

        TextView tv = new TextView(this);
        tv.setText(time);
        tv.setTextSize(16);
        tv.setPadding(16, 0, 0, 0);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView delete = new TextView(this);
        delete.setText("❌");
        delete.setTextSize(20);
        delete.setPadding(16, 0, 0, 0);
        delete.setOnClickListener(v -> {
            llScheduledTimesContainer.removeView(row);
            Toast.makeText(this, "Đã xóa " + time, Toast.LENGTH_SHORT).show();
        });

        row.addView(icon);
        row.addView(tv);
        row.addView(delete);

        llScheduledTimesContainer.addView(row);
    }

    // ==========================================================
    // Lắng nghe dữ liệu cảm biến
    // ==========================================================
    private void attachCamBienListener() {

        camBienListener = new com.google.firebase.database.ValueEventListener() {
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

    // ==========================================================
    // Trở về MainActivity
    // ==========================================================
    private void navigateBackToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
