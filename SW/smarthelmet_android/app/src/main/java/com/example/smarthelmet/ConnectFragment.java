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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(),"Your smartphone doesn't support BT", Toast.LENGTH_SHORT).show();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(receiver, filter);
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
                if(!bluetoothAdapter.isEnabled()){
                    Toast.makeText(getActivity(), "Please, turn on BT first", Toast.LENGTH_SHORT).show();
                }
                else {
                    connectProgressInd.setVisibility(View.VISIBLE);
                    connectText.setText("Looking for a helmet");
                }
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(receiver);
    }


    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                Log.d("bluetooth device found", deviceName);
                if(deviceName.equals("SmartHelmet")){
                    ConnectThread newConnection = new ConnectThread(device);
                    newConnection.start();
                }
            }
        }
    };

    public BluetoothSocket get_socket(){
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

            //manageMyConnectedSocket(mmSocket);
            connectProgress.setProgress(100);
            connectText.setText("Connected");

            getActivity().startService(new Intent(getActivity(), BTService.class));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    connectText.setVisibility(View.GONE);
                    connectProgress.setVisibility(View.GONE);
                    connectProgressInd.setVisibility(View.GONE);

                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slide_down, R.anim.slide_up)
                            .replace(R.id.frame_container, new OverviewFragment()) // replace flContainer
                            .commit();
                }
            }, 2000);
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
