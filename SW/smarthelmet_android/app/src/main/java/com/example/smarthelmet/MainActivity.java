package com.example.smarthelmet;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
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
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.connection:
                    getSupportFragmentManager()
                            .beginTransaction()
                            //.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            .replace(R.id.frame_container, new ConnectFragment()) // replace flContainer
                            .commit();
                    return true;

                case R.id.user:
                    getSupportFragmentManager()
                            .beginTransaction()
                            //.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            .replace(R.id.frame_container, new UserFragment()) // replace flContainer
                            .commit();
                    return true;

                case R.id.settings:
                    getSupportFragmentManager()
                            .beginTransaction()
                            //.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            .replace(R.id.frame_container, new SettingsFragment()) // replace flContainer
                            .commit();
                    return true;

                case R.id.chat:
                    getSupportFragmentManager()
                            .beginTransaction()
                            //.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            .replace(R.id.frame_container, new ChatFragment()) // replace flContainer
                            .commit();
                    return true;

                case R.id.environment:
                    getSupportFragmentManager()
                            .beginTransaction()
                            //.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            .replace(R.id.frame_container, new EnvironmentFragment()) // replace flContainer
                            .commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setVisibility(View.GONE);

        getSupportFragmentManager()
                .beginTransaction()
                //.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.frame_container, new LogoFragment())
                .commit();
    }
}
