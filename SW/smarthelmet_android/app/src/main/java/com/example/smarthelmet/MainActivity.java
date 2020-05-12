package com.example.smarthelmet;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.smarthelmet.Fragments.ChatFragment;
import com.example.smarthelmet.Fragments.ConnectFragment;
import com.example.smarthelmet.Fragments.EnvironmentFragment;
import com.example.smarthelmet.Fragments.SettingsFragment;
import com.example.smarthelmet.Fragments.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.example.smarthelmet.Constants.chatFragmentTag;
import static com.example.smarthelmet.Constants.connectFragmentTag;
import static com.example.smarthelmet.Constants.environmentFragmentTag;
import static com.example.smarthelmet.Constants.settingsFragmentTag;
import static com.example.smarthelmet.Constants.userFragmentTag;

public class MainActivity extends AppCompatActivity {

    FragmentManager fragmentManager;

    private ConnectFragment connectFragment;
    private EnvironmentFragment environmentFragment;
    private UserFragment userFragment;
    private ChatFragment chatFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        connectFragment = new ConnectFragment();
        environmentFragment = new EnvironmentFragment();
        userFragment = new UserFragment();
        settingsFragment = new SettingsFragment();
        chatFragment = new ChatFragment();

        navigation.setVisibility(View.GONE);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorAccent));

        changeFragment(connectFragment, connectFragmentTag);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.connection:
                    changeFragment(connectFragment, connectFragmentTag);
                    break;

                case R.id.user:
                    changeFragment(userFragment, userFragmentTag);
                    break;

                case R.id.environment:
                    changeFragment(environmentFragment, environmentFragmentTag);
                    break;

                case R.id.chat:
                    changeFragment(chatFragment, chatFragmentTag);
                    break;

                case R.id.settings:
                    changeFragment(settingsFragment, settingsFragmentTag);
                    break;
            }

            return true;
        }
    };

    public void changeFragment(Fragment fragment, String tagFragmentName) {

        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        Fragment currentFragment = mFragmentManager.getPrimaryNavigationFragment();
        if (currentFragment != null) {
            fragmentTransaction.detach(currentFragment);
        }

        Fragment fragmentTemp = mFragmentManager.findFragmentByTag(tagFragmentName);
        if (fragmentTemp == null) {
            fragmentTemp = fragment;
            fragmentTransaction.add(R.id.frame_container, fragmentTemp, tagFragmentName);
        } else {
            fragmentTransaction.attach(fragmentTemp);
        }

        fragmentTransaction.setPrimaryNavigationFragment(fragmentTemp);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commitNowAllowingStateLoss();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
