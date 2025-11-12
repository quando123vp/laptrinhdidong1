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

    private MaterialButton btnAddTime, btnManualPump;
    private LinearLayout llScheduledTimesContainer;
    private TextView tvClearAll, tvPumpStatus, tvCurrentMoisture;
    private EditText etWaterAmount;
    private ImageView btnBackPump;
    private Handler handler = new Handler();

    // Firebase
    private DatabaseReference mDatabase;
    private DatabaseReference camBienRef;
    private DatabaseReference camActuatorPumpRef;

    // Local state
    private float currentMoisture = 0f; // đọc từ Firebase
    private boolean isPumping = false;

    // Firebase listener ref (so we can remove onDestroy)
    private com.google.firebase.database.ValueEventListener camBienListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
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

            // If some views are missing, avoid NPEs by checking null before use later.
            if (tvPumpStatus != null) tvPumpStatus.setVisibility(View.GONE);

            // Firebase init
            mDatabase = FirebaseDatabase.getInstance().getReference();
            camBienRef = mDatabase.child("CamBien");
            camActuatorPumpRef = mDatabase.child("CamActuator").child("Bom");

            // Bắt listener để cập nhật độ ẩm giống MainActivity
            attachCamBienListener();

            // ➕ Thêm giờ bơm tự động
            if (btnAddTime != null) btnAddTime.setOnClickListener(v -> showTimePickerDialog());

            // 🗑️ Xóa tất cả lịch bơm
            if (tvClearAll != null) tvClearAll.setOnClickListener(v -> {
                if (llScheduledTimesContainer != null) llScheduledTimesContainer.removeAllViews();
                Toast.makeText(this, "Đã xóa tất cả giờ bơm!", Toast.LENGTH_SHORT).show();
            });

            // 💧 Bơm thủ công -> gửi lệnh lên Firebase thay vì chỉ mô phỏng client
            if (btnManualPump != null) btnManualPump.setOnClickListener(v -> startManualPump());

            // 🔙 Back button trong layout — dùng chung hành vi với back gesture
            if (btnBackPump != null) btnBackPump.setOnClickListener(v -> navigateBackToMain());

            // Back gesture & button: dùng OnBackPressedDispatcher (AndroidX)
            OnBackPressedCallback callback = new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    navigateBackToMain();
                }
            };
            getOnBackPressedDispatcher().addCallback(this, callback);
        } catch (Exception e) {
            // catch unexpected exceptions during onCreate to avoid crash
            Log.e(TAG, "onCreate error", e);
            Toast.makeText(this, "Lỗi khởi tạo màn hình bơm: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // remove Firebase listener to avoid leaks
        try {
            if (camBienRef != null && camBienListener != null) {
                camBienRef.removeEventListener(camBienListener);
            }
        } catch (Exception e) {
            Log.w(TAG, "remove listener failed", e);
        }
        handler.removeCallbacksAndMessages(null);
    }

    // =========================
    // 💧 BƠM THỦ CÔNG (gửi lệnh lên Firebase)
    // =========================
    private void startManualPump() {
        if (isPumping) {
            Toast.makeText(this, "Đang bơm. Vui lòng chờ hoàn tất.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (etWaterAmount == null) {
            Toast.makeText(this, "Không tìm thấy ô nhập. Thử lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        String targetStr = etWaterAmount.getText().toString().trim();
        if (targetStr.isEmpty()) {
            Toast.makeText(this, "⚠️ Nhập độ ẩm mục tiêu (0 - 100)!", Toast.LENGTH_SHORT).show();
            return;
        }

        int target;
        try {
            String digitsOnly = targetStr.replaceAll("[^0-9\\-]", "");
            target = Integer.parseInt(digitsOnly);
        } catch (Exception e) {
            Toast.makeText(this, "⚠️ Giá trị không hợp lệ. Vui lòng nhập số 0-100.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clamp target vào [0, 100]
        if (target < 0) target = 0;
        if (target > 100) target = 100;

        // Nếu mục tiêu <= hiện tại -> không cần bơm
        if (target <= Math.round(currentMoisture)) {
            Toast.makeText(this, "✅ Độ ẩm hiện tại đã bằng hoặc cao hơn mục tiêu.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gửi lệnh lên Firebase: Bom/Command/TrangThai = "On", Bom/Command/TargetMoisture = target
        try {
            DatabaseReference cmdRef = mDatabase.child("Bom").child("Command");
            cmdRef.child("TrangThai").setValue("On");
            cmdRef.child("TargetMoisture").setValue(target);
            cmdRef.child("ThoiGian").setValue(0);
            Toast.makeText(this, "Gửi lệnh bơm tới hệ thống. Máy bơm sẽ hoạt động đến " + target + "%", Toast.LENGTH_SHORT).show();

            // Cập nhật UI tạm thời để người dùng thấy phản hồi
            isPumping = true;
            if (btnManualPump != null) btnManualPump.setEnabled(false);
            if (etWaterAmount != null) etWaterAmount.setEnabled(false);
            if (tvPumpStatus != null) {
                tvPumpStatus.setVisibility(View.VISIBLE);
                tvPumpStatus.setText("💧 Lệnh gửi: bơm tới " + target + "%");
            }
        } catch (Exception e) {
            Log.e(TAG, "send command error", e);
            Toast.makeText(this, "❌ Lỗi gửi lệnh bơm: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // =========================
    // ⏰ BƠM TỰ ĐỘNG UI (lịch) - KHÔNG thay đổi
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
            if (llScheduledTimesContainer != null) llScheduledTimesContainer.removeView(row);
            Toast.makeText(this, "Đã xóa " + time, Toast.LENGTH_SHORT).show();
        });

        row.addView(clockIcon);
        row.addView(tvTime);
        row.addView(tvDelete);
        if (llScheduledTimesContainer != null) llScheduledTimesContainer.addView(row);
    }

    // =========================
    // Firebase: đọc CamBien giống MainActivity (an toàn)
    // =========================
    private void attachCamBienListener() {
        camBienListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot == null || !snapshot.exists()) {
                        if (tvCurrentMoisture != null) tvCurrentMoisture.setText("-- %");
                        return;
                    }

                    Float soilPerc = null;

                    DataSnapshot datSnap = snapshot.child("Dat");
                    if (datSnap.exists()) {
                        // 1) ưu tiên PhanTram
                        Object phObj = datSnap.child("PhanTram").getValue();
                        Double ph = tryParseDouble(phObj);
                        if (ph != null) {
                            soilPerc = ph.floatValue();
                        } else {
                            // 2) thử Analog
                            Object analogObj = datSnap.child("Analog").getValue();
                            Double analog = tryParseDouble(analogObj);
                            if (analog != null) {
                                soilPerc = (float) ((analog / 4095.0) * 100.0);
                            }
                        }
                    }

                    // 3) fallback: có thể CamBien/DoAmDat (legacy)
                    if (soilPerc == null) {
                        Object rootSoilObj = snapshot.child("DoAmDat").getValue();
                        Double rootSoil = tryParseDouble(rootSoilObj);
                        if (rootSoil != null) soilPerc = rootSoil.floatValue();
                    }

                    if (soilPerc != null) {
                        if (soilPerc < 0) soilPerc = 0f;
                        if (soilPerc > 100) soilPerc = 100f;
                        currentMoisture = soilPerc;
                        if (tvCurrentMoisture != null)
                            tvCurrentMoisture.setText(String.format(Locale.getDefault(), "%.0f %%", currentMoisture));
                    } else {
                        if (tvCurrentMoisture != null) tvCurrentMoisture.setText("-- %");
                    }

                    // Check pump state from CamActuator/Bom/TrangThai asynchronously.
                    camActuatorPumpRef.child("TrangThai").get().addOnSuccessListener(dataSnapshot -> {
                        try {
                            String val = dataSnapshot.getValue(String.class);
                            if (val != null && val.equalsIgnoreCase("Off")) {
                                if (isPumping) {
                                    isPumping = false;
                                    if (btnManualPump != null) btnManualPump.setEnabled(true);
                                    if (etWaterAmount != null) etWaterAmount.setEnabled(true);
                                    if (tvPumpStatus != null) tvPumpStatus.setVisibility(View.GONE);
                                }
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error processing pump state success", e);
                        }
                    }).addOnFailureListener(e -> {
                        // ignore failure to read pump state
                        Log.w(TAG, "Failed to read CamActuator/Bom/TrangThai", e);
                    });

                } catch (Exception ex) {
                    Log.e(TAG, "onDataChange handling error", ex);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "camBien listener cancelled", error.toException());
                if (tvCurrentMoisture != null) tvCurrentMoisture.setText("-- %");
            }
        };

        if (camBienRef != null) camBienRef.addValueEventListener(camBienListener);
    }

    private Double tryParseDouble(Object o) {
        if (o == null) return null;
        try {
            if (o instanceof Double) return (Double) o;
            if (o instanceof Long) return ((Long) o).doubleValue();
            if (o instanceof Integer) return ((Integer) o).doubleValue();
            if (o instanceof Float) return ((Float) o).doubleValue();
            if (o instanceof String) return Double.parseDouble((String) o);
        } catch (Exception ignored) {}
        return null;
    }

    // Common navigate back method used by both back button and gesture
    private void navigateBackToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
