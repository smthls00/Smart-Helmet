package com.example.smarthelmet.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.example.smarthelmet.R;
import com.example.smarthelmet.SeriesDataHolder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import static com.example.smarthelmet.Constants.BTDataIntent;
import static com.example.smarthelmet.Constants.BTEnvIntent;
import static com.example.smarthelmet.Constants.BTWarningIntent;
import static com.example.smarthelmet.Constants.altCommand;
import static com.example.smarthelmet.Constants.co2Command;
import static com.example.smarthelmet.Constants.coCommand;
import static com.example.smarthelmet.Constants.envPreferenceIntent;
import static com.example.smarthelmet.Constants.environmentFragmentTag;
import static com.example.smarthelmet.Constants.gasCommand;
import static com.example.smarthelmet.Constants.humCommand;
import static com.example.smarthelmet.Constants.lpgCommand;
import static com.example.smarthelmet.Constants.otpCommand;
import static com.example.smarthelmet.Constants.prsCommand;
import static com.example.smarthelmet.Constants.smkCommand;
import static com.example.smarthelmet.Constants.tvocCommand;
import static com.example.smarthelmet.Constants.zoomMessageBundle;
import static com.example.smarthelmet.Constants.zoomSeriesBundle;

public class EnvironmentFragment extends Fragment implements View.OnClickListener {

    int gasThreshold;
    int co2Threshold;
    int smkThreshold;
    int otpThreshold;
    int prsThreshold;
    int humThreshold;
    int altThreshold;
    int coThreshold;
    int tvocThreshold;
    int lpgThreshold;

    Intent warningIntent;

    GraphView gasChart;
    LineGraphSeries<DataPoint> gasSeries;

    GraphView co2Chart;
    LineGraphSeries<DataPoint> co2Series;

    GraphView outTempChart;
    LineGraphSeries<DataPoint> outTempSeries;

    GraphView presChart;
    LineGraphSeries<DataPoint> presSeries;

    GraphView humChart;
    LineGraphSeries<DataPoint> humSeries;

    GraphView altChart;
    LineGraphSeries<DataPoint> altSeries;

    GraphView coChart;
    LineGraphSeries<DataPoint> coSeries;

    GraphView lpgChart;
    LineGraphSeries<DataPoint> lpgSeries;

    GraphView smokeChart;
    LineGraphSeries<DataPoint> smokeSeries;

    GraphView tvocChart;
    LineGraphSeries<DataPoint> tvocSeries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(BTDataReceiver,
                new IntentFilter(BTDataIntent));

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(PreferenceReceiver,
                new IntentFilter(envPreferenceIntent));

        warningIntent = new Intent(BTWarningIntent);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        gasThreshold = Integer.parseInt(sharedPref.getString(getString(R.string.gasPreference), getString(R.string.gasThreshold)));
        co2Threshold = Integer.parseInt(sharedPref.getString(getString(R.string.co2Preference), getString(R.string.co2Threshold)));
        smkThreshold = Integer.parseInt(sharedPref.getString(getString(R.string.smokePreference), getString(R.string.smokeThreshold)));
        otpThreshold = Integer.parseInt(sharedPref.getString(getString(R.string.otpPreference), getString(R.string.otpThreshold)));
        prsThreshold = Integer.parseInt(sharedPref.getString(getString(R.string.pressurePreference), getString(R.string.pressureThreshold)));
        humThreshold = Integer.parseInt(sharedPref.getString(getString(R.string.humidityPreference), getString(R.string.humidityThreshold)));
        altThreshold = Integer.parseInt(sharedPref.getString(getString(R.string.altitudePreference), getString(R.string.altitudeThreshold)));
        coThreshold = Integer.parseInt(sharedPref.getString(getString(R.string.coPreference), getString(R.string.coThreshold)));
        tvocThreshold = Integer.parseInt(sharedPref.getString(getString(R.string.tvocPreference), getString(R.string.tvocThreshold)));
        lpgThreshold = Integer.parseInt(sharedPref.getString(getString(R.string.lpgPreference), getString(R.string.lpgThreshold)));

