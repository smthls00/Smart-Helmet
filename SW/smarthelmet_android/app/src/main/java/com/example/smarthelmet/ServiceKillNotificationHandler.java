package com.example.smarthelmet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ServiceKillNotificationHandler extends BroadcastReceiver {

    public final String stopService = "stopServiceIntent";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("ServiceKillerHandler", "notificationPressed");

        //String action = intent.getStringExtra("action");

        Intent stopServiceIntent = new Intent(stopService);
        stopServiceIntent.putExtra(stopService, "OK");
        context.sendBroadcast(stopServiceIntent);
    }

    public void performAction1(){

    }

    public void performAction2(){

    }

}
