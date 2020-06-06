package com.example.smarthelmet.Fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthelmet.BTService;
import com.example.smarthelmet.R;
import com.jjoe64.graphview.series.DataPoint;

import java.io.File;

import static com.example.smarthelmet.Constants.*;

public class SettingsFragment extends PreferenceFragmentCompat {

    EditTextPreference sendPreference;
    EditTextPreference forwardDistancePreference;
    EditTextPreference backwardDistancePreference;
    EditTextPreference batteryPreference;

    CheckBoxPreference receivePreference;
    CheckBoxPreference dataLogPreference;
    CheckBoxPreference thresholdNotificationPreference;

    Preference exportPreference;
    Preference leftPreference;
    Preference rightPreference;


    EditTextPreference bpmPreference;
    EditTextPreference utpPreference;
    EditTextPreference gasPreference;
    EditTextPreference co2Preference;
    EditTextPreference smokePreference;
    EditTextPreference otpPreference;
    EditTextPreference prsPreference;
    EditTextPreference humPreference;
    EditTextPreference altPreference;
    EditTextPreference coPreference;
    EditTextPreference tvocPreference;
    EditTextPreference lpgPreference;

    Intent commandIntent;
    Intent dataLogIntent;
    Intent receiveIntent;
    Intent usrIntent;
    Intent envIntent;
    Intent thresholdIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sendPreference = getPreferenceManager().findPreference(getResources().getString(R.string.sendPreference));
        receivePreference = getPreferenceManager().findPreference(getResources().getString(R.string.receivePreference));
        dataLogPreference = getPreferenceManager().findPreference(getResources().getString(R.string.loggingPreference));
        exportPreference = getPreferenceManager().findPreference(getResources().getString(R.string.exportPreference));
        forwardDistancePreference = getPreferenceManager().findPreference(getResources().getString(R.string.forwardDistancePreference));
        backwardDistancePreference = getPreferenceManager().findPreference(getResources().getString(R.string.backwardDistancePreference));
        thresholdNotificationPreference = getPreferenceManager().findPreference(getResources().getString(R.string.thresholdNotificationPreference));
        leftPreference = getPreferenceManager().findPreference(getResources().getString(R.string.leftPreference));
        rightPreference = getPreferenceManager().findPreference(getResources().getString(R.string.rightPreference));
        batteryPreference = getPreferenceManager().findPreference(getResources().getString(R.string.batteryPreference));

        bpmPreference = getPreferenceManager().findPreference(getResources().getString(R.string.bpmPreference));
        utpPreference = getPreferenceManager().findPreference(getResources().getString(R.string.utpPreference));
        gasPreference = getPreferenceManager().findPreference(getResources().getString(R.string.gasPreference));
        co2Preference = getPreferenceManager().findPreference(getResources().getString(R.string.co2Preference));
        smokePreference = getPreferenceManager().findPreference(getResources().getString(R.string.smokePreference));
        otpPreference = getPreferenceManager().findPreference(getResources().getString(R.string.otpPreference));
        prsPreference = getPreferenceManager().findPreference(getResources().getString(R.string.pressurePreference));
        humPreference = getPreferenceManager().findPreference(getResources().getString(R.string.humidityPreference));
        altPreference = getPreferenceManager().findPreference(getResources().getString(R.string.altitudePreference));
        coPreference = getPreferenceManager().findPreference(getResources().getString(R.string.coPreference));
        tvocPreference = getPreferenceManager().findPreference(getResources().getString(R.string.tvocPreference));
        lpgPreference = getPreferenceManager().findPreference(getResources().getString(R.string.lpgPreference));

        commandIntent = new Intent(BTCommandIntent);
        dataLogIntent = new Intent(BTDataLogIntent);
        receiveIntent = new Intent(BTDataReceiveIntent);
        usrIntent = new Intent(usrPreferenceIntent);
        envIntent = new Intent(envPreferenceIntent);
        thresholdIntent = new Intent(BTThresholdNotificationIntent);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(BTDataReceiver,
                new IntentFilter(BTDataIntent));

        sendPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("sendSettingsFragment", newValue.toString());

                if (BTService.getConnectionStatus()) {
                    commandIntent.putExtra(BTCommandIntent, newValue.toString());
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(commandIntent);
                } else
                    Toast.makeText(getContext(), R.string.connectFirst, Toast.LENGTH_SHORT).show();


