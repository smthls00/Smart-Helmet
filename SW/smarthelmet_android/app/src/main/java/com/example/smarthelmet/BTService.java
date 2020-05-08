package com.example.smarthelmet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.smarthelmet.Fragments.ConnectFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;

public class BTService extends Service {

    private final String UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private final String BTScanIntent = "BTScanIntent";
    private final String BTStateIntent = "BTStateIntent";
    private final String BTDataIntent = "BTDataIntent";
    private final String connectIntent = "connectIntent";
    private final String BTOff = "BTOff";
    private final String BTOn = "BTOn";
    private final String scannerTimeOut = "TimeOut";
    public final String stopService = "stopServiceIntent";



    ConnectThread connectThread;
    RxTxThread rxtxThread;
    BluetoothAdapter bluetoothAdapter;
    IntentFilter btActionFilter;
    IntentFilter scannerFilter;
    IntentFilter btStateFilter;
    Intent btDataIntent;
    Intent btScanIntent;
    Intent btStateIntent;
    Handler scannerHandler;
    IntentFilter stopServerFilter;

    static boolean isConnected;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("BTService", "onCreate()");
        //Toast.makeText(this, "Service was Created", Toast.LENGTH_LONG).show();
        btDataIntent = new Intent(BTDataIntent);
        btStateIntent = new Intent(BTStateIntent);
        btScanIntent = new Intent(BTScanIntent);

        btActionFilter = new IntentFilter();
        btActionFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        btActionFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        btActionFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        scannerFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        btStateFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        stopServerFilter = new IntentFilter(stopService);

        scannerHandler = new Handler();

        isConnected = false;

        createNotificationDisconnectChannel();
        createNotificationFgChannel();

        makeForegroundNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        bluetoothAdapter = ConnectFragment.get_btAdapter();

        registerReceiver(scannerReceiver, scannerFilter);
        registerReceiver(btStateReceiver, btStateFilter);
        registerReceiver(btActionReceiver, btActionFilter);
        registerReceiver(serviceKillReceiver, stopServerFilter);

        scannerHandler.postDelayed(scannerRunnable, 5000);

        bluetoothAdapter.startDiscovery();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(connectReceiver,
                new IntentFilter(connectIntent));




