package com.example.smarthelmet;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_down, R.anim.slide_up)
                .replace(R.id.frame_container, new LogoFragment()) // replace flContainer
                .commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setVisibility(View.GONE);
    }




    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.overview:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slide_down, R.anim.slide_up)
                            .replace(R.id.frame_container, new OverviewFragment()) // replace flContainer
                            .commit();
                    return true;

                case R.id.settings:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slide_down, R.anim.slide_up)
                            .replace(R.id.frame_container, new SettingsFragment()) // replace flContainer
                            .commit();
                    return true;

                case R.id.chat:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slide_down, R.anim.slide_up)
                            .replace(R.id.frame_container, new ChatFragment()) // replace flContainer
                            .commit();
                    return true;
            }
            return false;
        }
    };
}
