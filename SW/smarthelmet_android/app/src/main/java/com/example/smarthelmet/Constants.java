package com.example.smarthelmet;

import android.os.Environment;

public final class Constants {
    public static final String BTScanIntent = "BTScanIntent";
    public static final String BTStateIntent = "BTStateIntent";
    public static final String BTDataIntent = "BTDataIntent";
    public static final String BTUserIntent = "BTUserIntent";
    public static final String BTEnvIntent = "BTEnvIntent";
    public static final String BTCommandIntent = "BTCommandIntent";
    public static final String BTDataReceiveIntent = "BTReceiveIntent";
    public static final String BTDataLogIntent = "BTDataLogIntent";
    public static final String BTWarningIntent = "BTWarningIntent";
    public static final String BTThresholdNotificationIntent = "BTThresholdNotificationIntent";
    public static final String connectIntent = "connectIntent";
    public static final String usrPreferenceIntent = "usrPreferenceIntent";
    public static final String envPreferenceIntent = "envPreferenceIntent";
    public static final String stopService = "stopServiceIntent";

    public static final String BTOff = "BTOff";
    public static final String BTOn = "BTOn";
    public static final String scannerTimeOut = "TimeOut";

    public static final String stopOKService = "OK";
    public static final String onStopSearching = "onStop";

    public static final String BTdeviceName = "ESP32_SmartHelmet";


    public final static String zoomMessageBundle = "zoomMessage";
    public final static String zoomSeriesBundle = "zoomSeries";

    public static final String connectFragmentTag = "connectFragmentTag";
    public static final String userFragmentTag = "userFragmentTag";
    public static final String settingsFragmentTag = "settingsFragmentTag";
    public static final String environmentFragmentTag = "environmentFragmentTag";
    public static final String chatFragmentTag = "chatFragmentTag";

    public static final String logDataPath = Environment.getExternalStorageDirectory().toString();
    public static final String logFileName = "smartHelmetLogData.txt";


    public static final String stopBTCommand = "s\n";
    public static final String continueBTCommand = "c\n";
    public static final String forwardDistanceBTCommand = "f";
    public static final String backwardDistanceBTCommand = "b\n";
    public static final String vibrateBTCommand = "v\n";




    public static final String gasCommand = "g";
    public static final String altCommand = "a";
    public static final String utpCommand = "t";
    public static final String otpCommand = "o";
    public static final String humCommand = "h";
    public static final String bpmCommand = "b";
    public static final String smkCommand = "s";
    public static final String co2Command = "c";
    public static final String prsCommand = "p";
    public static final String coCommand = "z";
    public static final String lpgCommand = "l";
    public static final String tvocCommand = "v";

    public static final String actCommand = "act";
    public static final String stepsCommand = "steps";
}
