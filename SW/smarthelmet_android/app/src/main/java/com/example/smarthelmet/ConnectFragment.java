package com.example.smarthelmet;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.IOException;

import static android.content.ContentValues.TAG;

public class ConnectFragment extends Fragment {


    private static final int REQUEST_ENABLE_BT = 1;
    public final String UUID = "";

    ProgressBar connectProgress;
    ProgressBar connectProgressInd;
    TextView connectText;
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket mmSocket;

    Handler scannerHandler;
    Runnable scannerRunnable;
    ConnectThread newConnection;

    IntentFilter scannerFilter;
    IntentFilter btFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Your smartphone doesn't support BT", Toast.LENGTH_SHORT).show();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        scannerFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        btFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

        getActivity().registerReceiver(scannerReceiver, scannerFilter);
        getActivity().registerReceiver(btReceiver, btFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_connect, container, false);

        connectText = view.findViewById(R.id.connectTv);
        connectProgress = view.findViewById(R.id.connectPb); // initiate the progress bar
        connectProgressInd = view.findViewById(R.id.connectIndPb); // initiate the progress bar

        connectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(getActivity(), "Please, turn on BT first", Toast.LENGTH_SHORT).show();
                } else {
                    connectProgressInd.setVisibility(View.VISIBLE);
                    connectText.setText("Looking \n for a helmet");
                    connectText.setClickable(false);

                    bluetoothAdapter.startDiscovery();

                scannerHandler = new Handler();
                scannerHandler.postDelayed(scannerRunnable = new Runnable() {
                    @Override
                    public void run() {
                            connectProgressInd.setVisibility(View.GONE);
                            connectText.setText("Tap to connect");
                            connectText.setClickable(true);

                            bluetoothAdapter.cancelDiscovery();
                            Toast.makeText(getActivity(),"Can't find a helmet", Toast.LENGTH_LONG).show();
                    }
                }, 15000);
                }
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStop() {
        if(newConnection != null)
            newConnection.cancel();

        scannerHandler.removeCallbacks(scannerRunnable);
        getActivity().unregisterReceiver(btReceiver);
        getActivity().unregisterReceiver(scannerReceiver);

        super.onStop();
    }

    @Override
    public void onStart() {
        if(newConnection != null && !newConnection.isAlive())
            newConnection.start();

        getActivity().registerReceiver(scannerReceiver, scannerFilter);
        getActivity().registerReceiver(btReceiver, btFilter);

        super.onStart();
    }


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

                Log.d("bluetooth device found", deviceName);
                if (deviceName.equals("SmartHelmet")) {

                    bluetoothAdapter.cancelDiscovery();
                    scannerHandler.removeCallbacks(scannerRunnable);

                    newConnection = new ConnectThread(device);
                    newConnection.start();
                }
            }
        }
    };

    // Create a BroadcastReceiver for BT_STATE_CHANGED.
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                Log.d("bluetooth device found","hello");
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:

                        //Indicates the local Bluetooth adapter is off.
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        //Indicates the local Bluetooth adapter is turning on. However local clients should wait for STATE_ON before attempting to use the adapter.
                        break;

                    case BluetoothAdapter.STATE_ON:
                        //Indicates the local Bluetooth adapter is on, and ready for use.
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        connectProgressInd.setVisibility(View.GONE);
                        connectText.setText("Tap to connect");
                        connectText.setClickable(true);

                        bluetoothAdapter.cancelDiscovery();
                        Toast.makeText(getContext(),"Can't find a helmet", Toast.LENGTH_LONG).show();

                        //Indicates the local Bluetooth adapter is turning off. Local clients should immediately attempt graceful disconnection of any remote links.
                        break;
                }
            }
        }
    };

    public BluetoothSocket get_socket() {
        return mmSocket;
    }

    public class ConnectThread extends Thread {

        private final BluetoothDevice mmDevice;

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
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.

            connectProgressInd.setVisibility(View.GONE);
            connectProgress.setProgress(100);
            connectText.setText("Connected");

            getActivity().startService(new Intent(getActivity(), BTService.class));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    connectText.setVisibility(View.GONE);
                    connectProgress.setVisibility(View.GONE);;

                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slide_down, R.anim.slide_up)
                            .replace(R.id.frame_container, new OverviewFragment()) // replace flContainer
                            .commit();
                }
            }, 2500);
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

}
