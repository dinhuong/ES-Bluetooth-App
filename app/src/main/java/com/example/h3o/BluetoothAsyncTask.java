package com.example.h3o;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothAsyncTask extends AsyncTask<Void, String, String> {
    BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inStream;
    private String data="";

    private WeakReference<WaveHelper> mWaveHelper;
    private WeakReference<TextView> tvLevel;
    private WeakReference<TextView> tvTemp;
    private WeakReference<TextView> tvVolume;

    public BluetoothAsyncTask(BluetoothDevice device, UUID uuid, WaveHelper waveHelper, TextView temp, TextView level, TextView volume) {
        mWaveHelper = new WeakReference<>(waveHelper);
        tvTemp = new WeakReference<>(temp);
        tvLevel = new WeakReference<>(level);
        tvVolume = new WeakReference<>(volume);

        socket = null;
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            outputStream = socket.getOutputStream();
            inStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @Override
    protected String doInBackground(Void... voids) {
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

            publishProgress(message);
        }

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        String mess = values[0];
        // 123,456
        String[] datumArr = mess.split(",", 0);

        //update temperature
        float temp = Float.parseFloat(datumArr[0]);
        tvTemp.get().setText("Temperature: " + temp + "C");

        //update level
        float level = Float.parseFloat(datumArr[1]);
        if (MainActivity.level > level) {
            MainActivity.volume += (MainActivity.level - level);
            tvVolume.get().setText("Volume of today: " + MainActivity.volume + "ml");
        }
        MainActivity.level = level;
        mWaveHelper.get().update(level);
        tvLevel.get().setText(level + " ml");
    }
}
