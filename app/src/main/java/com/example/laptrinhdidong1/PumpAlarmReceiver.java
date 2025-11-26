package com.example.laptrinhdidong1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PumpAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();

        // --- SỬA ĐỔI: Ưu tiên lấy độ ẩm từ Intent (cài riêng cho giờ này) ---
        // Nếu không có trong Intent (trường hợp cũ) thì mới lấy trong SharedPreferences
        int intentMoisture = intent.getIntExtra("TARGET_MOISTURE", -1);

        int finalTargetMoisture;
        if (intentMoisture != -1) {
            finalTargetMoisture = intentMoisture;
        } else {
            SharedPreferences prefs = context.getSharedPreferences("PumpPrefs", Context.MODE_PRIVATE);
            finalTargetMoisture = prefs.getInt("saved_target_moisture", 70);
        }

        DatabaseReference cmdRef = FirebaseDatabase.getInstance().getReference().child("Bom").child("Command");

        // Gửi độ ẩm mục tiêu lên Firebase
        cmdRef.child("TargetMoisture").setValue(finalTargetMoisture);
        cmdRef.child("ThoiGian").setValue(0);

        cmdRef.child("TrangThai").setValue("On").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showNotification(context, finalTargetMoisture);

                    boolean isOneTime = intent.getBooleanExtra("IS_ONE_TIME", false);
                    if (isOneTime) {
                        int reqCode = intent.getIntExtra("REQUEST_CODE", -1);
                        SharedPreferences prefs = context.getSharedPreferences("PumpPrefs", Context.MODE_PRIVATE);
                        prefs.edit().putInt("PENDING_DELETE_ID", reqCode).apply();
                    }
                }
                pendingResult.finish();
            }
        });
    }

    private void showNotification(Context context, int target) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "pump_channel_id";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Thông báo máy bơm", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Máy bơm đang chạy 💧")
                .setContentText("Hệ thống đang bơm đến độ ẩm " + target + "%")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}