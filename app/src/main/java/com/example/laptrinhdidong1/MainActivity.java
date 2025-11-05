package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String userId;

    // 🔹 Giao diện cảm biến
    private TextView tvSoilMoisture, tvTempHumid, tvLightIntensity, tvRainStatus;
    private CardView cardSoil, cardTempHumid, cardLight, cardRain;
    private CardView cardPump, cardLightControl, cardRoof;
    private ImageView btnSettings;

    // ⚙️ Menu cài đặt
    private RelativeLayout settingsMenuPanel;
    private View dimBackground;
    private View menuView;
    private boolean isMenuOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // 🔹 Ánh xạ view
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
        btnSettings = findViewById(R.id.btn_settings);

        // 🧠 Load avatar người dùng
        loadUserAvatar();
        userRef.child("avatarLocalPath").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) { loadUserAvatar(); }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        setupSettingsMenu();
        setupSensorDataListener();
        setupNavigationCards();
    }

    /** 🧠 Load avatar user từ Firebase/local path */
    private void loadUserAvatar() {
        userRef.child("avatarLocalPath").get().addOnSuccessListener(snapshot -> {
            String localPath = snapshot.getValue(String.class);
            if (localPath != null && !localPath.isEmpty()) {
                File file = new File(localPath);
                if (file.exists()) {
                    Glide.with(this)
                            .load(Uri.fromFile(file))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .circleCrop()
                            .placeholder(R.drawable.ic_user_default)
                            .error(R.drawable.ic_user_default)
                            .into(btnSettings);
                } else btnSettings.setImageResource(R.drawable.ic_user_default);
            } else btnSettings.setImageResource(R.drawable.ic_user_default);
        });
    }

    /** 📡 Theo dõi dữ liệu cảm biến */
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
                Log.w(TAG, "Firebase Error:", error.toException());
            }
        });
    }

    /** 🧭 Điều hướng sang các trang chi tiết */
    private void setupNavigationCards() {
        cardSoil.setOnClickListener(v -> startActivity(new Intent(this, SoilHistoryActivity.class)));
        cardTempHumid.setOnClickListener(v -> startActivity(new Intent(this, TempHumidHistoryActivity.class)));
        cardLight.setOnClickListener(v -> startActivity(new Intent(this, LightHistoryActivity.class)));
        cardRain.setOnClickListener(v -> startActivity(new Intent(this, RainHistoryActivity.class)));
        cardPump.setOnClickListener(v -> startActivity(new Intent(this, PumpSettingActivity.class)));
        cardLightControl.setOnClickListener(v -> startActivity(new Intent(this, LightSettingActivity.class)));
        cardRoof.setOnClickListener(v -> startActivity(new Intent(this, RoofSettingActivity.class)));
    }

    /** ⚙️ Menu cài đặt */
    private void setupSettingsMenu() {
        menuView = getLayoutInflater().inflate(R.layout.layout_settings_menu, null);
        addContentView(menuView, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        settingsMenuPanel = menuView.findViewById(R.id.settings_menu_panel);
        dimBackground = menuView.findViewById(R.id.view_dim_background);

        menuView.setVisibility(View.VISIBLE);
        dimBackground.setAlpha(0f);
        dimBackground.setVisibility(View.GONE);
        menuView.post(() -> settingsMenuPanel.setTranslationX(settingsMenuPanel.getWidth()));

        btnSettings.setOnClickListener(v -> toggleMenu());
        dimBackground.setOnClickListener(v -> {
            if (isMenuOpen) toggleMenu();
        });

        // Giới thiệu app
        menuView.findViewById(R.id.item_about).setOnClickListener(v -> {
            toggleMenu();
            Snackbar.make(findViewById(android.R.id.content),
                    "Ứng dụng được phát triển bởi Nhóm 3 - Dự án Nhà Thông Minh",
                    Snackbar.LENGTH_SHORT).show();
        });

        // Hồ sơ người dùng
        menuView.findViewById(R.id.item_account).setOnClickListener(v -> {
            toggleMenu();
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    /** 🎬 Hiệu ứng trượt menu */
    private void toggleMenu() {
        float menuWidth = settingsMenuPanel.getWidth();

        if (isMenuOpen) {
            // Đóng
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
            // Mở
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
