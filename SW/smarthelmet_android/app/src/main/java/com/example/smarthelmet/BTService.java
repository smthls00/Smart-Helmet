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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;
import static com.example.smarthelmet.Constants.*;

public class BTService extends Service {

    private static final String UUID = "00001101-0000-1000-8000-00805F9B34FB";

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

    String lastWarningMessage;

    static boolean isConnected;

    private String channelID = "0";

    boolean logFlag;
    boolean thresholdNotificationFlag;

//    long envTime;
//    long usrTime;
//    long lastEnvTime;
//    long lastUsrTime;
//    int envCounter;
//    int usrCounter;
//    float envAverageTime;
//    float usrAverageTime;

    FirebaseDatabase database;
    DatabaseReference databaseReference;
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

        database = FirebaseDatabase.getInstance();

        lastWarningMessage = "someText";

        logFlag = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.loggingPreference), false);
        thresholdNotificationFlag = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.thresholdNotificationPreference), true);

        createNotificationDisconnectChannel();
        createNotificationFgChannel();
        createNotificationWarningChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        registerReceiver(scannerReceiver, scannerFilter);
        registerReceiver(btStateReceiver, btStateFilter);
        registerReceiver(btActionReceiver, btActionFilter);
        registerReceiver(serviceKillReceiver, stopServerFilter);

        int scannerDelay = 15000;

        scannerHandler.postDelayed(scannerRunnable, scannerDelay);

        bluetoothAdapter.startDiscovery();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(connectReceiver,
                new IntentFilter(connectIntent));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(commandReceiver,
                new IntentFilter(BTCommandIntent));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dataLogReceiver,
                new IntentFilter(BTDataLogIntent));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dataReceiveReceiver,
                new IntentFilter(BTDataReceiveIntent));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(warningReceiver,
                new IntentFilter(BTWarningIntent));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(thresholdReceiver,
                new IntentFilter(BTThresholdNotificationIntent));


        makeForegroundNotification();
        return START_STICKY;
    }

    private BroadcastReceiver serviceKillReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.d("btService", "serviceKiller");

            String message = intent.getStringExtra(stopService);

            if(message == null)
                return;

            if (message.equals(stopOKService)) {
                fgKill();
            }
        }
    };

    private BroadcastReceiver thresholdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.d("btService", "thresholdReceiver");

            thresholdNotificationFlag = intent.getBooleanExtra(BTThresholdNotificationIntent, true);
        }
    };

    private BroadcastReceiver dataReceiveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            boolean receivePreference = intent.getBooleanExtra(BTDataReceiveIntent, false);

            Log.d("btDataReceiverReceiver", "flag" + receivePreference);

            if (!receivePreference) {
                writeBTMessage(stopBTCommand);
            } else {
                writeBTMessage(continueBTCommand);
            }
        }
    };



    private BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.d("connectOnStop", intent.getAction());

            String message = intent.getStringExtra(connectIntent);

            if(message == null)
                return;

            if (message.equals(onStopSearching)) {
                if (!isConnected) {
                    Log.d("stopDiscoveringOnStop", intent.getAction());

                    bluetoothAdapter.cancelDiscovery();
                    scannerHandler.removeCallbacks(scannerRunnable);

                    fgKill();
                }
            }
        }
    };

    private BroadcastReceiver warningReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent

            String message = intent.getStringExtra(BTWarningIntent);

            if(message == null || message.equals(lastWarningMessage) || !thresholdNotificationFlag)
                return;

            lastWarningMessage = message;

            Log.d("warningReceiver ", message);

            makeWarningNotification(message);

            writeBTMessage(vibrateBTCommand);
        }
    };


    private void writeBTMessage(String message){
        if (isConnected && (rxtxThread != null)) {
            rxtxThread.write(message.getBytes());
        }
    }

    private BroadcastReceiver commandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent

            String message = intent.getStringExtra(BTCommandIntent);

            if (message == null)
                return;

            writeBTMessage(message);
        }
    };

    private BroadcastReceiver dataLogReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent

            logFlag = intent.getBooleanExtra(BTDataLogIntent, false);
        }
    };


    private void appendLogsToFile(String logs) {
        File logFile = new File(logDataPath, logFileName);

        try {

            if (!logFile.exists()) {
                Log.d("btService", "fileNotThere");
                logFile.createNewFile();
            }

            BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile, true));
            logWriter.append(logs);
            logWriter.newLine();
            logWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Runnable scannerRunnable = new Runnable() {
        @Override
        public void run() {

            Log.d("BTScan", "TimeOutScan");
            bluetoothAdapter.cancelDiscovery();

            broadcastIntent(BTScanIntent, scannerTimeOut);
            fgKill();
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
                        if (isConnected) {
                            makeDisconnectNotification();
                            isConnected = false;

                            broadcastIntent(BTStateIntent, BTOff);

                            fgKill();
                        }
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
                    if (!isConnected) {
                        broadcastIntent(BTStateIntent, BTOn);
                        isConnected = true;
                    }
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    //Indicates the local Bluetooth adapter is on, and ready for use.
                    Log.d("BTService", "ACTION_ACL_DISCONNECT_REQUESTED");
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    //Indicates the local Bluetooth adapter is turning off. Local clients should immediately attempt graceful disconnection of any remote links.
                    Log.d("BTService", "ACTION_ACL_DISCONNECTED");
                    if (isConnected) {
                        broadcastIntent(BTStateIntent, BTOff);
                        isConnected = false;

                        makeDisconnectNotification();

                        fgKill();
                    }
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
                    if (deviceName.equals(BTdeviceName)) {
                        bluetoothAdapter.cancelDiscovery();
                        scannerHandler.removeCallbacks(scannerRunnable);

                        connectThread = new ConnectThread(device);
                        connectThread.start();
                    }
                }
            }
        }
    };

    public void makeDisconnectNotification() {
        Intent notificationIntent = new Intent();
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID + 1)
                .setSmallIcon(R.drawable.ic_build_black_24dp)
                .setContentTitle(getResources().getString(R.string.disconnected))
                .setContentText(getResources().getString(R.string.notConnected))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    public void makeWarningNotification(String message) {
        Intent notificationIntent = new Intent();
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID + 2)
                .setSmallIcon(R.drawable.ic_build_black_24dp)
                .setContentTitle(getResources().getString(R.string.warning))
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    public void makeForegroundNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), ServiceKillNotificationHandler.class);
        notificationIntent.setAction(getResources().getString(R.string.serviceStop));

        PendingIntent pendingIntentKill = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification builder = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.ic_build_black_24dp)
                .setContentTitle(getResources().getString(R.string.backgroundProcessing))
                .setContentText(getResources().getString(R.string.backgroundData))
                .addAction(R.drawable.rectangle_button, getResources().getString(R.string.serviceStop), pendingIntentKill)
                .setOngoing(true)
                .build();

        startForeground(1, builder);
    }


    public void fgKill() {
        if (isConnected) {
            makeDisconnectNotification();
            isConnected = false;
        }

        broadcastIntent(BTStateIntent, BTOff);
        stopForeground(true);
        stopSelf();
    }

    public static boolean getConnectionStatus() {
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
        } catch (Exception e) {
            Log.d("BTService", e.toString());
        }

        super.onDestroy();
    }


    public void broadcastIntent(String TAG, String data) {
        switch (TAG) {
            case BTUserIntent:
            case BTBatteryIntent:
            case BTEnvIntent:
                btDataIntent.putExtra(TAG, data);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(btDataIntent);
                break;
            case BTScanIntent:
                btScanIntent.putExtra(TAG, data);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(btScanIntent);
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
            CharSequence name = getResources().getString(R.string.backgroundProcessing);
            String description = getResources().getString(R.string.backgroundData);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(channelID, name, importance);
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
            CharSequence name = getResources().getString(R.string.disconnected);
            String description = getResources().getString(R.string.disconnected);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelID + 1, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotificationWarningChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getResources().getString(R.string.warning);
            String description = getResources().getString(R.string.warning);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelID + 2, name, importance);
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
            if (!isConnected) {
                broadcastIntent(BTStateIntent, BTOn);
                isConnected = true;
            }
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

            boolean receivePreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("receivePreference", false);
            Log.d(TAG, "shittyshit" + receivePreference);
            if (!receivePreference) {
                writeBTMessage(stopBTCommand);
            } else {
                writeBTMessage(continueBTCommand);
            }
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
                        //SystemClock.sleep(25);

                        numBytes = mmInStream.read(mmBuffer, 0, numBytes);

                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < numBytes; i++) {
                            if (mmBuffer[i] == 0x00)
                                break;
                            sb.append(String.format("%02X ", mmBuffer[i]));
                        }

                        //Log.d(TAG, "dataHex = " + sb + ", length = " + numBytes);

                        // Send the obtained bytes to the UI activity.
                        String dataRx = new String(mmBuffer, 0, numBytes);

                        //Log.d(TAG, "DataRx = " + dataRx);

