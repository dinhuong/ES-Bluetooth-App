package com.example.h3o;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.CompoundButtonCompat;

import com.gelitenight.waveview.library.WaveView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private WaveHelper mWaveHelper;
    private TextView tvLevel;
    private TextView tvTemp;
    private TextView tvVolume;
    private TextView tvPaired;

    private int mBorderColor;
    private int mBorderWidth = 10;
    private static float level=100f;
    private static float capacity = 500f;

    private BluetoothAdapter adapter;
    private OutputStream outputStream;
    private InputStream inStream;
    BluetoothDevice myBluetooth = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    public void getPairedDevice(View view) {
        if (adapter.isEnabled()) {
            Toast.makeText(this, "get paired", Toast.LENGTH_SHORT).show();
//            Set<BluetoothDevice> devices = adapter.getBondedDevices();
//            for (BluetoothDevice device : devices){
//                //tvPaired.append(device.getName()+"/n");z
//                Toast.makeText(this, device.getName(), Toast.LENGTH_SHORT).show();
//            }
            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                Log.d("tag", "getPairedDevice: " + pairedDevices.toString());
                //Toast.makeText(this, pairedDevices.size(), Toast.LENGTH_SHORT).show();
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    Log.d("device", "getPairedDevice: " + device.getName());
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Toast.makeText(this, device.getName(), Toast.LENGTH_SHORT).show();
                    myBluetooth = (BluetoothDevice) device;
                    ParcelUuid[] uuids = device.getUuids();
                    BluetoothSocket socket = null;
                    try {
                        socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());

                        socket.connect();
                        outputStream = socket.getOutputStream();
                        inStream = socket.getInputStream();
                        run();
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

    public void run() {
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        int b = BUFFER_SIZE;

        while (true) {
            try {
                int bytes = 0;
                bytes = inStream.read(buffer, 0, BUFFER_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // temperature: 27.37ￂﾰC
            //Water remaining: 361.00 ml
            //temperature: 29.81ￂﾰC
            //Water remaining: 384.02 ml
            //temperature: 27.37ￂﾰC
            //Water remaining: 364.29 ml
            //temperature: 27.37ￂﾰC
            //Water remaining: 367.58 ml
            //temperature: 27.86ￂﾰC
            //Water remaining: 361.00 ml
            //temperature: 27.37ￂﾰC
            //Water remaining: 384.02 ml
            //temperature: 26.88ￂﾰC
            //Water remaining: 361.00 ml
            //temperature: 26.39ￂﾰC
            //Water remaining: 357.72 ml
            //temperature: 25.42ￂﾰC
            //Water remaining: 357.72 ml
            //temperature: 26.88ￂﾰC
            //Water remaining: 384.02 ml
            String message = convert_to_string(buffer);
            Log.d("byte_", "run: " + message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}