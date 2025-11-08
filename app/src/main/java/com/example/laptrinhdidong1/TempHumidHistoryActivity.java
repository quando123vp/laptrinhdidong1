package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TempHumidHistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LineChart chart;
    private DatabaseReference db;

    // 🔹 THAY ĐỔI: Sử dụng RecyclerView
    private RecyclerView rvHistory;
    private TextView tvNoData;
    private HistoryAdapter adapter;
    private ArrayList<HistoryItem> historyList = new ArrayList<>();
    // 🔹 KẾT THÚC THAY ĐỔI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_humid_history);

        // 🔹 THAY ĐỔI: Ánh xạ view
        btnBack = findViewById(R.id.btnBackTempHumid);
        chart = findViewById(R.id.chartTempHumid);
        rvHistory = findViewById(R.id.rv_temp_humid_history);
        tvNoData = findViewById(R.id.tv_no_data);
        db = FirebaseDatabase.getInstance().getReference("LichSu");
        // 🔹 KẾT THÚC THAY ĐỔI

        btnBack.setOnClickListener(v -> onBackPressed());

        setupRecyclerView();
        loadHistory();
    }

    private void setupRecyclerView() {
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this, historyList);
        rvHistory.setAdapter(adapter);
    }

    private void loadHistory() {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 🔹 THAY ĐỔI: Xóa list cũ
                historyList.clear();
                ArrayList<Entry> allTempEntries = new ArrayList<>();
                ArrayList<Entry> allHumidEntries = new ArrayList<>();
                int index = 0;
                // 🔹 KẾT THÚC THAY ĐỔI

                if (!snapshot.exists()) {
                    // 🔹 THAY ĐỔI: Hiển thị thông báo "No Data"
                    tvNoData.setVisibility(View.VISIBLE);
                    rvHistory.setVisibility(View.GONE);
                    chart.clear();
                    chart.invalidate();
                    // 🔹 KẾT THÚC THAY ĐỔI
                    return;
                }

                // 🔹 THAY ĐỔI: Ẩn thông báo "No Data"
                tvNoData.setVisibility(View.GONE);
                rvHistory.setVisibility(View.VISIBLE);
                // 🔹 KẾT THÚC THAY ĐỔI

                for (DataSnapshot timeSnap : snapshot.getChildren()) {
                    Float nhietDo = timeSnap.child("NhietDo").getValue(Float.class);
                    Float doAm = timeSnap.child("DoAm").getValue(Float.class);
                    String time = timeSnap.getKey();

                    if (nhietDo != null && doAm != null) {
                        allTempEntries.add(new Entry(index, nhietDo));
                        allHumidEntries.add(new Entry(index, doAm));
                        index++;

                        // 🔹 THAY ĐỔI: Thêm vào list cho RecyclerView
                        historyList.add(new HistoryItem(time, nhietDo, doAm));
                        // 🔹 KẾT THÚC THAY ĐỔI
                    }
                }

                // 🔹 THAY ĐỔI: Đảo ngược list để hiển thị mục mới nhất lên đầu
                Collections.reverse(historyList);
                // Thông báo cho adapter biết dữ liệu đã thay đổi
                adapter.notifyDataSetChanged();
                // 🔹 KẾT THÚC THAY ĐỔI

                // --- Logic lọc 10 entry cuối cho biểu đồ (giữ nguyên) ---
                ArrayList<Entry> chartTempEntries;
                ArrayList<Entry> chartHumidEntries;

                if (allTempEntries.size() > 10) {
                    int totalSize = allTempEntries.size();
                    List<Entry> last10Temp = allTempEntries.subList(totalSize - 10, totalSize);
                    List<Entry> last10Humid = allHumidEntries.subList(totalSize - 10, totalSize);

                    chartTempEntries = new ArrayList<>();
                    chartHumidEntries = new ArrayList<>();
                    for (int i = 0; i < last10Temp.size(); i++) {
                        chartTempEntries.add(new Entry(i, last10Temp.get(i).getY()));
                        chartHumidEntries.add(new Entry(i, last10Humid.get(i).getY()));
                    }
                } else {
                    chartTempEntries = allTempEntries;
                    chartHumidEntries = allHumidEntries;
                }

                if (!chartTempEntries.isEmpty()) {
                    showChart(chartTempEntries, chartHumidEntries);
                } else {
                    chart.clear();
                    chart.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TempHumidHistoryActivity.this, "❌ Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                tvNoData.setText("❌ Lỗi tải dữ liệu");
                tvNoData.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
            }
        });
    }

    private void showChart(ArrayList<Entry> tempEntries, ArrayList<Entry> humidEntries) {

        // --- 🔹 BỘ STYLE HIỆN ĐẠI CHO BIỂU ĐỒ ---

        // 1. Set Nhiệt độ (Màu Cam)
        LineDataSet tempSet = new LineDataSet(tempEntries, "Nhiệt độ (°C)");
        int tempColor = ContextCompat.getColor(this, R.color.iconOrange);
        tempSet.setColor(tempColor);
        tempSet.setCircleColor(tempColor);
        tempSet.setLineWidth(2.5f);
        tempSet.setCircleRadius(4f);
        tempSet.setDrawCircleHole(false);
        tempSet.setValueTextSize(10f);
        tempSet.setValueTextColor(Color.BLACK);
        // Hiệu ứng fill (tô màu)
        tempSet.setDrawFilled(true);
        tempSet.setFillColor(tempColor);
        tempSet.setFillAlpha(40); // Độ mờ 0-255

        // 2. Set Độ ẩm (Màu Xanh)
        LineDataSet humidSet = new LineDataSet(humidEntries, "Độ ẩm (%)");
        int humidColor = ContextCompat.getColor(this, R.color.primaryBlue);
        humidSet.setColor(humidColor);
        humidSet.setCircleColor(humidColor);
        humidSet.setLineWidth(2.5f);
        humidSet.setCircleRadius(4f);
        humidSet.setDrawCircleHole(false);
        humidSet.setValueTextSize(10f);
        humidSet.setValueTextColor(Color.BLACK);
        // Hiệu ứng fill (tô màu)
        humidSet.setDrawFilled(true);
        humidSet.setFillColor(humidColor);
        humidSet.setFillAlpha(40);

        // --- 🔹 KẾT THÚC BỘ STYLE ---

        LineData data = new LineData(tempSet, humidSet);
        data.setDrawValues(false); // Ẩn giá trị (số) trên các điểm cho gọn
        chart.setData(data);

        // Tùy chỉnh trục X (Trục thời gian)
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        xAxis.setDrawGridLines(false); // Ẩn lưới dọc

        // Tùy chỉnh trục Y (Trục giá trị)
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        leftAxis.setGridColor(ContextCompat.getColor(this, R.color.divider_color)); // Làm mờ lưới ngang
        chart.getAxisRight().setEnabled(false); // Tắt trục Y bên phải

        // Chú thích
        Legend legend = chart.getLegend();
        legend.setTextSize(12f);
        legend.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        legend.setForm(Legend.LegendForm.LINE);

        chart.getDescription().setText("10 mốc gần nhất");
        chart.getDescription().setTextSize(12f);
        chart.getDescription().setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        chart.animateX(500); // Thêm hiệu ứng động
        chart.invalidate(); // refresh chart
    }

    // ⛔ KHÔNG CẦN HÀM NÀY NỮA ⛔
    // private void addText(String s) { ... }
}