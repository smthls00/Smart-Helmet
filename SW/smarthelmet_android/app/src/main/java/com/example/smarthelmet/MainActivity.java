package com.example.smarthelmet;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.smarthelmet.Fragments.ChatFragment;
import com.example.smarthelmet.Fragments.ConnectFragment;
import com.example.smarthelmet.Fragments.EnvironmentFragment;
import com.example.smarthelmet.Fragments.LogoFragment;
import com.example.smarthelmet.Fragments.SettingsFragment;
import com.example.smarthelmet.Fragments.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setVisibility(View.GONE);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorAccent));

        getSupportFragmentManager()
                .beginTransaction()
                //.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                //.replace(R.id.frame_container, new LogoFragment())
                .replace(R.id.frame_container, new ConnectFragment())
                .commitAllowingStateLoss();
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {

            Fragment fragment = null;
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);

            switch (item.getItemId()) {
                case R.id.connection:
                    if (!(currentFragment instanceof ConnectFragment)) {
                        fragment = new ConnectFragment();
                    }
                    break;

                case R.id.user:
                    if (!(currentFragment instanceof UserFragment)) {
                        fragment = new UserFragment();
                    }
                    break;

                case R.id.environment:
                    if (!(currentFragment instanceof EnvironmentFragment)) {
                        fragment = new EnvironmentFragment();
                    }
                    break;

                case R.id.chat:
                    if (!(currentFragment instanceof ChatFragment)) {
                        fragment = new ChatFragment();
                    }
                    break;

                case R.id.settings:
                    if (!(currentFragment instanceof SettingsFragment)) {
                        fragment = new SettingsFragment();
                    }
                    break;
            }
            if (fragment != null)
                getSupportFragmentManager()
                        .beginTransaction()
                        //.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.frame_container, fragment) // replace flContainer
                        .commitAllowingStateLoss();
            return true;
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
