package com.example.laptrinhdidong1;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNewEmail, etNewPassword, etConfirmPassword, etName, etPhone;
    private Button btnRegister;
    private TextView tvBackToLogin;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 🔥 Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users"); // node lưu thông tin người dùng

        // Ánh xạ
        etNewEmail = findViewById(R.id.etNewEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etName = findViewById(R.id.etName); // thêm tên
        etPhone = findViewById(R.id.etPhone); // thêm sđt
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // 🔙 Quay lại đăng nhập
        tvBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // ✅ Nút đăng ký
        btnRegister.setOnClickListener(v -> {
            String email = etNewEmail.getText().toString().trim();
            String pass = etNewPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔐 Tạo tài khoản Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // 🔹 Lưu thông tin cá nhân vào Firebase Database
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("phone", phone);
                                userData.put("gender", "");
                                userData.put("birthday", "");
                                userData.put("avatarUrl", ""); // để trống ban đầu

                                dbRef.child(user.getUid()).setValue(userData)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(this, LoginActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Lỗi lưu dữ liệu người dùng!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                            Toast.makeText(this, "❌ Lỗi: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
