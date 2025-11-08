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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
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

    private TextView tvSoilMoisture, tvTempHumid, tvHumidity, tvLightIntensity, tvRainStatus;
    private CardView cardSoil, cardTempHumid, cardLight, cardRain;
    private CardView cardPump, cardLightControl, cardRoof;
    private ImageView btnSettings;

    private RelativeLayout settingsMenuPanel;
    private View dimBackground;
    private View menuView;
    private boolean isMenuOpen = false;

    // giữ tham chiếu listener để có thể remove nếu cần
    private ValueEventListener avatarListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Nếu chưa login -> chuyển về LoginActivity
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // findViewById
        tvSoilMoisture = findViewById(R.id.tv_soil_moisture);
        tvTempHumid = findViewById(R.id.tv_temp_humid);
        tvHumidity = findViewById(R.id.tv_humidity);
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

        // Sử dụng ValueEventListener trực tiếp (thay cho .get())
        setupAvatarListener();

        setupSettingsMenu();
        setupSensorDataListener();
        setupNavigationCards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // fallback: reload avatar khi activity trở lại foreground
        Log.d(TAG, "onResume - reload avatar (fallback)");
        // nếu listener đã đăng ký thì không cần gọi thêm; nhưng gọi load để an toàn
        loadUserAvatarOnce();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // remove listener nếu có để tránh leak
        if (userRef != null && avatarListener != null) {
            userRef.child("avatarLocalPath").removeEventListener(avatarListener);
        }
    }

    /**
     * Đăng ký ValueEventListener để lắng nghe thay đổi avatarLocalPath realtime
     * Khi thay đổi -> load ảnh trực tiếp từ đường dẫn (không dùng .get())
     */
    private void setupAvatarListener() {
        avatarListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String localPath = snapshot.getValue(String.class);
                Log.d(TAG, "avatarLocalPath changed: " + localPath);
                if (localPath != null && !localPath.isEmpty()) {
                    File file = new File(localPath);
                    if (file.exists()) {
                        // dùng lastModified làm signature để tránh cache cũ
                        long lastMod = file.lastModified();
                        Glide.with(MainActivity.this)
                                .load(Uri.fromFile(file))
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .signature(new ObjectKey(lastMod))
                                .circleCrop()
                                .placeholder(R.drawable.ic_user_default)
                                .error(R.drawable.ic_user_default)
                                .into(btnSettings);
                    } else {
                        // file không tồn tại (có thể vừa xóa) -> default
                        btnSettings.setImageResource(R.drawable.ic_user_default);
                    }
                } else {
                    btnSettings.setImageResource(R.drawable.ic_user_default);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "avatar listener cancelled", error.toException());
            }
        };

        // đăng ký listener
        userRef.child("avatarLocalPath").addValueEventListener(avatarListener);
    }

    /**
     * Fallback load một lần (dùng khi onResume) - vẫn sử dụng signature nếu file tồn tại
     */
    private void loadUserAvatarOnce() {
        userRef.child("avatarLocalPath").get().addOnSuccessListener(snapshot -> {
            String localPath = snapshot.getValue(String.class);
            Log.d(TAG, "loadUserAvatarOnce localPath=" + localPath);
            if (localPath != null && !localPath.isEmpty()) {
                File file = new File(localPath);
                if (file.exists()) {
                    long lastMod = file.lastModified();
                    Glide.with(MainActivity.this)
                            .load(Uri.fromFile(file))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .signature(new ObjectKey(lastMod))
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
        }).addOnFailureListener(e -> {
            Log.w(TAG, "Load avatar once failed", e);
            btnSettings.setImageResource(R.drawable.ic_user_default);
        });
    }

    private void setupSensorDataListener() {
        mDatabase.child("CamBien").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Float nhietDo = snapshot.child("NhietDo").getValue(Float.class);
                Float doAm = snapshot.child("DoAm").getValue(Float.class);

                tvTempHumid.setText(nhietDo != null ? String.format("%.1f°C", nhietDo) : "--°C");
                tvHumidity.setText(doAm != null ? String.format("%.1f%%", doAm) : "--%");

                DataSnapshot lightSnap = snapshot.child("AnhSang");
                if (lightSnap.exists()) {
                    String trangThai = lightSnap.child("TrangThai").getValue(String.class);
                    tvLightIntensity.setText(trangThai != null ? trangThai : "--");
                } else {
                    tvLightIntensity.setText("--");
                }

                Long doAmDat = snapshot.child("DoAmDat").getValue(Long.class);
                if (doAmDat != null)
                    tvSoilMoisture.setText(doAmDat + "%");
                else tvSoilMoisture.setText("--%");

                String trangThaiMua = snapshot.child("TrangThaiMua").getValue(String.class);
                if (trangThaiMua != null)
                    tvRainStatus.setText(trangThaiMua);
                else tvRainStatus.setText("--");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Firebase Error:", error.toException());
            }
        });
    }

    /** 🧭 Điều hướng an toàn */
    private void setupNavigationCards() {
        setSafeNavigation(cardSoil, SoilHistoryActivity.class);
        setSafeNavigation(cardTempHumid, TempHumidHistoryActivity.class);
        setSafeNavigation(cardLight, LightHistoryActivity.class);
        setSafeNavigation(cardRain, RainHistoryActivity.class);
        setSafeNavigation(cardPump, PumpSettingActivity.class);
        setSafeNavigation(cardLightControl, LightSettingActivity.class);
        setSafeNavigation(cardRoof, RoofSettingActivity.class);
    }

    /** 🧱 Hàm mở Activity an toàn */
    private void setSafeNavigation(CardView card, Class<?> targetActivity) {
        if (card == null) return;
        card.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, targetActivity);
                startActivity(intent);
            } catch (Exception e) {
                Snackbar.make(findViewById(android.R.id.content),
                        "⚠️ Không thể mở trang: " + targetActivity.getSimpleName(),
                        Snackbar.LENGTH_LONG).show();
                Log.e(TAG, "Navigation error: " + targetActivity.getSimpleName(), e);
            }
        });
    }

    /** Thiết lập menu cài đặt (inflate layout_settings_menu) và xử lý nút logout */
    private void setupSettingsMenu() {
        // Inflate layout menu (layout_settings_menu.xml) lên Activity hiện tại
        menuView = getLayoutInflater().inflate(R.layout.layout_settings_menu, null);
        addContentView(menuView, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        settingsMenuPanel = menuView.findViewById(R.id.settings_menu_panel);
        dimBackground = menuView.findViewById(R.id.view_dim_background);

        // cấu hình khởi tạo
        menuView.setVisibility(View.VISIBLE);
        dimBackground.setAlpha(0f);
        dimBackground.setVisibility(View.GONE);
        // đặt menu sang phải ngoài màn hình (sau khi layout đã đo)
        menuView.post(() -> settingsMenuPanel.setTranslationX(settingsMenuPanel.getWidth()));

        // mở/đóng menu khi bấm icon settings
        btnSettings.setOnClickListener(v -> toggleMenu());
        dimBackground.setOnClickListener(v -> { if (isMenuOpen) toggleMenu(); });

        // item About
        View itemAbout = menuView.findViewById(R.id.item_about);
        if (itemAbout != null) {
            itemAbout.setOnClickListener(v -> {
                toggleMenu();
                Snackbar.make(findViewById(android.R.id.content),
                        "Ứng dụng được phát triển bởi Nhóm 3 - Dự án Nhà Thông Minh",
                        Snackbar.LENGTH_SHORT).show();
            });
        }

        // item Account -> ProfileActivity
        View itemAccount = menuView.findViewById(R.id.item_account);
        if (itemAccount != null) {
            itemAccount.setOnClickListener(v -> {
                toggleMenu();
                startActivity(new Intent(this, ProfileActivity.class));
            });
        }

        // === Thêm xử lý cho nút Đăng xuất (btnLogout) ===
        View logoutBtn = menuView.findViewById(R.id.btnLogout);
        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> {
                // Debug: chắc chắn onClick được gọi
                Toast.makeText(MainActivity.this, "Đang đăng xuất...", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "btnLogout clicked");

                // 1) Firebase sign out (nếu dùng)
                try {
                    if (mAuth != null) mAuth.signOut();
                    else FirebaseAuth.getInstance().signOut();
                } catch (Exception e) {
                    Log.w(TAG, "Firebase signOut failed", e);
                }

                // 2) Xóa SharedPreferences (session/local token)
                try {
                    SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();
                } catch (Exception e) {
                    Log.w(TAG, "Clear prefs failed", e);
                }

                // 3) Đóng menu (nếu đang mở)
                if (isMenuOpen) toggleMenu();

                // 4) Chuyển về LoginActivity và xóa backstack để không thể quay lại
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                // đảm bảo Activity này bị finish
                finish();
            });
        } else {
            Log.w(TAG, "btnLogout not found in menuView. Kiểm tra id trong layout_settings_menu.xml");
        }
    }

    private void toggleMenu() {
        float menuWidth = settingsMenuPanel.getWidth();

        if (isMenuOpen) {
            // slide out -> sang phải
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
            // show background và slide in từ phải sang trái
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
