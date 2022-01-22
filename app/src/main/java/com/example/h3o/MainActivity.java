package com.example.h3o;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Output;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING=2;
    static final int STATE_CONNECTED=3;
    static final int STATE_CONNECTION_FAILED=4;
    static final int STATE_MESSAGE_RECEIVED=5;

    int REQUEST_ENABLE_BLUETOOTH=1;

    private WaveHelper mWaveHelper;
    private TextView tvLevel;
    private TextView tvTemp;
    private TextView tvVolume;
    private TextView tvStatus;
    private ListView lvDevices;

    private int mBorderColor;
    private int mBorderWidth = 10;
    private static float level=100f;
    private static float capacity = 500f;
    private static float volume=0f;
    private static float temperature=28f;

    BluetoothDevice[] bluetoothDevices;

    private BluetoothAdapter adapter;
    BluetoothDevice myBluetooth = null;
    private String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTemp = findViewById(R.id.tv_temp);
        tvVolume = findViewById(R.id.tv_volume);
        tvLevel = findViewById(R.id.tv_level);
        tvStatus = findViewById(R.id.tv_status);
        lvDevices = findViewById(R.id.lv_devices);

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

    public void filter() {
        // temperature: 27.37ￂﾰC  //20 chars
        //Water remaining: 361.00 ml //24 chars

        String[] datumArr = data.split("\r\n", 0);
        for (int i=0; i<datumArr.length; i++) {
            String datum = datumArr[i];
            if (datum.contains("temperature") && datum.contains("ￂﾰC")) {
                float temp = Float.parseFloat(datum.split(" ")[1]);
                if (Math.abs(MainActivity.temperature - temp) > 1) {
                    updateTemp(temp);
                }
            } else if (datum.contains("Water remaining") && datum.contains("ml")) {
                float level = Float.parseFloat(datum.split(" ")[1]);
                if (Math.abs(MainActivity.level - level) > 1) {
                    updateLevel(level);
                }
            } else {
                data = data.substring(i*48);
            }
        }
    }

    public void openAnalysis(View view) {
        Intent intent = new Intent(this, AnalysisActivity.class);
        startActivity(intent);
    }

    public void getPairedDevice(View view) {
        if(!adapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
            tvStatus.setText("Listening");
        }
        tvStatus.setText("connecting");
            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
            String[] devicesName = new String[pairedDevices.size()];
            int index=0;

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    bluetoothDevices[index] = device;
                    devicesName[index]=device.getName();
                    index++;
                }
                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,devicesName);
                lvDevices.setAdapter(arrayAdapter);

                lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        ClientClass clientClass = new ClientClass(bluetoothDevices[i], bluetoothDevices[i].getUuids()[0].getUuid());
                        clientClass.start();

                        tvStatus.setText("Connecting");
                    }
                });
            }

//        if (adapter.isEnabled()) {
//            Toast.makeText(this, "get paired", Toast.LENGTH_SHORT).show();
//
//            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
//            if (pairedDevices.size() > 0) {
//                Log.d("tag", "getPairedDevice: " + pairedDevices.toString());
//
//                for (BluetoothDevice device : pairedDevices) {
//                    String deviceName = device.getName();
//                    Log.d("device", "getPairedDevice: " + device.getName());
//                    String deviceHardwareAddress = device.getAddress(); // MAC address
//                    Toast.makeText(this, device.getName(), Toast.LENGTH_SHORT).show();
//
//                    myBluetooth = (BluetoothDevice) device;
//                    ParcelUuid[] uuids = device.getUuids();
//
//                    ClientClass client = new ClientClass(myBluetooth, uuids[0].getUuid());
//                    client.start();
//
//                }
//            }
//        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case STATE_LISTENING:
                    tvStatus.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    tvStatus.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    tvStatus.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    tvStatus.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] buffer= (byte[]) msg.obj;
                    String tempMsg=new String(buffer,0,msg.arg1);
                    data += tempMsg;
                    filter();
                    break;
            }
            return true;
        }
    });

    private class ClientClass extends Thread
    {
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device, UUID uuid)
        {
            try {
                socket=device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            try {
                socket.connect();
                Message message=Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);

                DataTransfer dataTransfer = new DataTransfer(socket);
                dataTransfer.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message= Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class DataTransfer extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public DataTransfer (BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn=null;
            OutputStream tempOut=null;

            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
            outputStream=tempOut;
        }

        public void run() {
            final int BUFFER_SIZE = 1024;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes = 0;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}