        return START_STICKY;
    }

    private BroadcastReceiver serviceKillReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.d("btservice", "servicekiller");

            String message = intent.getStringExtra(stopService);

            if(message.equals("OK")){
                fgKill();
            }
        }
    };

    private BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.d("connectOnStop", intent.getAction());

            String message = intent.getStringExtra(connectIntent);

            if(message.equals("onStop")){
                if(bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                    scannerHandler.removeCallbacks(scannerRunnable);
                }
            }
        }
    };

    Runnable scannerRunnable = new Runnable() {
        @Override
        public void run() {

            Log.d("BTScan","TimeOutScan");
            bluetoothAdapter.cancelDiscovery();

            broadcastIntent(BTScanIntent, scannerTimeOut);
        }
    };

    // Create a BroadcastReceiver for BT_STATE_CHANGED.
    private final BroadcastReceiver btStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        //Indicates the local Bluetooth adapter is off.
                        Log.d("BTService", "STATE_OFF");
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        //Indicates the local Bluetooth adapter is turning on. However local clients should wait for STATE_ON before attempting to use the adapter.
                        Log.d("BTService", "STATE_TURNING_ON");
                        break;

                    case BluetoothAdapter.STATE_ON:
                        //Indicates the local Bluetooth adapter is on, and ready for use.
                        Log.d("BTService", "STATE_ON");
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //Indicates the local Bluetooth adapter is turning off. Local clients should immediately attempt graceful disconnection of any remote links.
                        Log.d("BTService", "STATE_TURNING_OFF");
                        bluetoothAdapter.cancelDiscovery();
                        if(isConnected){
                            makeDisconnectNotification();
                            isConnected = false;
                        }

                        broadcastIntent(BTStateIntent, BTOff);

                        fgKill();
                        break;
                }
            }
        }
    };

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver btActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    //Indicates the local Bluetooth adapter is off.
                    Log.d("BTService", "ACTION_FOUND");
                    break;

                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    //Indicates the local Bluetooth adapter is connected.
                    Log.d("BTService", "ACTION_ACL_CONNECTED");
                    broadcastIntent(BTStateIntent, BTOn);
                    isConnected = true;
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    //Indicates the local Bluetooth adapter is on, and ready for use.
                    Log.d("BTService", "ACTION_ACL_DISCONNECT_REQUESTED");
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    //Indicates the local Bluetooth adapter is turning off. Local clients should immediately attempt graceful disconnection of any remote links.
                    Log.d("BTService", "ACTION_ACL_DISCONNECTED");
                    broadcastIntent(BTStateIntent, BTOff);
                    isConnected = false;

                    makeDisconnectNotification();

                    fgKill();
                    break;
            }
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver scannerReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (deviceName != null) {
                    Log.d("BTService", deviceName);
                    if (deviceName.equals("ESP32_SmartHelmet")) {
                        bluetoothAdapter.cancelDiscovery();
                        scannerHandler.removeCallbacks(scannerRunnable);

                        connectThread = new ConnectThread(device);
                        connectThread.start();
                    }
                }
            }
        }
    };

    public void makeDisconnectNotification(){
        Intent notificationIntent = new Intent();
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "1")
                .setSmallIcon(R.drawable.ic_build_black_24dp)
                .setContentTitle("Smart Helmet Disconnected")
                .setContentText("You are not connected anymore")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    public void makeForegroundNotification(){
        Intent notificationIntent = new Intent(getApplicationContext(), ServiceKillNotificationHandler.class);
        notificationIntent.setAction("stop");

        PendingIntent pendingIntentKill = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification builder = new NotificationCompat.Builder(this, "0")
                .setSmallIcon(R.drawable.ic_build_black_24dp)
                .setContentTitle("Background data processing")
                .setContentText("Data is received and processed in background")
                .addAction(R.drawable.rectangle_button, "stop", pendingIntentKill)
                .setOngoing(true)
                .build();

        startForeground(1, builder);
    }


    public void fgKill(){
        if(isConnected){
            makeDisconnectNotification();
            isConnected = false;
        }

        broadcastIntent(BTStateIntent, BTOff);
        stopForeground(true);
        stopSelf();
    }

    public static boolean getConnectionStatus(){
        return isConnected;
    }


    @Override
    public void onDestroy() {

        Log.d("BTService", "onDestroy()");

        try {

            if (connectThread != null)
                connectThread.cancel();


            unregisterReceiver(scannerReceiver);
            unregisterReceiver(btStateReceiver);
            unregisterReceiver(btActionReceiver);
            unregisterReceiver(serviceKillReceiver);
        } catch (Exception e){
            Log.d("BTService", e.toString());
        }

        super.onDestroy();
    }


    public void broadcastIntent(String TAG, String data){
        String message = TAG;
        switch (TAG){
            case BTScanIntent:
                btScanIntent.putExtra(TAG, data);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(btScanIntent);
                break;
            case BTDataIntent:
                btDataIntent.putExtra(TAG, data);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(btDataIntent);
                break;
            case BTStateIntent:
                btStateIntent.putExtra(TAG, data);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(btStateIntent);
                break;
        }

    }

    private void createNotificationFgChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "FgNotifications";
            String description = "ForegroundServiceNOtification";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("0", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotificationDisconnectChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Disconnect";
            String description = "Disconnected";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public class ConnectThread extends Thread {

        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.d(TAG, "connection error");
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            rxtxThread = new RxTxThread(mmSocket);
            rxtxThread.start();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private class RxTxThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public RxTxThread(BluetoothSocket socket) {
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



            String data = "hello";
            write(data.getBytes());
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

                        // Send the obtained bytes to the UI activity.
                        String dataRx = new String(mmBuffer, 0, numBytes);
                        broadcastIntent(BTDataIntent, dataRx);
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
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}