                return false;
            }
        });


        leftPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d("leftPreference", "leftPreferenceClick");

                if (BTService.getConnectionStatus()) {
                    commandIntent.putExtra(BTCommandIntent, leftBTCommand);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(commandIntent);
                } else
                    Toast.makeText(getContext(), R.string.connectFirst, Toast.LENGTH_SHORT).show();


                return false;
            }
        });


        rightPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d("rightPreference", "rightPreference");

                if (BTService.getConnectionStatus()) {
                    commandIntent.putExtra(BTCommandIntent, rightBTCommand);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(commandIntent);
                } else
                    Toast.makeText(getContext(), R.string.connectFirst, Toast.LENGTH_SHORT).show();


                return false;
            }
        });

        dataLogPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("dataLogSettingsFragment", newValue.toString());
                dataLogIntent.putExtra(BTDataLogIntent, (boolean) newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(dataLogIntent);

                return true;
            }
        });

        thresholdNotificationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                thresholdIntent.putExtra(BTThresholdNotificationIntent, (boolean) newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(thresholdIntent);

                return true;
            }
        });


        exportPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d("dataLogSettingsFragment", "exportPreference");
                Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                File logFile = new File(logDataPath, logFileName);

                if (logFile.exists()) {
                    dataLogPreference.setChecked(false);
                    dataLogIntent.putExtra(BTDataLogIntent, false);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(dataLogIntent);

                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    intentShareFile.setType("application/pdf");
                    intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + logFile.getAbsolutePath()));

                    intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                            "Sharing File...");
                    intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

                    startActivityForResult(Intent.createChooser(intentShareFile, "Share File"), 228);
                } else
                    Toast.makeText(getContext(), "No logged data found", Toast.LENGTH_SHORT).show();


                return true;
            }
        });


        forwardDistancePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (BTService.getConnectionStatus()) {
                    commandIntent.putExtra(BTCommandIntent, forwardDistanceBTCommand + newValue.toString() + "\n");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(commandIntent);
                } else
                    Toast.makeText(getContext(), R.string.connectFirst, Toast.LENGTH_SHORT).show();

                return true;
            }
        });

        bpmPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                usrIntent.putExtra(usrPreferenceIntent, bpmCommand + newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(usrIntent);
                return true;
            }
        });

        utpPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                usrIntent.putExtra(usrPreferenceIntent, utpCommand + newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(usrIntent);
                return true;
            }
        });

        gasPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                envIntent.putExtra(envPreferenceIntent, gasCommand + newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(envIntent);
                return true;
            }
        });

        co2Preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                envIntent.putExtra(envPreferenceIntent, co2Command + newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(envIntent);
                return true;
            }
        });

        smokePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                envIntent.putExtra(envPreferenceIntent, smkCommand + newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(envIntent);
                return true;
            }
        });

        otpPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                envIntent.putExtra(envPreferenceIntent, otpCommand + newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(envIntent);
                return true;
            }
        });

        prsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                envIntent.putExtra(envPreferenceIntent, prsCommand + newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(envIntent);
                return true;
            }
        });

        humPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                envIntent.putExtra(envPreferenceIntent, humCommand + newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(envIntent);
                return true;
            }
        });

        altPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                envIntent.putExtra(envPreferenceIntent, altCommand + newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(envIntent);
                return true;
            }
        });

        coPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                envIntent.putExtra(envPreferenceIntent, coCommand + newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(envIntent);
                return true;
            }
        });

        tvocPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                envIntent.putExtra(envPreferenceIntent, tvocCommand + newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(envIntent);
                return true;
            }
        });

        lpgPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                envIntent.putExtra(envPreferenceIntent, lpgCommand + newValue);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(envIntent);
                return true;
            }
        });
}

    private BroadcastReceiver BTDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(BTBatteryIntent);

            if (message == null)
                return;

            Log.d("batteryIntent", "message: " + message);

            try {

                float batteryLvl = Float.parseFloat(message.substring(1));

                batteryLvl = (float)(batteryLvl / 1023 * 1.1 * 2 * 3.3);

                Log.d("batteryIntent", "voltage: " + batteryLvl);
                batteryLvl = (int)(batteryLvl * 100 / 3.8);

                Log.d("batteryIntent", "percentage: " + batteryLvl);


                if(batteryLvl > 100)
                    batteryLvl = 100;

                batteryPreference.setTitle(getResources().getString(R.string.batteryLevel) + " " + (int)batteryLvl);

            } catch (Exception e) {
                Log.d("settingsFragmentBattery", e.toString());
            }


        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 228) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Clear logs")
                    .setMessage("Do you want to clear logs data?")

                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            File logFile = new File(logDataPath, logFileName);
                            if (logFile.exists())
                                logFile.delete();
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // do not change current paddings of start, end, and bottom
        // increase top padding by precomputed pixels
        final RecyclerView rv = getListView(); // This holds the PreferenceScreen's items
        rv.setPadding(0, 0, 0, 150); // (left, top, right, bottom)

        super.onViewCreated(view, savedInstanceState);
    }
}
