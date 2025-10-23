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

    // Khai báo Database Reference (Tham chiếu Cơ sở dữ liệu)
    private DatabaseReference mDatabase;

    // Khai báo các thành phần UI (TextView cho cảm biến)
    private TextView tvSoilMoisture; // Độ ẩm Đất
    private TextView tvTempHumid;    // Nhiệt độ/Độ ẩm
    private TextView tvLightIntensity; // Cường độ Ánh sáng
    private TextView tvRainStatus;     // Trạng thái Mưa

    // Khai báo các thành phần UI (Switch cho điều khiển)
    private Switch swWaterPump; // Bơm Nước
    private Switch swLight;     // Đèn
    private Switch swRoof;      // Mái Che

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Khởi tạo Firebase
        // Đảm bảo bạn đã thêm google-services.json và dependencies
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 2. Liên kết các thành phần UI bằng findViewById
        tvSoilMoisture = findViewById(R.id.tv_soil_moisture);
        tvTempHumid = findViewById(R.id.tv_temp_humid);
        tvLightIntensity = findViewById(R.id.tv_light_intensity);
        tvRainStatus = findViewById(R.id.tv_rain_status);

        swWaterPump = findViewById(R.id.sw_water_pump);
        swLight = findViewById(R.id.sw_light);
        swRoof = findViewById(R.id.sw_roof);

        // 3. Đọc dữ liệu cảm biến từ Firebase (Realtime Listener)
        setupSensorDataListener();

        // 4. Thiết lập sự kiện lắng nghe cho các Switch để GHI dữ liệu lên Firebase
        setupControlSwitches();
    }

    /**
     * Thiết lập lắng nghe dữ liệu từ Firebase cho các cảm biến.
     */
    private void setupSensorDataListener() {
        // Lắng nghe thay đổi tại nút 'sensors'
        mDatabase.child("sensors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Lấy các giá trị và cập nhật UI

                // Đất
                Long soilMoisture = dataSnapshot.child("soil_moisture").getValue(Long.class);
                if (soilMoisture != null) {
                    tvSoilMoisture.setText(soilMoisture + "%");
                } else {
                    tvSoilMoisture.setText("N/A");
                }

                // Nhiệt độ & Độ ẩm
                String tempHumid = dataSnapshot.child("dht").getValue(String.class);
                if (tempHumid != null) {
                    tvTempHumid.setText(tempHumid);
                } else {
                    tvTempHumid.setText("N/A");
                }

                // Ánh sáng
                Long lightIntensity = dataSnapshot.child("light_intensity").getValue(Long.class);
                if (lightIntensity != null) {
                    tvLightIntensity.setText(lightIntensity + " Lux");
                } else {
                    tvLightIntensity.setText("N/A");
                }

                // Trạng thái Mưa
                String rainStatus = dataSnapshot.child("rain_status").getValue(String.class);
                if (rainStatus != null) {
                    tvRainStatus.setText(rainStatus);
                } else {
                    tvRainStatus.setText("N/A");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { // <-- LỖI ĐÃ SỬA TẠI ĐÂY
                // Xử lý lỗi
                Log.w(TAG, "Lỗi đọc dữ liệu cảm biến: ", databaseError.toException());
                tvSoilMoisture.setText("Error");
            }
        });
    }

    /**
     * Thiết lập sự kiện lắng nghe cho các Switch (Đọc và Ghi).
     */
    private void setupControlSwitches() {
        // ĐỌC trạng thái Bơm Nước từ Firebase (Đảm bảo đồng bộ trạng thái khi app khởi động)
        mDatabase.child("controls").child("water_pump").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean state = snapshot.getValue(Boolean.class);
                if (state != null) {
                    // Vô hiệu hóa listener để tránh vòng lặp khi cập nhật trạng thái
                    swWaterPump.setOnCheckedChangeListener(null);
                    swWaterPump.setChecked(state);
                    // Kích hoạt lại listener
                    swWaterPump.setOnCheckedChangeListener(controlSwitchListener);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Lỗi đọc trạng thái bơm: ", error.toException());
            }
        });

        // Thiết lập OnCheckedChangeListener cho các Switch điều khiển khác
        swWaterPump.setOnCheckedChangeListener(controlSwitchListener);
        swLight.setOnCheckedChangeListener(controlSwitchListener);
        swRoof.setOnCheckedChangeListener(controlSwitchListener);
    }

    // Listener chung để ghi trạng thái Switch lên Firebase
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
                // Khai báo biến final để sử dụng trong lambda (SỬA LỖI LAMBDA)
                final String nodeKey = controlNode;

                // Ghi trạng thái (true/false) lên node controls/controlNode
                mDatabase.child("controls").child(nodeKey).setValue(isChecked)
                        .addOnFailureListener(e -> Log.e(TAG, "Lỗi ghi trạng thái điều khiển " + nodeKey, e));
            }
        }
    };
}