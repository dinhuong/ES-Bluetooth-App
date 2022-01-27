package com.example.h3o;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.CompoundButtonCompat;

import com.gelitenight.waveview.library.WaveView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private WaveHelper mWaveHelper;
    private TextView tvLevel;
    private TextView tvTemp;
    private TextView tvVolume;
    private TextView tvPaired;

    private int mBorderColor;
    private int mBorderWidth = 10;
    public static float level=100f;
    public static float capacity = 500f;
    public static float temperature=28f;
    public static float volume=0f;

    private String data="";

    private BluetoothAdapter adapter;
    private OutputStream outputStream;
    private InputStream inStream;
    BluetoothDevice myBluetooth = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            level = savedInstanceState.getFloat("level");
            temperature = savedInstanceState.getFloat("temp");
            tvTemp.setText("Temperature: " + temperature);
        }
        else {
            tvTemp.setText("Temperature: ");

        }
        tvTemp = findViewById(R.id.tv_temp);
        tvVolume = findViewById(R.id.tv_volume);
        tvLevel = findViewById(R.id.tv_level);

        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(this, "bluetooth is not available", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "bluetooth is available", Toast.LENGTH_SHORT).show();
        }

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
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putFloat("temp", temperature);
        outState.putFloat("level", level);
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

    public void updateLevel(float level) {
        Log.d("TAG", "update: "+ String.valueOf(level));
        if (MainActivity.level > level) {
            MainActivity.volume += (MainActivity.level - level);
            tvVolume.setText("Volume of today: " + MainActivity.volume + "ml");
        }
        MainActivity.level = level;
        mWaveHelper.update(level);
        tvLevel.setText(level + " ml");
    }

    public void updateTemp(float temperature) {
        MainActivity.temperature = temperature;
        tvTemp.setText("Temperature: " + temperature + "C");
    }

    public void filter(String mess) {
        // 123,456
        String[] datumArr = mess.split(",", 0);
        tvTemp.setText(mess);
        float temp = Float.parseFloat(datumArr[0]);
        updateTemp(temp);
        float level = Float.parseFloat(datumArr[1]);
        updateLevel(level);
    }

    public void openAnalysis(View view) {
        Intent intent = new Intent(this, AnalysisActivity.class);
        startActivity(intent);
    }

    public void getPairedDevice(View view) {
        if (adapter.isEnabled()) {
            Toast.makeText(this, "get paired", Toast.LENGTH_SHORT).show();

            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                Log.d("tag", "getPairedDevice: " + pairedDevices.toString());

                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    Log.d("device", "getPairedDevice: " + device.getName());
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Toast.makeText(this, device.getName(), Toast.LENGTH_SHORT).show();
                    myBluetooth = (BluetoothDevice) device;
                    ParcelUuid[] uuids = device.getUuids();
//                    BluetoothSocket socket = null;
//                    try {
//                        socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
//                        socket.connect();
//                        outputStream = socket.getOutputStream();
//                        inStream = socket.getInputStream();
//                        run();
//                        break;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    new BluetoothAsyncTask(device, uuids[0].getUuid(), mWaveHelper, tvTemp, tvLevel, tvVolume).execute();
                    break;
                }
            }
        }
    }
    public void write(String s) throws IOException {
        outputStream.write(s.getBytes());
    }

    public String convert_to_string(byte[] buffer){
        String s = "";
        for (byte _byte: buffer){
            s = s + (char) _byte;
        }
        return s;
    }

    public String get_message(String text){
        int num_pos = 0;
        ArrayList<Integer> positions = new ArrayList<Integer>();
        int position = text.indexOf("\n", 0);

        while (position != -1) {
            positions.add(position);
            num_pos += 1;
            position = text.indexOf("\n", position + 1);
        }
        String clean_message = "";
        if (num_pos < 2){
            return "";
        }
        int start_pos = positions.get(num_pos - 2) + 1;
        int end_pos = positions.get(num_pos - 1) - 1;

        for (int i = start_pos; i <= end_pos; i++) {
            clean_message += text.charAt(i);
        }
        return clean_message;
    }

    public void run() {
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];

        while (true) {
            try {
                int bytes = 0;
                bytes = inStream.read(buffer, 0, BUFFER_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String message = convert_to_string(buffer).trim();
            data += message;

            message = get_message(data);

            if (message == ""){
                continue;
            }

            Log.d("byte_", "run: " + data);
//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            filter(message);
            break;
        }
    }
}