package com.example.smarthelmet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.example.smarthelmet.Constants.stopOKService;
import static com.example.smarthelmet.Constants.stopService;

public class ServiceKillNotificationHandler extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("ServiceKillerHandler", "notificationPressed");

        //String action = intent.getStringExtra("action");

        Intent stopServiceIntent = new Intent(stopService);
        stopServiceIntent.putExtra(stopService, stopOKService);
        context.sendBroadcast(stopServiceIntent);
    }

    public void performAction1() {

    }

    public void performAction2() {

    }

}
