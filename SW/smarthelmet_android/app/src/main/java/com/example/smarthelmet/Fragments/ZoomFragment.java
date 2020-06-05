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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.smarthelmet.R;
import com.example.smarthelmet.SeriesDataHolder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import static com.example.smarthelmet.Constants.BTDataIntent;
import static com.example.smarthelmet.Constants.BTEnvIntent;
import static com.example.smarthelmet.Constants.BTUserIntent;
import static com.example.smarthelmet.Constants.actCommand;
import static com.example.smarthelmet.Constants.altCommand;
import static com.example.smarthelmet.Constants.bpmCommand;
import static com.example.smarthelmet.Constants.co2Command;
import static com.example.smarthelmet.Constants.coCommand;
import static com.example.smarthelmet.Constants.gasCommand;
import static com.example.smarthelmet.Constants.humCommand;
import static com.example.smarthelmet.Constants.lpgCommand;
import static com.example.smarthelmet.Constants.otpCommand;
import static com.example.smarthelmet.Constants.prsCommand;
import static com.example.smarthelmet.Constants.smkCommand;
import static com.example.smarthelmet.Constants.stepsCommand;
import static com.example.smarthelmet.Constants.tvocCommand;
import static com.example.smarthelmet.Constants.utpCommand;
import static com.example.smarthelmet.Constants.zoomMessageBundle;
import static com.example.smarthelmet.Constants.zoomSeriesBundle;


public class ZoomFragment extends Fragment {

    GraphView zoomChart;
    LineGraphSeries<DataPoint> zoomSeries;
    BarGraphSeries<DataPoint> zoomSeriesBar;

    ImageView zoomIv;
    TextView zoomTv;

