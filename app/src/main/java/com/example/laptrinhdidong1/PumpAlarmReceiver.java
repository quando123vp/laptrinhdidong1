package com.example.laptrinhdidong1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PumpAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. Gửi lệnh BẬT bơm lên Firebase
        DatabaseReference cmdRef = FirebaseDatabase.getInstance().getReference().child("Bom").child("Command");

        // Cài đặt các thông số (giống như nút Manual Pump)
        cmdRef.child("TrangThai").setValue("On");
        cmdRef.child("TargetMoisture").setValue(70); // Hoặc lấy giá trị từ Intent gửi sang
        cmdRef.child("ThoiGian").setValue(0);

        Toast.makeText(context, "⏰ Đã đến giờ! Đang bật máy bơm...", Toast.LENGTH_LONG).show();
    }
}