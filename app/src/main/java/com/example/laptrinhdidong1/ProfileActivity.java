package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvPhone, tvGender, tvBirth, btnUpdate;
    private ImageView imgAvatar, btnBack, icEdit;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 🔹 Ánh xạ view
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        tvGender = findViewById(R.id.tvGender);
        tvBirth = findViewById(R.id.tvBirthday);
        btnUpdate = findViewById(R.id.btnUpdate);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnBack = findViewById(R.id.btnBack);
        icEdit = findViewById(R.id.icEdit);

        // ⚙️ Firebase setup
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            goToLogin();
            return;
        }

        userId = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // 🔁 Load thông tin người dùng
        loadUserInfo();

        // ✏️ Sửa thông tin (ấn vào icon hoặc chữ đều mở UpdateProfileActivity)
        View.OnClickListener editProfileListener = v -> {
            Intent intent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
            startActivityForResult(intent, 1001);
        };
        btnUpdate.setOnClickListener(editProfileListener);
        icEdit.setOnClickListener(editProfileListener);

        // 🔙 Quay lại MainActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    /** 📥 Load thông tin người dùng từ Firebase */
    private void loadUserInfo() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                tvName.setText(snapshot.child("name").getValue(String.class));
                tvPhone.setText(snapshot.child("phone").getValue(String.class));
                tvGender.setText(snapshot.child("gender").getValue(String.class));
                tvBirth.setText(snapshot.child("birth").getValue(String.class));

                // 🖼️ Hiển thị ảnh cục bộ (nếu có)
                String localPath = snapshot.child("avatarLocalPath").getValue(String.class);
                if (localPath != null && !localPath.isEmpty()) {
                    File file = new File(localPath);
                    if (file.exists()) {
                        Glide.with(ProfileActivity.this)
                                .load(Uri.fromFile(file))
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .placeholder(R.drawable.ic_user_default)
                                .error(R.drawable.ic_user_default)
                                .circleCrop()
                                .into(imgAvatar);
                    } else {
                        imgAvatar.setImageResource(R.drawable.ic_user_default);
                    }
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_user_default);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 🧭 Khi quay lại từ UpdateProfileActivity thì reload lại thông tin */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null && data.getBooleanExtra("updated", false)) {
            loadUserInfo();
        }
    }

    /** 🚪 Quay lại LoginActivity khi chưa đăng nhập */
    private void goToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
