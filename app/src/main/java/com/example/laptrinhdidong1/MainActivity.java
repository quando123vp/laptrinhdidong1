package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference userRef;
    private String userId;

    // Views
    private ImageView btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFirebase();
        initViews();

        loadUserAvatarListener();
        loadSensorValues();
        initNavigationCards();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = user.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
    }

    private void initViews() {
        btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserAvatarOnce();
    }

    /* ===================== AVATAR ======================= */

    private void loadUserAvatarListener() {
        userRef.child("avatarLocalPath").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setAvatar(snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadUserAvatarOnce() {
        userRef.child("avatarLocalPath").get()
                .addOnSuccessListener(snapshot -> setAvatar(snapshot.getValue(String.class)));
    }

    private void setAvatar(String path) {
        if (path == null || path.isEmpty()) {
            btnSettings.setImageResource(R.drawable.ic_user_default);
            return;
        }

        File f = new File(path);
        if (!f.exists()) {
            btnSettings.setImageResource(R.drawable.ic_user_default);
            return;
        }

        Glide.with(this)
                .load(Uri.fromFile(f))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .signature(new ObjectKey(f.lastModified()))
                .circleCrop()
                .into(btnSettings);
    }

    /* ===================== SENSOR LISTENER ======================= */

    private void loadSensorValues() {
        mDatabase.child("CamBien").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                updateTemperature(snapshot);
                updateHumidity(snapshot);
                updateLight(snapshot);
                updateSoil(snapshot);
                updateRain(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    /* ---------- Temperature ---------- */
    // ⭐ FIX CHUẨN NHẤT — khớp đúng XML: tv_temperature
    private void updateTemperature(DataSnapshot snapshot) {
        TextView tv = findViewById(R.id.tv_temperature);
        if (tv == null) return;  // chống crash 100%

        Object raw = snapshot.child("NhietDo").getValue();
        if (raw == null) {
            tv.setText("--°C");
            return;
        }

        try {
            double temp = Double.parseDouble(raw.toString().trim());
            tv.setText(temp + "°C");
        } catch (Exception e) {
            tv.setText("--°C");
        }
    }

    /* ---------- Humidity ---------- */
    private void updateHumidity(DataSnapshot snapshot) {
        TextView tv = findViewById(R.id.tv_humidity);
        if (tv == null) return;

        Double hum = toDouble(snapshot.child("DoAm").getValue());
        tv.setText(hum != null ? hum + "%" : "--%");
    }

    /* ---------- Light ---------- */
    private void updateLight(DataSnapshot snapshot) {
        TextView tv = findViewById(R.id.tv_light_value);
        if (tv == null) return;

        Object val = snapshot.child("AnhSang").child("TrangThai").getValue();
        tv.setText(val != null ? val.toString() : "--");
    }

    /* ---------- Soil ---------- */
    private void updateSoil(DataSnapshot snapshot) {
        TextView tv = findViewById(R.id.tv_soil_value);
        if (tv == null) return;

        Double soil = toDouble(snapshot.child("Dat").child("PhanTram").getValue());
        tv.setText(soil != null ? soil + "%" : "--%");
    }

    /* ---------- Rain ---------- */
    private void updateRain(DataSnapshot snapshot) {
        TextView tv = findViewById(R.id.tv_rain_value);
        if (tv == null) return;

        Object val = snapshot.child("Mua").child("TrangThai").getValue();
        tv.setText(val != null ? val.toString() : "--");
    }

    /* ===================== NAVIGATION ======================= */
    private void initNavigationCards() {

        findViewById(R.id.card_soil).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SoilHistoryActivity.class)));

        findViewById(R.id.card_temp_humid).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TempHumidHistoryActivity.class)));

        findViewById(R.id.card_light).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LightHistoryActivity.class)));

        findViewById(R.id.card_rain).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RainHistoryActivity.class)));

        findViewById(R.id.card_pump).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PumpSettingActivity.class)));

        findViewById(R.id.card_light_control).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LightSettingActivity.class)));
    }

    /* ===================== HELPERS ======================= */
    private Double toDouble(Object v) {
        try {
            if (v instanceof Long) return ((Long) v).doubleValue();
            if (v instanceof Double) return (Double) v;
            if (v instanceof String) return Double.parseDouble(((String) v).trim());
        } catch (Exception ignored) { }
        return null;
    }
}