    String currChart;
    String currIntent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(BTDataReceiver,
                new IntentFilter(BTDataIntent));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_zoom, container, false);

        BottomNavigationView navigation = getActivity().findViewById(R.id.navigation);
        navigation.setVisibility(View.GONE);

        zoomIv = view.findViewById(R.id.zoomIv);
        zoomTv = view.findViewById(R.id.zoomTv);

        zoomChart = view.findViewById(R.id.zoomChart);

        currChart = this.getArguments().getString(zoomMessageBundle);

        SeriesDataHolder seriesDataHolder = (SeriesDataHolder) getArguments().getSerializable(zoomSeriesBundle);

        if (seriesDataHolder != null) {
            zoomSeriesBar = seriesDataHolder.getBarSeries();
            zoomSeries = seriesDataHolder.getLineSeries();
        }

        zoomChartCommon_create();

        if (currChart.equals(utpCommand) || currChart.equals(bpmCommand) || currChart.equals(stepsCommand) || currChart.equals(actCommand))
            currIntent = BTUserIntent;
        else
            currIntent = BTEnvIntent;

        switch (currChart) {
            case utpCommand:
            case otpCommand:
                tmpUIUpdate();
                tempChart_create();
                break;
            case bpmCommand:
                bpmUIUpdate();
                bpmChart_create();
                break;
            case stepsCommand:
                stepsUIUpdate();
                stepsChart_create();
                break;
            case actCommand:
                actUIUpdate();
                actChart_create();
                break;
            case gasCommand:
                gasUIUpdate();
                gasChart_create();
                break;
            case humCommand:
                humUIUpdate();
                humChart_create();
                break;
            case co2Command:
                co2UIUpdate();
                co2Chart_create();
                break;
            case altCommand:
                altUIUpdate();
                altChart_create();
                break;
            case smkCommand:
                smokeUIUpdate();
                smokeChart_create();
                break;
            case prsCommand:
                presUIUpdate();
                presChart_create();
                break;

            case coCommand:
                coUIUpdate();
                coChart_create();
                break;

            case tvocCommand:
                tvocUIUPdate();
                tvocChart_create();
                break;

            case lpgCommand:
                lpgUIUpdate();
                lpgChart_create();
                break;

            default:
                break;
        }

        zoomSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(zoomChart.getContext(), String.format("%.1f", dataPoint.getY()), Toast.LENGTH_SHORT).show();
            }
        });

        zoomSeriesBar.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(zoomChart.getContext(), String.format("%.0f", dataPoint.getY()), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }


    private BroadcastReceiver BTDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(currIntent);
            Log.d("receiver", "zoomReceiver: " + message);

            if (message == null)
                return;


            try {
                float val = 0;

                switch (currChart) {
                    case bpmCommand:
                        val = Float.parseFloat(message.substring(message.indexOf(bpmCommand) + 1, message.indexOf(utpCommand)));
                        break;
                    case utpCommand:
                        val = Float.parseFloat(message.substring(message.indexOf(utpCommand) + 1));
                        break;
                    case co2Command:
                        val = Float.parseFloat(message.substring(message.indexOf(coCommand) + 1, message.indexOf(tvocCommand)));
                        break;
                    case tvocCommand:
                        val = Float.parseFloat(message.substring(message.indexOf(tvocCommand) + 1, message.indexOf(prsCommand)));
                        break;
                    case prsCommand:
                        val = Float.parseFloat(message.substring(message.indexOf(prsCommand) + 1, message.indexOf(humCommand)));
                        break;
                    case humCommand:
                        val = Float.parseFloat(message.substring(message.indexOf(humCommand) + 1, message.indexOf(gasCommand)));
                        break;
                    case gasCommand:
                        val = Float.parseFloat(message.substring(message.indexOf(gasCommand) + 1, message.indexOf(altCommand)));
                        break;
                    case altCommand:
                        val = Float.parseFloat(message.substring(message.indexOf(altCommand) + 1, message.indexOf(lpgCommand)));
                        break;
                    case lpgCommand:
                        val = Float.parseFloat(message.substring(message.indexOf(lpgCommand) + 1, message.indexOf(coCommand)));
                        break;
                    case smkCommand:
                        val = Float.parseFloat(message.substring(message.indexOf(smkCommand)));
                        break;
                }

                if (val != 0)
                    zoomSeries.appendData(new DataPoint(zoomSeries.getHighestValueX() + 0.1, val), true, 60 * 10);

            } catch (Exception e) {
                Log.d("exceptionUserFragmentBT", e.toString());
            }


        }
    };

    private void tmpUIUpdate() {
        zoomTv.setText(R.string.temperature);
        zoomIv.setImageResource(R.drawable.ic_ac_unit_black_24dp);
    }

    private void bpmUIUpdate() {
        zoomTv.setText(R.string.heartRate);
        zoomIv.setImageResource(R.drawable.ic_favorite_black_24dp);

    }

    private void actUIUpdate() {
        zoomTv.setText(R.string.activities);
        zoomIv.setImageResource(R.drawable.ic_star_black_24dp);
    }

    private void stepsUIUpdate() {
        zoomTv.setText(R.string.steps);
        zoomIv.setImageResource(R.drawable.ic_directions_walk_black_24dp);
    }

    private void gasUIUpdate() {
        zoomTv.setText(R.string.gasResistance);
        zoomIv.setImageResource(R.drawable.ic_bubble_chart_black_24dp);
    }

    private void smokeUIUpdate() {
        zoomTv.setText(R.string.smoke);
        zoomIv.setImageResource(R.drawable.ic_cloud_black_24dp);
    }

    private void presUIUpdate() {
        zoomTv.setText(R.string.pressure);
        zoomIv.setImageResource(R.drawable.ic_vertical_align_bottom_black_24dp);
    }

    private void co2UIUpdate() {
        zoomTv.setText(R.string.co2);
        zoomIv.setImageResource(R.drawable.ic_local_florist_black_24dp);
    }

    private void humUIUpdate() {
        zoomTv.setText(R.string.humidity);
        zoomIv.setImageResource(R.drawable.ic_opacity_black_24dp);
    }

    private void altUIUpdate() {
        zoomTv.setText(R.string.altitude);
        zoomIv.setImageResource(R.drawable.ic_filter_hdr_black_24dp);
    }

    private void coUIUpdate() {
        zoomTv.setText(R.string.co);
        zoomIv.setImageResource(R.drawable.ic_whatshot_black_24dp);
    }

    private void tvocUIUPdate() {
        zoomTv.setText(R.string.tvoc);
        zoomIv.setImageResource(R.drawable.ic_grain_black_24dp);
    }

    private void lpgUIUpdate() {
        zoomTv.setText(R.string.lpg);
        zoomIv.setImageResource(R.drawable.ic_local_gas_station_black_24dp);
    }

    private void zoomChartCommon_create() {
        zoomSeries.setThickness(12);

        zoomChart.getViewport().setXAxisBoundsManual(true);
        zoomChart.getViewport().setYAxisBoundsManual(true);

//        zoomChart.getViewport().setScalable(true);
//        zoomChart.getViewport().setScalableY(true);

        zoomChart.getViewport().setMinY(0);
        zoomChart.getViewport().setMinX(0);
        zoomChart.getViewport().setMaxX(6);

        zoomChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        zoomChart.getGridLabelRenderer().setNumVerticalLabels(10);
        zoomChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        zoomChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        zoomChart.getGridLabelRenderer().setTextSize(30f);

        zoomChart.addSeries(zoomSeries);

        zoomChart.getViewport().scrollToEnd();
    }


    private void gasChart_create() {
        zoomChart.getViewport().setMaxY(10000);
    }

    private void humChart_create() {
        zoomChart.getViewport().setMaxY(100);
    }

    private void co2Chart_create() {
        zoomChart.getViewport().setMaxY(10000);
    }

    private void smokeChart_create() {
        zoomChart.getViewport().setMaxY(10000);
    }

    private void coChart_create() {
        zoomChart.getViewport().setMaxY(10000);
    }

    private void tvocChart_create() {
        zoomChart.getViewport().setMaxY(10000);
    }

    private void lpgChart_create() {
        zoomChart.getViewport().setMaxY(10000);
    }

    private void altChart_create() {
        zoomChart.getViewport().setMaxY(1000);
        zoomChart.getViewport().setMinY(-1000);
    }

    private void presChart_create() {
        zoomChart.getViewport().setMaxY(10000);
    }

    private void tempChart_create() {
        zoomChart.getViewport().setMaxY(50);
        zoomChart.getViewport().setMinY(-20);
    }

    private void bpmChart_create() {
        zoomChart.getViewport().setMaxY(180);
    }

    private void actChart_create() {
        zoomChart.getGridLabelRenderer().setNumHorizontalLabels(6);
        zoomChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        zoomChart.getViewport().setMinX(1);
        zoomChart.getViewport().setMaxX(6);
        zoomChart.getViewport().setMaxY(100);

        zoomChart.addSeries(zoomSeriesBar);
    }

    private void stepsChart_create() {
        zoomChart.getGridLabelRenderer().setNumHorizontalLabels(7);
        zoomChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        zoomChart.getViewport().setMinX(1);
        zoomChart.getViewport().setMaxX(7);
        zoomChart.getViewport().setMaxY(300);

        zoomChart.addSeries(zoomSeriesBar);
    }

    @Override
    public void onResume() {
        Log.d("zoomFragment", "onResume");

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("zoomFragment", "onResume");

        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d("zoomFragment", "onDestroy");

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(BTDataReceiver);

        super.onDestroy();
    }
}
