package com.example.smarthelmet.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.smarthelmet.R;
import com.example.smarthelmet.SeriesDataHolder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import static com.example.smarthelmet.Constants.BTDataIntent;
import static com.example.smarthelmet.Constants.BTEnvIntent;
import static com.example.smarthelmet.Constants.altCommand;
import static com.example.smarthelmet.Constants.co2Command;
import static com.example.smarthelmet.Constants.environmentFragmentTag;
import static com.example.smarthelmet.Constants.gasCommand;
import static com.example.smarthelmet.Constants.humCommand;
import static com.example.smarthelmet.Constants.otpCommand;
import static com.example.smarthelmet.Constants.prsCommand;
import static com.example.smarthelmet.Constants.smkCommand;
import static com.example.smarthelmet.Constants.zoomMessageBundle;
import static com.example.smarthelmet.Constants.zoomSeriesBundle;

public class EnvironmentFragment extends Fragment implements View.OnClickListener {

    GraphView gasChart;
    LineGraphSeries<DataPoint> gasSeries;

    GraphView co2Chart;
    LineGraphSeries<DataPoint> co2Series;

    GraphView smokeChart;
    LineGraphSeries<DataPoint> smokeSeries;

    GraphView outTempChart;
    LineGraphSeries<DataPoint> outTempSeries;

    GraphView presChart;
    LineGraphSeries<DataPoint> presSeries;

    GraphView humChart;
    LineGraphSeries<DataPoint> humSeries;

    GraphView altChart;
    LineGraphSeries<DataPoint> altSeries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(BTDataReceiver,
                new IntentFilter(BTDataIntent));

        gasSeries = new LineGraphSeries<>();
        co2Series = new LineGraphSeries<>();
        smokeSeries = new LineGraphSeries<>();
        outTempSeries = new LineGraphSeries<>();
        presSeries = new LineGraphSeries<>();
        humSeries = new LineGraphSeries<>();
        altSeries = new LineGraphSeries<>();


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

        gasChart_create();
        co2Chart_create();
        smokeChart_create();
        outTempChart_create();
        presChart_create();
        humChart_create();
        altChart_create();


        gasChart.setOnClickListener(this);
        co2Chart.setOnClickListener(this);
        smokeChart.setOnClickListener(this);
        outTempChart.setOnClickListener(this);
        presChart.setOnClickListener(this);
        humChart.setOnClickListener(this);
        altChart.setOnClickListener(this);


        return view;
    }

    private BroadcastReceiver BTDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(BTEnvIntent);


            if (message == null)
                return;

            Log.d("receiverEnv", "message: " + message);

            try {
                float tmpVal = Float.parseFloat(message.substring(message.indexOf(otpCommand) + 1, message.indexOf(prsCommand)));
                float co2Val = 0;//Float.parseFloat(message.substring(message.indexOf(":") + 1, message.indexOf("p")));
                float smokeVal = 0;
                float presVal = Float.parseFloat(message.substring(message.indexOf(prsCommand) + 1, message.indexOf(humCommand)));
                float humVal = Float.parseFloat(message.substring(message.indexOf(humCommand) + 1, message.indexOf(gasCommand)));
                float gasVal = Float.parseFloat(message.substring(message.indexOf(gasCommand) + 1, message.indexOf(altCommand)));
                float altVal = Float.parseFloat(message.substring(message.indexOf(altCommand) + 1));


                outTempSeries.appendData(new DataPoint(outTempSeries.getHighestValueX() + 0.1, tmpVal), true, 60 * 10);
                //smokeSeries.appendData(new DataPoint(smokeSeries.getHighestValueX() + 0.5, smokeVal), true, 60 * 2);
                //co2Series.appendData(new DataPoint(co2Series.getHighestValueX() + 0.5, co2Val), true, 60 * 2);
                presSeries.appendData(new DataPoint(presSeries.getHighestValueX() + 0.1, presVal), true, 60 * 10);
                humSeries.appendData(new DataPoint(humSeries.getHighestValueX() + 0.1, humVal), true, 60 * 10);
                gasSeries.appendData(new DataPoint(gasSeries.getHighestValueX() + 0.1, gasVal), true, 60 * 10);
                altSeries.appendData(new DataPoint(altSeries.getHighestValueX() + 0.1, altVal), true, 60 * 10);

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

    @Override
    public void onClick(View v) {
        Log.d("onClickUserFragment", "onClick");

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
            default:
                break;
        }

    }
}
