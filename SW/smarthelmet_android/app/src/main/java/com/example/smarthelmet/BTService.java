package com.example.smarthelmet;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.smarthelmet.Fragments.ConnectFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;

public class BTService extends Service {


    static boolean isConnected = false;
    ConnectedThread connectedThread;

    public static boolean getConnection() {
        return isConnected;
    }

    public static void setConnection(boolean updateConnected) {
        isConnected = updateConnected;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //Toast.makeText(this, "Service was Created", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connectedThread = new ConnectedThread(ConnectFragment.get_socket());
        connectedThread.start();


        String data = "hello";
        connectedThread.write(data.getBytes());

        isConnected = true;

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (connectedThread != null)
            connectedThread.cancel();

        super.onDestroy();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.available();
                    if (numBytes != 0) {

                        mmBuffer = new byte[1024];
                        SystemClock.sleep(25);
                        numBytes = mmInStream.available();

                        numBytes = mmInStream.read(mmBuffer, 0, numBytes);

                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < numBytes; i++) {
                            sb.append(String.format("%02X ", mmBuffer[i]));
                        }

                        Log.d(TAG, "datahex = " + sb);

                        String dataRx = new String(mmBuffer, 0, numBytes);
                        // Send the obtained bytes to the UI activity.
                        Intent intent = new Intent("BTEvent");
                        intent.putExtra("RXData", dataRx);

                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    }

                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                isConnected = false;
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}