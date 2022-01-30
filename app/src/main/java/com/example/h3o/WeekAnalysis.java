package com.example.h3o;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class WeekAnalysis extends AppCompatActivity {

    private LineChart lineChart;
    List<Entry> entryList = new ArrayList<>();
    LineData lineData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_analysis);

        lineChart = findViewById(R.id.activity_main_linechart);

        entryList.add(new Entry(1, 120));
        entryList.add(new Entry(2, 220));
        entryList.add(new Entry(3, 311));
        entryList.add(new Entry(4, 514));
        entryList.add(new Entry(5, 620));
        entryList.add(new Entry(6, 910));
        entryList.add(new Entry(7, 1131));
        LineDataSet lineDataSet = new LineDataSet(entryList, "water consumption (ml)");
        lineDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        lineDataSet.setFillAlpha(150);
        lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.setVisibleXRangeMaximum(5000);
        lineChart.invalidate();
    }
}