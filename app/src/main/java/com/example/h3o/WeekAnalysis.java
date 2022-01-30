package com.example.h3o;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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

        entryList.add(new Entry(1, 620));
        entryList.add(new Entry(2, 720));
        entryList.add(new Entry(3, 277));
        entryList.add(new Entry(4, 444));
        entryList.add(new Entry(5, 822));
        entryList.add(new Entry(6, 177));
        entryList.add(new Entry(7, 221));
        entryList.add(new Entry(8, 315));
        entryList.add(new Entry(9, 729));
        LineDataSet lineDataSet = new LineDataSet(entryList, "water consumption (ml)");
        // set blue line
        lineDataSet.setColors(ColorTemplate.LIBERTY_COLORS);
        lineDataSet.setFillAlpha(150);
        lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.setVisibleYRange(0, 1200, YAxis.AxisDependency.LEFT);
        lineChart.invalidate();
    }
}