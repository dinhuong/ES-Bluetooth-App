package com.example.h3o;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.CompoundButtonCompat;

import com.gelitenight.waveview.library.WaveView;

public class MainActivity extends AppCompatActivity {

    private WaveHelper mWaveHelper;
    private TextView tvLevel;
    private TextView tvTemp;
    private TextView tvVolume;

    private int mBorderColor;
    private int mBorderWidth = 10;
    private static float level=100f;
    private static float capacity = 500f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTemp = findViewById(R.id.tv_temp);
        tvVolume = findViewById(R.id.tv_volume);
        tvLevel = findViewById(R.id.tv_level);

        final WaveView waveView = (WaveView) findViewById(R.id.wave);
        mBorderColor = Color.parseColor("#44f16d7a");
        waveView.setBorder(mBorderWidth, mBorderColor);
        waveView.setShapeType(WaveView.ShapeType.CIRCLE);
        waveView.setWaveColor(
                Color.parseColor("#28f16d7a"),
                Color.parseColor("#3cf16d7a"));

        mWaveHelper = new WaveHelper(waveView, level, capacity);

        CompoundButtonCompat.setButtonTintList(
                (RadioButton) findViewById(R.id.colorRed),
                getResources().getColorStateList(R.color.red));
        CompoundButtonCompat.setButtonTintList(
                (RadioButton) findViewById(R.id.colorGreen),
                getResources().getColorStateList(R.color.green));
        CompoundButtonCompat.setButtonTintList(
                (RadioButton) findViewById(R.id.colorBlue),
                getResources().getColorStateList(R.color.blue));

        ((RadioGroup) findViewById(R.id.colorChoice))
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        switch (i) {
                            case R.id.colorRed:
                                waveView.setWaveColor(
                                        Color.parseColor("#28f16d7a"),
                                        Color.parseColor("#3cf16d7a"));
                                mBorderColor = Color.parseColor("#44f16d7a");
                                waveView.setBorder(mBorderWidth, mBorderColor);
                                break;
                            case R.id.colorGreen:
                                waveView.setWaveColor(
                                        Color.parseColor("#40b7d28d"),
                                        Color.parseColor("#80b7d28d"));
                                mBorderColor = Color.parseColor("#B0b7d28d");
                                waveView.setBorder(mBorderWidth, mBorderColor);
                                break;
                            case R.id.colorBlue:
                                waveView.setWaveColor(
                                        Color.parseColor("#88b8f1ed"),
                                        Color.parseColor("#b8f1ed"));
                                mBorderColor = Color.parseColor("#b8f1ed");
                                waveView.setBorder(mBorderWidth, mBorderColor);
                                break;
                        }
                    }
                });


    }

    @Override
    protected void onPause() {
        super.onPause();
        mWaveHelper.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWaveHelper.start();
    }

    public void update(View view) {
        Log.d("TAG", "update: "+ String.valueOf(level));
        level = 250f;
        mWaveHelper.update(level);
    }

    public void openAnalysis(View view) {
        Intent intent = new Intent(this, AnalysisActivity.class);
        startActivity(intent);
    }
}