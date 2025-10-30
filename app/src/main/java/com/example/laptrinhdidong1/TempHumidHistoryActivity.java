package com.example.laptrinhdidong1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
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

public class TempHumidHistoryActivity extends AppCompatActivity {

    private LinearLayout llContainer;
    private ImageView btnBack;
    private LineChart chart;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_humid_history);

        llContainer = findViewById(R.id.ll_temp_humid_history);
        btnBack = findViewById(R.id.btnBackTempHumid);
        chart = findViewById(R.id.chartTempHumid);
        db = FirebaseDatabase.getInstance().getReference("LichSu");

        btnBack.setOnClickListener(v -> onBackPressed());
        loadHistory();
    }

    private void loadHistory() {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                llContainer.removeAllViews();
                ArrayList<Entry> tempEntries = new ArrayList<>();
                ArrayList<Entry> humidEntries = new ArrayList<>();

                int index = 0;

                if (!snapshot.exists()) {
                    addText("⚠️ Chưa có dữ liệu lịch sử");
                    return;
                }

                for (DataSnapshot timeSnap : snapshot.getChildren()) {
                    Float nhietDo = timeSnap.child("NhietDo").getValue(Float.class);
                    Float doAm = timeSnap.child("DoAm").getValue(Float.class);
                    String time = timeSnap.getKey();

                    if (nhietDo != null && doAm != null) {
                        // Dữ liệu cho chart
                        tempEntries.add(new Entry(index, nhietDo));
                        humidEntries.add(new Entry(index, doAm));
                        index++;

                        // Hiển thị chi tiết
                        addText("⏱ " + time + " → " + nhietDo + "°C | " + doAm + "%");
                    }
                }

                // Vẽ biểu đồ
                showChart(tempEntries, humidEntries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TempHumidHistoryActivity.this, "❌ Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChart(ArrayList<Entry> tempEntries, ArrayList<Entry> humidEntries) {
        LineDataSet tempSet = new LineDataSet(tempEntries, "Nhiệt độ (°C)");
        tempSet.setColor(Color.RED);
        tempSet.setCircleColor(Color.RED);
        tempSet.setLineWidth(2f);
        tempSet.setValueTextSize(10f);

        LineDataSet humidSet = new LineDataSet(humidEntries, "Độ ẩm (%)");
        humidSet.setColor(Color.BLUE);
        humidSet.setCircleColor(Color.BLUE);
        humidSet.setLineWidth(2f);
        humidSet.setValueTextSize(10f);

        LineData data = new LineData(tempSet, humidSet);
        chart.setData(data);

        // Tùy chỉnh trục
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        chart.getAxisRight().setEnabled(false);

        // Hiển thị chú thích
        Legend legend = chart.getLegend();
        legend.setTextSize(12f);

        chart.getDescription().setText("Lịch sử Nhiệt độ & Độ ẩm");
        chart.getDescription().setTextSize(12f);
        chart.invalidate(); // refresh chart
    }

    private void addText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(16);
        t.setPadding(16, 10, 16, 10);
        llContainer.addView(t);
    }
}
