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
import android.view.ViewGroup;
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
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String userId;

    // Views (resolved at runtime via names)
    private TextView tvSoilMoisture;
    private TextView tvTemp;
    private TextView tvHumidity;
    private TextView tvLight;
    private TextView tvRainStatus;

    private CardView cardSoil;
    private CardView cardTempHumid;
    private CardView cardLightSensor;
    private CardView cardRain;
    private CardView cardPump;
    private CardView cardLightControl;
    private CardView cardRoof;

    private ImageView btnSettings;

    // settings menu
    private RelativeLayout settingsMenuPanel;
    private View dimBackground;
    private View menuView;
    private boolean isMenuOpen = false;

    private ValueEventListener avatarListener;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        // root view used for recursive search
        rootView = findViewById(getIdByName("rootConstraint"));
        if (rootView == null) rootView = findViewById(android.R.id.content);

        // Firebase init
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Log.d(TAG, "User not logged in -> open LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // --- FIND VIEWS AT RUNTIME (avoid compile-time missing ids) ---
        tvSoilMoisture = findTextViewByNames(new String[]{"tv_soil_moisture", "tv_soil_value", "tv_soil"});
        tvTemp = findTextViewByNames(new String[]{"tv_temp_humid", "tv_temp_value", "tv_temp"});
        tvHumidity = findTextViewByNames(new String[]{"tv_humidity", "tv_humidity_value", "tv_humid"});
        tvLight = findTextViewByNames(new String[]{"tv_light_intensity", "tv_light_state", "tv_light"});
        tvRainStatus = findTextViewByNames(new String[]{"tv_rain_status", "tv_rain"});

        cardSoil = findCardByNames(new String[]{"card_soil"});
        cardTempHumid = findCardByNames(new String[]{"card_temp_humid", "card_temp"});
        cardLightSensor = findCardByNames(new String[]{"card_light", "card_light_sensor"});
        cardRain = findCardByNames(new String[]{"card_rain"});

        cardPump = findCardByNames(new String[]{"card_pump"});
        cardLightControl = findCardByNames(new String[]{"card_light_control", "card_light"});
        cardRoof = findCardByNames(new String[]{"card_roof"});

        btnSettings = findImageByNames(new String[]{"btn_settings", "btn_avatar", "icon_settings"});

        // debug logs
        if (tvTemp == null) Log.w(TAG, "Temperature TextView not found (tried multiple names).");
        if (tvHumidity == null) Log.w(TAG, "Humidity TextView not found.");
        if (tvLight == null) Log.w(TAG, "Light TextView not found.");
        if (cardLightSensor == null) Log.w(TAG, "Light sensor CardView not found.");
        if (cardLightControl == null) Log.w(TAG, "Light control CardView not found.");

        // Setup features
        setupAvatarListener();
        setupSettingsMenu();
        setupSensorDataListener();
        setupNavigationCards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - load avatar once");
        loadUserAvatarOnce();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && avatarListener != null) {
            try {
                userRef.child("avatarLocalPath").removeEventListener(avatarListener);
            } catch (Exception e) {
                Log.w(TAG, "Failed to remove avatar listener", e);
            }
        }
    }

    /* ---------------- Avatar ---------------- */

    private void setupAvatarListener() {
        if (userRef == null) return;

        avatarListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String localPath = snapshot.getValue(String.class);
                Log.d(TAG, "avatarLocalPath changed: " + localPath);
                if (btnSettings == null) {
                    Log.w(TAG, "btnSettings null - cannot set avatar");
                    return;
                }
                if (localPath != null && !localPath.isEmpty()) {
                    File file = new File(localPath);
                    if (file.exists()) {
                        long lastMod = file.lastModified();
                        try {
                            Glide.with(MainActivity.this)
                                    .load(Uri.fromFile(file))
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .signature(new ObjectKey(lastMod))
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_user_default)
                                    .error(R.drawable.ic_user_default)
                                    .into(btnSettings);
                        } catch (Exception e) {
                            Log.w(TAG, "Glide load avatar failed", e);
                            btnSettings.setImageResource(R.drawable.ic_user_default);
                        }
                    } else {
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

        try {
            userRef.child("avatarLocalPath").addValueEventListener(avatarListener);
        } catch (Exception e) {
            Log.w(TAG, "add avatar listener failed", e);
        }
    }

    private void loadUserAvatarOnce() {
        if (userRef == null) return;
        userRef.child("avatarLocalPath").get().addOnSuccessListener(snapshot -> {
            String localPath = snapshot.getValue(String.class);
            if (btnSettings == null) return;
            if (localPath != null && !localPath.isEmpty()) {
                File file = new File(localPath);
                if (file.exists()) {
                    long lastMod = file.lastModified();
                    try {
                        Glide.with(MainActivity.this)
                                .load(Uri.fromFile(file))
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .signature(new ObjectKey(lastMod))
                                .circleCrop()
                                .placeholder(R.drawable.ic_user_default)
                                .error(R.drawable.ic_user_default)
                                .into(btnSettings);
                    } catch (Exception e) {
                        Log.w(TAG, "Glide load once failed", e);
                        btnSettings.setImageResource(R.drawable.ic_user_default);
                    }
                } else {
                    btnSettings.setImageResource(R.drawable.ic_user_default);
                }
            } else {
                btnSettings.setImageResource(R.drawable.ic_user_default);
            }
        }).addOnFailureListener(e -> {
            Log.w(TAG, "Load avatar once failed", e);
            if (btnSettings != null) btnSettings.setImageResource(R.drawable.ic_user_default);
        });
    }

    /* ---------------- Settings menu ---------------- */

    private void setupSettingsMenu() {
        try {
            menuView = getLayoutInflater().inflate(R.layout.layout_settings_menu, null);
            addContentView(menuView, new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));

            settingsMenuPanel = menuView.findViewById(getIdByName("settings_menu_panel"));
            dimBackground = menuView.findViewById(getIdByName("view_dim_background"));

            menuView.setVisibility(View.VISIBLE);
            if (dimBackground != null) {
                dimBackground.setAlpha(0f);
                dimBackground.setVisibility(View.GONE);
            }
            if (settingsMenuPanel != null) {
                settingsMenuPanel.post(() -> settingsMenuPanel.setTranslationX(settingsMenuPanel.getWidth()));
            }
        } catch (Exception e) {
            Log.w(TAG, "inflate settings menu failed", e);
            menuView = null;
            settingsMenuPanel = null;
            dimBackground = null;
        }

        if (btnSettings != null) btnSettings.setOnClickListener(v -> toggleMenu());
        if (dimBackground != null) dimBackground.setOnClickListener(v -> { if (isMenuOpen) toggleMenu(); });

        if (menuView != null) {
            View itemAbout = menuView.findViewById(getIdByName("item_about"));
            if (itemAbout != null) itemAbout.setOnClickListener(v -> {
                toggleMenu();
                Snackbar.make(findViewById(android.R.id.content),
                        "Ứng dụng được phát triển bởi Nhóm 3 - Dự án Điều Khiển Vườn Thông Minh",
                        Snackbar.LENGTH_SHORT).show();
            });

            View itemAccount = menuView.findViewById(getIdByName("item_account"));
            if (itemAccount != null) itemAccount.setOnClickListener(v -> {
                toggleMenu();
                startActivity(new Intent(this, ProfileActivity.class));
            });

            View logoutBtn = menuView.findViewById(getIdByName("btnLogout"));
            if (logoutBtn != null) logoutBtn.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "Đang đăng xuất...", Toast.LENGTH_SHORT).show();
                try { if (mAuth != null) mAuth.signOut(); else FirebaseAuth.getInstance().signOut(); } catch (Exception e) { Log.w(TAG, "signOut failed", e); }
                try { SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE); prefs.edit().clear().apply(); } catch (Exception e) { Log.w(TAG, "clear prefs failed", e); }
                if (isMenuOpen) toggleMenu();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void toggleMenu() {
        if (settingsMenuPanel == null || dimBackground == null) {
            Log.w(TAG, "toggleMenu views missing");
            return;
        }
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

    /* ---------------- Sensors listener ---------------- */

    private void setupSensorDataListener() {
        if (mDatabase == null) return;

        mDatabase.child("CamBien").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.w(TAG, "CamBien missing");
                    setSensorPlaceholders();
                    return;
                }

                Double nhietDo = toDoubleSafe(snapshot.child("NhietDo").getValue());
                Double doAm = toDoubleSafe(snapshot.child("DoAm").getValue());

                if (tvTemp != null) tvTemp.setText(nhietDo != null ? String.format(Locale.getDefault(), "%.1f°C", nhietDo) : "--°C");
                if (tvHumidity != null) tvHumidity.setText(doAm != null ? String.format(Locale.getDefault(), "%.1f%%", doAm) : "--%");

                DataSnapshot lightSnap = snapshot.child("AnhSang");
                if (tvLight != null) {
                    if (lightSnap.exists()) {
                        String trangThai = lightSnap.child("TrangThai").getValue(String.class);
                        tvLight.setText(trangThai != null ? trangThai : "--");
                    } else tvLight.setText("--");
                }

                Double soilPercent = null;
                DataSnapshot datSnap = snapshot.child("Dat");
                if (datSnap.exists()) {
                    Double phanTram = toDoubleSafe(datSnap.child("PhanTram").getValue());
                    if (phanTram != null) soilPercent = phanTram;
                    else {
                        Double analog = toDoubleSafe(datSnap.child("Analog").getValue());
                        if (analog != null) soilPercent = (analog / 4095.0) * 100.0;
                    }
                } else {
                    Double doAmDatRoot = toDoubleSafe(snapshot.child("DoAmDat").getValue());
                    if (doAmDatRoot != null) soilPercent = doAmDatRoot;
                }
                if (tvSoilMoisture != null) tvSoilMoisture.setText(soilPercent != null ? String.format(Locale.getDefault(), "%.0f%%", soilPercent) : "--%");

                String trangThaiMua = null;
                DataSnapshot muaSnap = snapshot.child("Mua");
                if (muaSnap.exists()) {
                    trangThaiMua = muaSnap.child("TrangThai").getValue(String.class);
                    if (trangThaiMua == null) {
                        Long digital = toLongSafe(muaSnap.child("Digital").getValue());
                        if (digital != null) trangThaiMua = (digital == 0) ? "Có mưa" : "Không mưa";
                        else trangThaiMua = snapshot.child("TrangThaiMua").getValue(String.class);
                    }
                } else {
                    trangThaiMua = snapshot.child("TrangThaiMua").getValue(String.class);
                }
                if (tvRainStatus != null) tvRainStatus.setText(trangThaiMua != null ? trangThaiMua : "--");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Firebase error", error.toException());
            }
        });
    }

    private void setSensorPlaceholders() {
        if (tvTemp != null) tvTemp.setText("--°C");
        if (tvHumidity != null) tvHumidity.setText("--%");
        if (tvLight != null) tvLight.setText("--");
        if (tvSoilMoisture != null) tvSoilMoisture.setText("--%");
        if (tvRainStatus != null) tvRainStatus.setText("--");
    }

    /* ---------------- Navigation (distinguish duplicate ids) ---------------- */

    private void setupNavigationCards() {
        // basic ones
        setSafeNavigation(cardSoil, SoilHistoryActivity.class);
        setSafeNavigation(cardTempHumid, TempHumidHistoryActivity.class);
        setSafeNavigation(cardRain, RainHistoryActivity.class);

        // 1) Light sensor card -> LightHistoryActivity
        int tvLightStateId = getIdByName("tv_light_state"); // exists in item_light_card.xml
        CardView sensorLightCard = null;
        if (tvLightStateId != 0) sensorLightCard = findCardContainingChildId(tvLightStateId);
        if (sensorLightCard == null) sensorLightCard = cardLightSensor; // fallback
        setSafeNavigation(sensorLightCard, LightHistoryActivity.class);

        // 2) Light control card -> LightSettingActivity
        CardView controlLightCard = findCardContainingText("Hệ thống Đèn chiếu sáng");
        if (controlLightCard == null) controlLightCard = findCardByNames(new String[]{"card_light_control", "card_light"});
        if (controlLightCard == null) controlLightCard = cardLightControl; // fallback
        setSafeNavigation(controlLightCard, LightSettingActivity.class);

        // others
        setSafeNavigation(cardPump, PumpSettingActivity.class);
        setSafeNavigation(cardRoof, RoofSettingActivity.class);
    }

    private void setSafeNavigation(CardView card, Class<?> targetActivity) {
        if (card == null) return;
        card.setOnClickListener(v -> {
            try {
                startActivity(new Intent(MainActivity.this, targetActivity));
            } catch (Exception e) {
                Snackbar.make(findViewById(android.R.id.content),
                        "⚠️ Không thể mở trang: " + targetActivity.getSimpleName(),
                        Snackbar.LENGTH_LONG).show();
                Log.e(TAG, "Navigation error: " + targetActivity.getSimpleName(), e);
            }
        });
    }

    /** Find CardView which contains descendant with given childId (search up the parent chain) */
    private CardView findCardContainingChildId(int childId) {
        if (childId == 0 || rootView == null) return null;
        View child = tryFindView(childId);
        if (child == null) return null;
        View parent = (View) child.getParent();
        while (parent != null) {
            if (parent instanceof CardView) return (CardView) parent;
            View p = (View) parent.getParent();
            if (p == null) break;
            parent = p;
        }
        // fallback full recursive search
        return findCardContainingChildRecursive(rootView, childId);
    }

    private CardView findCardContainingChildRecursive(View v, int childId) {
        if (v == null) return null;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                View c = g.getChildAt(i);
                if (c.getId() == childId) {
                    View parent = (View) c.getParent();
                    while (parent != null) {
                        if (parent instanceof CardView) return (CardView) parent;
                        View p = (View) parent.getParent();
                        if (p == null) break;
                        parent = p;
                    }
                }
                CardView found = findCardContainingChildRecursive(c, childId);
                if (found != null) return found;
            }
        }
        return null;
    }

    /** Find CardView that contains a TextView whose text contains targetText (case-insensitive) */
    private CardView findCardContainingText(String targetText) {
        if (targetText == null || rootView == null) return null;
        String lower = targetText.trim().toLowerCase();
        return findCardContainingTextRecursive(rootView, lower);
    }

    private CardView findCardContainingTextRecursive(View v, String lowerText) {
        if (v == null) return null;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                View c = g.getChildAt(i);
                if (c instanceof TextView) {
                    CharSequence cs = ((TextView) c).getText();
                    if (cs != null) {
                        String s = cs.toString().trim().toLowerCase();
                        if (s.contains(lowerText) || lowerText.contains(s)) {
                            View parent = (View) c.getParent();
                            while (parent != null) {
                                if (parent instanceof CardView) return (CardView) parent;
                                View p = (View) parent.getParent();
                                if (p == null) break;
                                parent = p;
                            }
                        }
                    }
                }
                CardView found = findCardContainingTextRecursive(c, lowerText);
                if (found != null) return found;
            }
        } else if (v instanceof TextView) {
            CharSequence cs = ((TextView) v).getText();
            if (cs != null && cs.toString().toLowerCase().contains(lowerText)) {
                View parent = (View) v.getParent();
                while (parent != null) {
                    if (parent instanceof CardView) return (CardView) parent;
                    View p = (View) parent.getParent();
                    if (p == null) break;
                    parent = p;
                }
            }
        }
        return null;
    }

    /* ---------------- Safe find helpers (by name) ---------------- */

    private int getIdByName(String idName) {
        if (idName == null) return 0;
        return getResources().getIdentifier(idName, "id", getPackageName());
    }

    private TextView findTextViewByNames(String[] names) {
        for (String n : names) {
            int id = getIdByName(n);
            if (id != 0) {
                View v = tryFindView(id);
                if (v instanceof TextView) return (TextView) v;
            }
        }
        return null;
    }

    private CardView findCardByNames(String[] names) {
        for (String n : names) {
            int id = getIdByName(n);
            if (id != 0) {
                View v = tryFindView(id);
                if (v instanceof CardView) return (CardView) v;
            }
        }
        return null;
    }

    private CardView findCardByNames(int[] ids) {
        for (int id : ids) {
            if (id == 0) continue;
            View v = tryFindView(id);
            if (v instanceof CardView) return (CardView) v;
        }
        return null;
    }

    private ImageView findImageByNames(String[] names) {
        for (String n : names) {
            int id = getIdByName(n);
            if (id != 0) {
                View v = tryFindView(id);
                if (v instanceof ImageView) return (ImageView) v;
            }
        }
        return null;
    }

    private View tryFindView(int id) {
        try {
            View v = findViewById(id);
            if (v != null) return v;
            if (rootView != null) return findViewRecursive(rootView, id);
        } catch (Exception e) {
            Log.w(TAG, "tryFindView error for id=" + id, e);
        }
        return null;
    }

    private View findViewRecursive(View parent, int id) {
        if (parent == null) return null;
        if (parent.getId() == id) return parent;
        if (!(parent instanceof ViewGroup)) return null;
        ViewGroup group = (ViewGroup) parent;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.getId() == id) return child;
            View found = findViewRecursive(child, id);
            if (found != null) return found;
        }
        return null;
    }

    /* ---------------- Helpers parse ---------------- */

    private Double toDoubleSafe(Object val) {
        if (val == null) return null;
        try {
            if (val instanceof Double) return (Double) val;
            if (val instanceof Long) return ((Long) val).doubleValue();
            if (val instanceof Integer) return ((Integer) val).doubleValue();
            if (val instanceof Float) return ((Float) val).doubleValue();
            if (val instanceof String) return Double.parseDouble((String) val);
        } catch (Exception e) {
            Log.w(TAG, "toDoubleSafe parse error for " + val, e);
        }
        return null;
    }

    private Long toLongSafe(Object val) {
        if (val == null) return null;
        try {
            if (val instanceof Long) return (Long) val;
            if (val instanceof Integer) return ((Integer) val).longValue();
            if (val instanceof Double) return ((Double) val).longValue();
            if (val instanceof String) return Long.parseLong((String) val);
        } catch (Exception e) {
            Log.w(TAG, "toLongSafe parse error for " + val, e);
        }
        return null;
    }
}
