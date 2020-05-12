package com.example.smarthelmet.Fragments;

import android.content.Intent;
import android.os.Bundle;
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

import static com.example.smarthelmet.Constants.BTCommandIntent;

public class SettingsFragment extends PreferenceFragmentCompat {

    EditTextPreference sendPreference;
    CheckBoxPreference receivePreference;


    Intent commandIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sendPreference = getPreferenceManager().findPreference("sendPreference");
        receivePreference = getPreferenceManager().findPreference("receivePreference");

        commandIntent = new Intent(BTCommandIntent);

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


        receivePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("receiveSettingsFragment", newValue.toString());

                return true;
            }
        });
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
