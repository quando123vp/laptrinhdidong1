package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private DatabaseReference mDatabase;

    private TextView tvSoilMoisture;
    private TextView tvTempHumid;
    private TextView tvLightIntensity;
    private TextView tvRainStatus;

    private Switch swWaterPump;
    private Switch swLight;
    private Switch swRoof;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔥 Khởi tạo Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 🧩 Gán UI
        tvSoilMoisture = findViewById(R.id.tv_soil_moisture);
        tvTempHumid = findViewById(R.id.tv_temp_humid);
        tvLightIntensity = findViewById(R.id.tv_light_intensity);
        tvRainStatus = findViewById(R.id.tv_rain_status);

        swWaterPump = findViewById(R.id.sw_water_pump);
        swLight = findViewById(R.id.sw_light);
        swRoof = findViewById(R.id.sw_roof);

        // Đọc cảm biến
        setupSensorDataListener();
        // Thiết lập công tắc điều khiển
        setupControlSwitches();
    }

    /**
     * Lắng nghe dữ liệu từ Firebase Realtime Database
     */
    private void setupSensorDataListener() {
        // 🧠 Lắng nghe dữ liệu cảm biến chung
        mDatabase.child("sensors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 1️⃣ Độ ẩm đất
                Long soilMoisture = dataSnapshot.child("soil_moisture").getValue(Long.class);
                tvSoilMoisture.setText(soilMoisture != null ? soilMoisture + "%" : "N/A");

                // 2️⃣ Nhiệt độ / độ ẩm (chuỗi gộp)
                String tempHumid = dataSnapshot.child("dht").getValue(String.class);
                tvTempHumid.setText(tempHumid != null ? tempHumid : "N/A");

                // 3️⃣ Trạng thái mưa
                String rainStatus = dataSnapshot.child("rain_status").getValue(String.class);
                tvRainStatus.setText(rainStatus != null ? rainStatus : "N/A");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Lỗi đọc dữ liệu cảm biến: ", error.toException());
            }
        });

        // 🆕 ✅ Đọc dữ liệu từ node "CamBien"
        mDatabase.child("CamBien").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // --- Đọc nhiệt độ & độ ẩm ---
                    Float nhietDo = snapshot.child("NhietDo").getValue(Float.class);
                    Float doAm = snapshot.child("DoAm").getValue(Float.class);

                    if (nhietDo != null && doAm != null) {
                        String text = "🌡 " + nhietDo + "°C  |  💧 " + doAm + "%";
                        tvTempHumid.setText(text);
                    }

                    // 🆕 --- Đọc trạng thái ánh sáng ---
                    DataSnapshot lightSnap = snapshot.child("AnhSang");
                    if (lightSnap.exists()) {
                        String trangThai = lightSnap.child("TrangThai").getValue(String.class);
                        if (trangThai != null) {
                            tvLightIntensity.setText("💡 " + trangThai);
                        } else {
                            tvLightIntensity.setText("N/A");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Lỗi đọc dữ liệu từ CamBien: ", error.toException());
            }
        });
    }

    /**
     * Ghi dữ liệu điều khiển từ app → Firebase
     */
    private void setupControlSwitches() {
        // Lắng nghe trạng thái Bơm nước từ Firebase
        mDatabase.child("controls").child("water_pump").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean state = snapshot.getValue(Boolean.class);
                if (state != null) {
                    swWaterPump.setOnCheckedChangeListener(null);
                    swWaterPump.setChecked(state);
                    swWaterPump.setOnCheckedChangeListener(controlSwitchListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Lỗi đọc trạng thái bơm: ", error.toException());
            }
        });

        // Lắng nghe các switch khác
        swWaterPump.setOnCheckedChangeListener(controlSwitchListener);
        swLight.setOnCheckedChangeListener(controlSwitchListener);
        swRoof.setOnCheckedChangeListener(controlSwitchListener);
    }

    // Listener ghi trạng thái switch điều khiển lên Firebase
    private final CompoundButton.OnCheckedChangeListener controlSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String controlNode = "";

            int id = buttonView.getId();
            if (id == R.id.sw_water_pump) {
                controlNode = "water_pump";
            } else if (id == R.id.sw_light) {
                controlNode = "light";
            } else if (id == R.id.sw_roof) {
                controlNode = "roof";
            }

            if (!controlNode.isEmpty()) {
                final String nodeKey = controlNode;
                mDatabase.child("controls").child(nodeKey).setValue(isChecked)
                        .addOnFailureListener(e -> Log.e(TAG, "Lỗi ghi trạng thái " + nodeKey, e));
            }
        }
    };
}