//                        if(usrCounter == 10){
//                            Log.d(TAG, "TIMEDELTAUSR = " + usrAverageTime / 10);
//                        }
//
//                        if(envCounter == 10){
//                            Log.d(TAG, "TIMEDELTAENV = " + envAverageTime / 10);
//                        }


                        if (logFlag) {
                            Log.d(TAG, "logFlag = " + logFlag);
                            appendLogsToFile(dataRx);
                        }

                        if(dataRx.charAt(0) == batteryCommand.charAt(0)){
                            broadcastIntent(BTBatteryIntent, dataRx);

                            Log.d(TAG, "battery = " + dataRx);
                        }
                        else if (dataRx.charAt(0) == co2Command.charAt(0)) {
                            broadcastIntent(BTEnvIntent, dataRx);

                            Log.d(TAG, "envData = " + dataRx);

                            databaseReference = database.getReference("Environment");
                            databaseReference.setValue(dataRx);

//                            envCounter++;
//
//                            if(lastEnvTime != 0){
//                                envTime = (int)((System.nanoTime() - lastEnvTime) / 1000000);
//                                envAverageTime += envTime;
//                            }
//
//                            lastEnvTime = System.nanoTime();
                        } else if (dataRx.charAt(0) == bpmCommand.charAt(0)) {
                            broadcastIntent(BTUserIntent, dataRx);

                            Log.d(TAG, "userData = " + dataRx);

                            databaseReference = database.getReference("User");
                            databaseReference.setValue(dataRx);

//                            usrCounter++;
//
//                            if(lastUsrTime != 0){
//                                usrTime = (int)((System.nanoTime() - lastUsrTime) / 1000000);
//                                usrAverageTime += usrTime;
//                            }
//
//                            lastUsrTime = System.nanoTime();
                        }

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
    }
}