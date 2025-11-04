package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UpdateProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1000;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private EditText etFullName, etPhone, etBirth;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnSave;
    private ImageView imgAvatar, btnChangeAvatar;

    private DatabaseReference databaseReference;
    private String userId;
    private Uri imageUri;
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        // 🔹 Ánh xạ view
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etBirth = findViewById(R.id.etBirth);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        btnSave = findViewById(R.id.btnSave);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);

        // ⚙️ Firebase setup
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        } else {
            Toast.makeText(this, "Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 📅 Ngày sinh chọn từ DatePicker
        etBirth.setFocusable(false);
        etBirth.setOnClickListener(v -> showDatePicker());

        // 🖼️ Đổi ảnh đại diện
        btnChangeAvatar.setOnClickListener(v -> checkImagePermission());

        // 💾 Lưu thông tin
        btnSave.setOnClickListener(v -> saveProfile());
    }

    /** 📅 Chọn ngày sinh (giới hạn ≥18 tuổi) */
    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    calendar.set(y, m, d);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etBirth.setText(sdf.format(calendar.getTime()));
                },
                year, month, day
        );

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        dialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        dialog.show();
    }

    /** 🧩 Kiểm tra quyền đọc ảnh */
    private void checkImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        }
    }

    /** 📂 Mở thư viện ảnh */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /** 📸 Nhận ảnh được chọn */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgAvatar.setImageURI(imageUri);
        }
    }

    /** 💾 Lưu thông tin người dùng */
    private void saveProfile() {
        String name = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String birth = etBirth.getText().toString().trim();
        String gender = (rgGender.getCheckedRadioButtonId() == R.id.rbMale) ? "Nam" : "Nữ";

        if (name.isEmpty() || phone.isEmpty() || birth.isEmpty()) {
            Toast.makeText(this, "⚠️ Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidAge(birth)) {
            Toast.makeText(this, "⚠️ Tuổi của bạn phải từ 18 trở lên!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("birth", birth);
        updates.put("gender", gender);

        // 🖼️ Lưu ảnh local
        if (imageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                File dir = new File(getExternalFilesDir("avatars"), "");
                if (!dir.exists()) dir.mkdirs();

                File file = new File(dir, "avatar_" + userId + ".jpg");
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

                updates.put("avatarLocalPath", file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "❌ Lỗi lưu ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        // 🔥 Cập nhật Firebase và trả kết quả về ProfileActivity
        databaseReference.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK, new Intent().putExtra("updated", true));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "❌ Lỗi khi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /** 🧮 Kiểm tra tuổi hợp lệ (≥18) */
    private boolean isValidAge(String birth) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar birthCal = Calendar.getInstance();
            birthCal.setTime(sdf.parse(birth));
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) age--;
            return age >= 18;
        } catch (ParseException e) {
            return false;
        }
    }
}
