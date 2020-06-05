package com.example.smarthelmet.Fragments;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.smarthelmet.BTService;
import com.example.smarthelmet.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.example.smarthelmet.Constants.BTOff;
import static com.example.smarthelmet.Constants.BTOn;
import static com.example.smarthelmet.Constants.BTScanIntent;
import static com.example.smarthelmet.Constants.BTStateIntent;
import static com.example.smarthelmet.Constants.connectIntent;
import static com.example.smarthelmet.Constants.onStopSearching;
import static com.example.smarthelmet.Constants.scannerTimeOut;

public class ConnectFragment extends Fragment {


    private static final int REQUEST_ENABLE_BT = 3;
    private static final int ACTION_REQUEST_MULTIPLE_PERMISSION = 2;

    int REQUEST_ACCESS_COARSE_LOCATION = 1;
    ProgressBar connectProgress;
    ProgressBar connectProgressInd;
    TextView connectText;
    Intent connectFragmentIntent;
    static BluetoothAdapter bluetoothAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectFragmentIntent = new Intent(connectIntent);

        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), R.string.notSupported, Toast.LENGTH_SHORT).show();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        int pCheck = getActivity().checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        pCheck += getActivity().checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        pCheck += getActivity().checkSelfPermission("Manifest.permission.BLUETOOTH_ADMIN");
        pCheck += getActivity().checkSelfPermission("Manifest.permission.BLUETOOTH");
        pCheck += getActivity().checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
        pCheck += getActivity().checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
        if (pCheck != 0) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}
                    , ACTION_REQUEST_MULTIPLE_PERMISSION);
        }
//        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
//        if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
//            new AlertDialog.Builder(getContext())
//                    .setTitle("Location must be enabled for BT purposes")  // GPS not found
//                    .setMessage("Do you want to enable it?") // Want to enable?
//                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            getActivity().startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                        }
//                    })
//                    .setNegativeButton("No", null)
//                    .show();
//        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(btScanReceiver,
                new IntentFilter(BTScanIntent));

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(btStateReceiver,
                new IntentFilter(BTStateIntent));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_connect, container, false);

        BottomNavigationView navigation = getActivity().findViewById(R.id.navigation);
        navigation.setVisibility(View.VISIBLE);

        connectText = view.findViewById(R.id.connectTv);
        connectProgress = view.findViewById(R.id.connectPb); // initiate the progress bar
        connectProgressInd = view.findViewById(R.id.connectIndPb); // initiate the progress bar

        Log.d("ConnectFragment", "onCreateView");

        connectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(getActivity(), R.string.btOnPls, Toast.LENGTH_SHORT).show();
                } else {
                    getActivity().startForegroundService(new Intent(getActivity(), BTService.class));

                    connectProgressInd.setVisibility(View.VISIBLE);
                    connectText.setText(R.string.searching);
                    connectText.setClickable(false);
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }


    private BroadcastReceiver btScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.d("ConnectBTEvent", intent.getAction());

            String message = intent.getStringExtra(BTScanIntent);

            if (message.equals(scannerTimeOut)) {
                connectProgressInd.setVisibility(View.GONE);
                connectProgress.setProgress(0);
                connectText.setText(R.string.connect);
                connectText.setClickable(true);

                if (getContext() != null)

                    Toast.makeText(getContext(), R.string.cantFind, Toast.LENGTH_LONG).show();
            }
        }
    };

    private BroadcastReceiver btStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.d("ConnectBTEvent", intent.getAction());

            String message = intent.getStringExtra(BTStateIntent);

            connectProgressInd.setVisibility(View.GONE);

            if (message.equals(BTOff)) {
                connectProgress.setProgress(0);
                connectText.setText(R.string.connect);
                connectText.setClickable(true);
            } else if (message.equals(BTOn)) {
                connectProgress.setProgress(100);
                connectText.setText(R.string.connected);
                connectText.setClickable(false);
            }
        }
    };

    @Override
    public void onStop() {
        Log.d("ConnectFragment", "OnStop");

        super.onStop();
    }

    @Override
    public void onStart() {
        Log.d("ConnectFragment", "OnStart");

        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d("ConnectFragment", "OnStart");

        if (BTService.getConnectionStatus()) {
            connectProgress.setProgress(100);
            connectText.setText(R.string.connected);
            connectText.setClickable(false);
        } else {
            connectProgress.setProgress(0);
            connectText.setText(R.string.connect);
            connectText.setClickable(true);
        }

        super.onResume();
    }

    @Override
    public void onPause() {

        connectFragmentIntent.putExtra(connectIntent, onStopSearching);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(connectFragmentIntent);

        super.onPause();
    }
}
