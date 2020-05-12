package com.example.smarthelmet.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smarthelmet.R;



public class SettingsFragment extends PreferenceFragmentCompat {

    EditTextPreference sendPreference;
    CheckBoxPreference receivePreference;
    PreferenceCategory generalCategory;
    PreferenceCategory sensorsCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sendPreference = (EditTextPreference) getPreferenceManager().findPreference("sendPreference");
        receivePreference = (CheckBoxPreference) getPreferenceManager().findPreference("receivePreference");

        generalCategory = getPreferenceManager().findPreference("generalCategory");
        sensorsCategory = getPreferenceManager().findPreference("sensorsCategory");


        sendPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("sendSettingsFragment", newValue.toString());

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
}
