package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DatabaseReference mDatabase;

    // Firebase user info
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String userId;

    // Sensors
    private TextView tvSoilMoisture, tvTempHumid, tvLightIntensity, tvRainStatus;
    private CardView cardSoil, cardTempHumid, cardLight, cardRain;
    private CardView cardPump, cardLightControl, cardRoof;

    // 🖼️ Avatar (thay icon settings)
    private ImageView btnSettings; // Giữ nguyên tên để không phải đổi nhiều
    private LinearLayout settingsMenuPanel;
    private View dimBackground;
    private boolean isMenuOpen = false;
    private View menuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase setup
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Ánh xạ cảm biến
        tvSoilMoisture = findViewById(R.id.tv_soil_moisture);
        tvTempHumid = findViewById(R.id.tv_temp_humid);
        tvLightIntensity = findViewById(R.id.tv_light_intensity);
        tvRainStatus = findViewById(R.id.tv_rain_status);

        cardSoil = findViewById(R.id.card_soil);
        cardTempHumid = findViewById(R.id.card_temp_humid);
        cardLight = findViewById(R.id.card_light_sensor);
        cardRain = findViewById(R.id.card_rain);

        cardPump = findViewById(R.id.card_pump);
        cardLightControl = findViewById(R.id.card_light);
        cardRoof = findViewById(R.id.card_roof);

        // ⚙️ Avatar người dùng (thay cho nút setting)
        btnSettings = findViewById(R.id.btn_settings);

        // 🧠 Load avatar và lắng nghe thay đổi
        loadUserAvatar();
        userRef.child("avatarLocalPath").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadUserAvatar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Menu setup
        setupSettingsMenu();
        setupSensorDataListener();
        setupNavigationCards();
    }

    /** 🧠 Load ảnh đại diện user thay cho icon setting */
    private void loadUserAvatar() {
        userRef.child("avatarLocalPath").get().addOnSuccessListener(snapshot -> {
            String localPath = snapshot.getValue(String.class);
            if (localPath != null && !localPath.isEmpty()) {
                File file = new File(localPath);
                if (file.exists()) {
                    Glide.with(MainActivity.this)
                            .load(Uri.fromFile(file))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .circleCrop()
                            .placeholder(R.drawable.ic_user_default)
                            .error(R.drawable.ic_user_default)
                            .into(btnSettings);
                } else {
                    btnSettings.setImageResource(R.drawable.ic_user_default);
                }
            } else {
                btnSettings.setImageResource(R.drawable.ic_user_default);
            }
        });
    }

    /** 📡 Lắng nghe dữ liệu cảm biến Firebase */
    private void setupSensorDataListener() {
        mDatabase.child("CamBien").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Float nhietDo = snapshot.child("NhietDo").getValue(Float.class);
                Float doAm = snapshot.child("DoAm").getValue(Float.class);
                if (nhietDo != null && doAm != null)
                    tvTempHumid.setText(String.format("%.1f°C | %.1f%%", nhietDo, doAm));

                DataSnapshot lightSnap = snapshot.child("AnhSang");
                if (lightSnap.exists()) {
                    String trangThai = lightSnap.child("TrangThai").getValue(String.class);
                    tvLightIntensity.setText(trangThai != null ? trangThai : "--");
                }

                Long doAmDat = snapshot.child("DoAmDat").getValue(Long.class);
                if (doAmDat != null)
                    tvSoilMoisture.setText(doAmDat + "%");

                String trangThaiMua = snapshot.child("TrangThaiMua").getValue(String.class);
                if (trangThaiMua != null)
                    tvRainStatus.setText(trangThaiMua);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Firebase Error: ", error.toException());
            }
        });
    }

    /** 🧭 Điều hướng các CardView */
    private void setupNavigationCards() {
        cardSoil.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SoilHistoryActivity.class)));
        cardTempHumid.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TempHumidHistoryActivity.class)));
        cardLight.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LightHistoryActivity.class)));
        cardRain.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RainHistoryActivity.class)));
        cardPump.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PumpSettingActivity.class)));
        cardLightControl.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LightSettingActivity.class)));
        cardRoof.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RoofSettingActivity.class)));
    }

    /** ⚙️ Thiết lập menu Setting (giữ nguyên chức năng cũ) */
    private void setupSettingsMenu() {
        // Gắn layout menu (layout_settings_menu.xml)
        menuView = getLayoutInflater().inflate(R.layout.layout_settings_menu, null);
        addContentView(menuView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        settingsMenuPanel = menuView.findViewById(R.id.settings_menu_panel);
        dimBackground = menuView.findViewById(R.id.view_dim_background);

        menuView.setVisibility(View.VISIBLE);
        dimBackground.setAlpha(0f);
        dimBackground.setVisibility(View.GONE);
        menuView.post(() -> settingsMenuPanel.setTranslationX(settingsMenuPanel.getWidth()));

        // ✅ Khi click avatar → mở menu (giống như click icon setting)
        btnSettings.setOnClickListener(v -> toggleMenu());
        dimBackground.setOnClickListener(v -> {
            if (isMenuOpen) toggleMenu();
        });

        // Các item trong menu
        menuView.findViewById(R.id.item_info).setOnClickListener(v -> {
            Log.d(TAG, "Thông tin được chọn");
            toggleMenu();
        });

        // ✅ Khi ấn vào “Giới thiệu” → hiện thông báo ở dưới màn hình
        menuView.findViewById(R.id.item_about).setOnClickListener(v -> {
            Log.d(TAG, "Giới thiệu được chọn");
            toggleMenu();

            Snackbar.make(findViewById(android.R.id.content),
                    "Ứng dụng được phát triển bởi nhóm 2 - DHKM16A1HN",
                    Snackbar.LENGTH_LONG).show();
        });

        menuView.findViewById(R.id.item_account).setOnClickListener(v -> {
            toggleMenu();
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });
    }

    /** 🎬 Hiệu ứng mở / đóng menu */
    private void toggleMenu() {
        float menuWidth = settingsMenuPanel.getWidth();

        if (isMenuOpen) {
            ObjectAnimator slideOut = ObjectAnimator.ofFloat(settingsMenuPanel, "translationX", 0f, menuWidth);
            slideOut.setDuration(300);
            slideOut.setInterpolator(new DecelerateInterpolator());
            slideOut.start();

            ValueAnimator fadeOut = ValueAnimator.ofFloat(1f, 0f);
            fadeOut.setDuration(300);
            fadeOut.addUpdateListener(a -> dimBackground.setAlpha((Float) a.getAnimatedValue()));
            fadeOut.start();

            dimBackground.postDelayed(() -> dimBackground.setVisibility(View.GONE), 300);
        } else {
            settingsMenuPanel.setVisibility(View.VISIBLE);
            dimBackground.setVisibility(View.VISIBLE);

            ObjectAnimator slideIn = ObjectAnimator.ofFloat(settingsMenuPanel, "translationX", menuWidth, 0f);
            slideIn.setDuration(300);
            slideIn.setInterpolator(new DecelerateInterpolator());
            slideIn.start();

            ValueAnimator fadeIn = ValueAnimator.ofFloat(0f, 1f);
            fadeIn.setDuration(300);
            fadeIn.addUpdateListener(a -> dimBackground.setAlpha((Float) a.getAnimatedValue()));
            fadeIn.start();
        }

        isMenuOpen = !isMenuOpen;
    }
}