        Log.d(environmentFragmentTag, "humThreshold " + humThreshold);


        gasSeries = new LineGraphSeries<>();
        co2Series = new LineGraphSeries<>();
        smokeSeries = new LineGraphSeries<>();
        outTempSeries = new LineGraphSeries<>();
        presSeries = new LineGraphSeries<>();
        humSeries = new LineGraphSeries<>();
        altSeries = new LineGraphSeries<>();
        coSeries = new LineGraphSeries<>();
        tvocSeries = new LineGraphSeries<>();
        lpgSeries = new LineGraphSeries<>();

        Log.d("envFragment", "onCreate");

        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_environment, container, false);

        BottomNavigationView navigation = getActivity().findViewById(R.id.navigation);
        navigation.setVisibility(View.VISIBLE);

        gasChart = view.findViewById(R.id.gasChart);
        co2Chart = view.findViewById(R.id.co2Chart);
        smokeChart = view.findViewById(R.id.smokeChart);
        outTempChart = view.findViewById(R.id.outTempChart);
        presChart = view.findViewById(R.id.presChart);
        humChart = view.findViewById(R.id.humChart);
        altChart = view.findViewById(R.id.altChart);
        coChart = view.findViewById(R.id.coChart);
        tvocChart = view.findViewById(R.id.tvocChart);
        lpgChart = view.findViewById(R.id.lpgChart);


        gasChart_create();
        co2Chart_create();
        smokeChart_create();
        outTempChart_create();
        presChart_create();
        humChart_create();
        altChart_create();
        coChart_create();
        tvocChart_create();
        lpgChart_create();


        gasChart.setOnClickListener(this);
        co2Chart.setOnClickListener(this);
        smokeChart.setOnClickListener(this);
        outTempChart.setOnClickListener(this);
        presChart.setOnClickListener(this);
        humChart.setOnClickListener(this);
        altChart.setOnClickListener(this);
        coChart.setOnClickListener(this);
        tvocChart.setOnClickListener(this);
        lpgChart.setOnClickListener(this);
        return view;
    }

    private BroadcastReceiver PreferenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(envPreferenceIntent);

            if (message == null)
                return;

            Log.d("preferenceEnv", "message: " + message);


            try {
                String thresholdCommand = String.valueOf(message.charAt(0));
                int thresholdValue = Integer.parseInt(message.substring(1));

                switch (thresholdCommand){
                    case gasCommand:
                        gasThreshold = thresholdValue;
                        break;
                    case co2Command:
                        co2Threshold = thresholdValue;
                        break;
                    case smkCommand:
                        smkThreshold = thresholdValue;
                        break;
                    case otpCommand:
                        otpThreshold = thresholdValue;
                        break;
                    case prsCommand:
                        prsThreshold = thresholdValue;
                        break;
                    case humCommand:
                        Log.d(environmentFragmentTag, "humThresholdUpdated " + thresholdValue);
                        humThreshold = thresholdValue;
                        break;
                    case coCommand:
                        coThreshold = thresholdValue;
                        break;
                    case tvocCommand:
                        tvocThreshold = thresholdValue;
                        break;
                    case lpgCommand:
                        lpgThreshold = thresholdValue;
                        break;
                }

            } catch (Exception e) {
                Log.d("exceptionEnvFragmentBT", e.toString());
            }
        }
    };

    private BroadcastReceiver BTDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(BTEnvIntent);

            if (message == null)
                return;

            Log.d("receiverEnv", "message: " + message);

            try {
                int co2Index = message.indexOf(co2Command);
                int tvocIndex = message.indexOf(tvocCommand);
                int otpIndex = message.indexOf(otpCommand);
                int prsIndex = message.indexOf(prsCommand);
                int humIndex = message.indexOf(humCommand);
                int gasIndex = message.indexOf(gasCommand);
                int altIndex = message.indexOf(altCommand);
                int lpgIndex = message.indexOf(lpgCommand);
                int coIndex = message.indexOf(coCommand);
                int smkIndex = message.indexOf(smkCommand);

                float co2Val = Float.parseFloat(message.substring(co2Index + 1, tvocIndex));
                float tvocVal = Float.parseFloat(message.substring(tvocIndex + 1, otpIndex));
                float otpVal = Float.parseFloat(message.substring(otpIndex + 1, prsIndex));
                float presVal = Float.parseFloat(message.substring(prsIndex + 1, humIndex));
                float humVal = Float.parseFloat(message.substring(humIndex + 1, gasIndex));
                float gasVal = Float.parseFloat(message.substring(gasIndex + 1, altIndex));
                float altVal = Float.parseFloat(message.substring(altIndex + 1, lpgIndex));
                float lpgVal = Float.parseFloat(message.substring(lpgIndex + 1, coIndex));
                float coVal = Float.parseFloat(message.substring(coIndex + 1, smkIndex));
                float smkVal = Float.parseFloat(message.substring(smkIndex + 1));

                co2Series.appendData(new DataPoint(co2Series.getHighestValueX() + 0.1, co2Val), true, 60 * 10);
                tvocSeries.appendData(new DataPoint(tvocSeries.getHighestValueX() + 0.1, tvocVal), true, 60 * 10);
                outTempSeries.appendData(new DataPoint(outTempSeries.getHighestValueX() + 0.1, otpVal), true, 60 * 10);
                presSeries.appendData(new DataPoint(presSeries.getHighestValueX() + 0.1, presVal), true, 60 * 10);
                humSeries.appendData(new DataPoint(humSeries.getHighestValueX() + 0.1, humVal), true, 60 * 10);
                gasSeries.appendData(new DataPoint(gasSeries.getHighestValueX() + 0.1, gasVal), true, 60 * 10);
                altSeries.appendData(new DataPoint(altSeries.getHighestValueX() + 0.1, altVal), true, 60 * 10);
                lpgSeries.appendData(new DataPoint(lpgSeries.getHighestValueX() + 0.1, lpgVal), true, 60 * 10);
                coSeries.appendData(new DataPoint(coSeries.getHighestValueX() + 0.1, coVal), true, 60 * 10);
                smokeSeries.appendData(new DataPoint(smokeSeries.getHighestValueX() + 0.1, smkVal), true, 60 * 10);



                if(co2Val > co2Threshold){
                    warningIntent.putExtra(BTWarningIntent, "CO² level is above " + co2Threshold + "ppm");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(warningIntent);
                }

                if(gasVal > gasThreshold){
                    warningIntent.putExtra(BTWarningIntent, "Gas Resistance level is above " + gasThreshold + "kOhm");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(warningIntent);
                }

                if(smkVal > smkThreshold){
                    warningIntent.putExtra(BTWarningIntent, "Smoke level is above " + smkThreshold + "ppm");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(warningIntent);
                }

                if(otpVal > otpThreshold){
                    warningIntent.putExtra(BTWarningIntent, "Temperature level is above " + otpThreshold + "°C");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(warningIntent);
                }

                if(presVal > prsThreshold){
                    warningIntent.putExtra(BTWarningIntent, "Pressure level is above " + prsThreshold + "hPa");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(warningIntent);
                }

                if(humVal > humThreshold){
                    Log.d(environmentFragmentTag, "humThresholdWarning " + humThreshold);
                    warningIntent.putExtra(BTWarningIntent, "Humidity level is above " + humThreshold + "%");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(warningIntent);
                }

                if(altVal > altThreshold){
                    warningIntent.putExtra(BTWarningIntent, "Altitude level is above " + altThreshold + "m");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(warningIntent);
                }

                if(coVal > coThreshold){
                    warningIntent.putExtra(BTWarningIntent, "CO level is above " + coThreshold + "ppm");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(warningIntent);
                }

                if(tvocVal > tvocThreshold){
                    warningIntent.putExtra(BTWarningIntent, "TVOC level is above " + tvocThreshold + "ppm");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(warningIntent);
                }

                if(lpgVal > lpgThreshold){
                    warningIntent.putExtra(BTWarningIntent, "LPG level is above " + lpgThreshold + "ppm");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(warningIntent);
                }

            } catch (Exception e) {
                Log.d("exceptionEnvFragmentBT", e.toString());
            }
        }
    };

    private void gasChart_create() {
        gasSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorGas));
        gasSeries.setThickness(6);

        //stepsChart.getViewport().setScalable(true);
        //stepsChart.getViewport().setScalableY(true);

        gasChart.getViewport().setXAxisBoundsManual(true);
        gasChart.getViewport().setMinX(0);
        gasChart.getViewport().setMaxX(6);
        gasChart.getViewport().setYAxisBoundsManual(true);
        gasChart.getViewport().setMaxY(10000);
        gasChart.getViewport().setMinY(0);

        gasChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        gasChart.getGridLabelRenderer().setNumVerticalLabels(10);
        gasChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        gasChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        gasChart.getGridLabelRenderer().setTextSize(25f);

        gasChart.addSeries(gasSeries);

        gasChart.getViewport().scrollToEnd();
    }

    private void co2Chart_create() {
        co2Series.setColor(ContextCompat.getColor(getActivity(), R.color.colorCo2));
        co2Series.setThickness(6);

        //stepsChart.getViewport().setScalable(true);
        //stepsChart.getViewport().setScalableY(true);

        co2Chart.getViewport().setXAxisBoundsManual(true);
        co2Chart.getViewport().setMinX(0);
        co2Chart.getViewport().setMaxX(6);
        co2Chart.getViewport().setYAxisBoundsManual(true);
        co2Chart.getViewport().setMaxY(10000);
        co2Chart.getViewport().setMinY(0);

        co2Chart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        co2Chart.getGridLabelRenderer().setNumVerticalLabels(10);
        co2Chart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        co2Chart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        co2Chart.getGridLabelRenderer().setTextSize(25f);

        co2Chart.addSeries(co2Series);

        co2Chart.getViewport().scrollToEnd();
    }

    private void smokeChart_create() {
        smokeSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorSmoke));
        smokeSeries.setThickness(6);

        //stepsChart.getViewport().setScalable(true);
        //stepsChart.getViewport().setScalableY(true);

        smokeChart.getViewport().setXAxisBoundsManual(true);
        smokeChart.getViewport().setMinX(0);
        smokeChart.getViewport().setMaxX(6);
        smokeChart.getViewport().setYAxisBoundsManual(true);
        smokeChart.getViewport().setMaxY(10000);
        smokeChart.getViewport().setMinY(0);

        smokeChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        smokeChart.getGridLabelRenderer().setNumVerticalLabels(10);
        smokeChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        smokeChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        smokeChart.getGridLabelRenderer().setTextSize(25f);

        smokeChart.addSeries(smokeSeries);

        smokeChart.getViewport().scrollToEnd();
    }

    private void outTempChart_create() {
        outTempSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorTmp));
        outTempSeries.setThickness(6);

        //stepsChart.getViewport().setScalable(true);
        //stepsChart.getViewport().setScalableY(true);

        outTempChart.getViewport().setXAxisBoundsManual(true);
        outTempChart.getViewport().setMinX(0);
        outTempChart.getViewport().setMaxX(6);
        outTempChart.getViewport().setYAxisBoundsManual(true);
        outTempChart.getViewport().setMaxY(50);
        outTempChart.getViewport().setMinY(-20);

        outTempChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        outTempChart.getGridLabelRenderer().setNumVerticalLabels(10);
        outTempChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        outTempChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        outTempChart.getGridLabelRenderer().setTextSize(25f);

        outTempChart.addSeries(outTempSeries);

        outTempChart.getViewport().scrollToEnd();
    }

    private void presChart_create() {
        presSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorPressure));
        presSeries.setThickness(6);
        //stepsChart.getViewport().setScalable(true);
        //stepsChart.getViewport().setScalableY(true);

        presChart.getViewport().setXAxisBoundsManual(true);
        presChart.getViewport().setMinX(0);
        presChart.getViewport().setMaxX(6);
        presChart.getViewport().setYAxisBoundsManual(true);
        presChart.getViewport().setMaxY(10000);
        presChart.getViewport().setMinY(0);

        presChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        presChart.getGridLabelRenderer().setNumVerticalLabels(10);
        presChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        presChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        presChart.getGridLabelRenderer().setTextSize(25f);

        presChart.addSeries(presSeries);

        presChart.getViewport().scrollToEnd();
    }

    private void humChart_create() {
        humSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorHumidity));
        humSeries.setThickness(6);

        //stepsChart.getViewport().setScalable(true);
        //stepsChart.getViewport().setScalableY(true);

        humChart.getViewport().setXAxisBoundsManual(true);
        humChart.getViewport().setMinX(0);
        humChart.getViewport().setMaxX(6);
        humChart.getViewport().setYAxisBoundsManual(true);
        humChart.getViewport().setMaxY(100);
        humChart.getViewport().setMinY(0);

        humChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        humChart.getGridLabelRenderer().setNumVerticalLabels(10);
        humChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        humChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        humChart.getGridLabelRenderer().setTextSize(25f);

        humChart.addSeries(humSeries);

        humChart.getViewport().scrollToEnd();
    }

    private void altChart_create() {
        altSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorAltitude));
        altSeries.setThickness(6);

        //stepsChart.getViewport().setScalable(true);
        //stepsChart.getViewport().setScalableY(true);

        altChart.getViewport().setXAxisBoundsManual(true);
        altChart.getViewport().setMinX(0);
        altChart.getViewport().setMaxX(6);
        altChart.getViewport().setYAxisBoundsManual(true);
        altChart.getViewport().setMaxY(1000);
        altChart.getViewport().setMinY(-1000);

        altChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        altChart.getGridLabelRenderer().setNumVerticalLabels(10);
        altChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        altChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        altChart.getGridLabelRenderer().setTextSize(25f);

        altChart.addSeries(altSeries);

        altChart.getViewport().scrollToEnd();
    }

    private void lpgChart_create() {
        lpgSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorLpg));
        lpgSeries.setThickness(6);

        //stepsChart.getViewport().setScalable(true);
        //stepsChart.getViewport().setScalableY(true);

        lpgChart.getViewport().setXAxisBoundsManual(true);
        lpgChart.getViewport().setMinX(0);
        lpgChart.getViewport().setMaxX(6);
        lpgChart.getViewport().setYAxisBoundsManual(true);
        lpgChart.getViewport().setMaxY(10000);
        lpgChart.getViewport().setMinY(0);

        lpgChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        lpgChart.getGridLabelRenderer().setNumVerticalLabels(10);
        lpgChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        lpgChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        lpgChart.getGridLabelRenderer().setTextSize(25f);

        lpgChart.addSeries(lpgSeries);

        lpgChart.getViewport().scrollToEnd();
    }

    private void coChart_create() {
        coSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorCo));
        coSeries.setThickness(6);

        //stepsChart.getViewport().setScalable(true);
        //stepsChart.getViewport().setScalableY(true);

        coChart.getViewport().setXAxisBoundsManual(true);
        coChart.getViewport().setMinX(0);
        coChart.getViewport().setMaxX(6);
        coChart.getViewport().setYAxisBoundsManual(true);
        coChart.getViewport().setMaxY(10000);
        coChart.getViewport().setMinY(0);

        coChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        coChart.getGridLabelRenderer().setNumVerticalLabels(10);
        coChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        coChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        coChart.getGridLabelRenderer().setTextSize(25f);

        coChart.addSeries(coSeries);

        coChart.getViewport().scrollToEnd();
    }

    private void tvocChart_create() {
        tvocSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorTvoc));
        tvocSeries.setThickness(6);

        //stepsChart.getViewport().setScalable(true);
        //stepsChart.getViewport().setScalableY(true);

        tvocChart.getViewport().setXAxisBoundsManual(true);
        tvocChart.getViewport().setMinX(0);
        tvocChart.getViewport().setMaxX(6);
        tvocChart.getViewport().setYAxisBoundsManual(true);
        tvocChart.getViewport().setMaxY(10000);
        tvocChart.getViewport().setMinY(0);

        tvocChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        tvocChart.getGridLabelRenderer().setNumVerticalLabels(10);
        tvocChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        tvocChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        tvocChart.getGridLabelRenderer().setTextSize(25f);

        tvocChart.addSeries(tvocSeries);

        tvocChart.getViewport().scrollToEnd();
    }

    @Override
    public void onClick(View v) {
        Bundle zoomBundle = new Bundle();
        Fragment zoomFragment = new ZoomFragment();

        switch (v.getId()) {

            case R.id.presChart:
                zoomBundle.putString(zoomMessageBundle, prsCommand);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(presSeries));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragment).
                        addToBackStack(environmentFragmentTag).
                        commitAllowingStateLoss();
                break;

            case R.id.outTempChart:
                zoomBundle.putString(zoomMessageBundle, otpCommand);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(outTempSeries));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragment).
                        addToBackStack(environmentFragmentTag).
                        commitAllowingStateLoss();
                break;

            case R.id.humChart:
                zoomBundle.putString(zoomMessageBundle, humCommand);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(humSeries));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragment).
                        addToBackStack(environmentFragmentTag).
                        commitAllowingStateLoss();
                break;

            case R.id.altChart:
                zoomBundle.putString(zoomMessageBundle, altCommand);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(altSeries));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragment).
                        addToBackStack(environmentFragmentTag).
                        commitAllowingStateLoss();
                break;

            case R.id.gasChart:
                zoomBundle.putString(zoomMessageBundle, gasCommand);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(gasSeries));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragment).
                        addToBackStack(environmentFragmentTag).
                        commitAllowingStateLoss();
                break;

            case R.id.smokeChart:
                zoomBundle.putString(zoomMessageBundle, smkCommand);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(smokeSeries));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragment).
                        addToBackStack(environmentFragmentTag).
                        commitAllowingStateLoss();
                break;

            case R.id.co2Chart:
                zoomBundle.putString(zoomMessageBundle, co2Command);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(co2Series));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragment).
                        addToBackStack(environmentFragmentTag).
                        commitAllowingStateLoss();
                break;

            case R.id.coChart:
                zoomBundle.putString(zoomMessageBundle, coCommand);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(coSeries));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragment).
                        addToBackStack(environmentFragmentTag).
                        commitAllowingStateLoss();
                break;

            case R.id.lpgChart:
                zoomBundle.putString(zoomMessageBundle, lpgCommand);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(lpgSeries));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragment).
                        addToBackStack(environmentFragmentTag).
                        commitAllowingStateLoss();
                break;

            case R.id.tvocChart:
                zoomBundle.putString(zoomMessageBundle, tvocCommand);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(tvocSeries));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragment).
                        addToBackStack(environmentFragmentTag).
                        commitAllowingStateLoss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {

        Log.d(environmentFragmentTag, "onDestroy");

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(BTDataReceiver);

        super.onDestroy();
    }
}
