package com.example.smarthelmet.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.smarthelmet.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;


public class UserFragment extends Fragment implements View.OnClickListener {

    private final String BTDataIntent = "BTDataIntent";

    GraphView actChart;
    BarGraphSeries<DataPoint> actSeries;

    GraphView stepsChart;
    BarGraphSeries<DataPoint> stepsSeries;


    GraphView tmpChart;
    LineGraphSeries<DataPoint> tmpSeries;

    GraphView bpmChart;
    LineGraphSeries<DataPoint> bpmSeries;

    double bpmMinute = 1;
    double tmpMinute = 1;


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
        final View view = inflater.inflate(R.layout.fragment_user, container, false);

        BottomNavigationView navigation = getActivity().findViewById(R.id.navigation);
        navigation.setVisibility(View.VISIBLE);

        bpmChart = view.findViewById(R.id.bpmChart);
        stepsChart = view.findViewById(R.id.stepsChart);
        actChart = view.findViewById(R.id.actChart);
        tmpChart = view.findViewById(R.id.tmpChart);

        tmpSeries = new LineGraphSeries<>();
        bpmSeries = new LineGraphSeries<>();

        actSeries = new BarGraphSeries<>(new DataPoint[]{
                new DataPoint(1, 14),
                new DataPoint(2, 20),
                new DataPoint(3, 50),
                new DataPoint(4, 5),
                new DataPoint(5, 5),
                new DataPoint(6, 6),
        });

        stepsSeries = new BarGraphSeries<>(new DataPoint[]{
                new DataPoint(1, 155),
                new DataPoint(2, 78),
                new DataPoint(3, 35),
                new DataPoint(4, 289),
                new DataPoint(5, 191),
                new DataPoint(6, 205),
                new DataPoint(7, 62)
        });

        bpmChart_create();
        stepsChart_create();
        actChart_create();
        tmpChart_create();


//        tmpSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
//            @Override
//            public void onTap(Series series, DataPointInterface dataPoint) {
//                Toast.makeText(tmpChart.getContext(), String.format("%.1f", dataPoint.getY()) + "°C", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        bpmSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
//            @Override
//            public void onTap(Series series, DataPointInterface dataPoint) {
//                Toast.makeText(bpmChart.getContext(), String.format("%.0f", dataPoint.getY()) + " BPM", Toast.LENGTH_SHORT).show();
//            }
//        });


        actSeries.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX() * 255 / 4, (int) Math.abs(data.getY() * 255 / 6), 100);
            }
        });

        stepsSeries.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX() * 255 / 4, (int) Math.abs(data.getY() * 255 / 6), 100);
            }
        });

        tmpChart.setOnClickListener(this);
        bpmChart.setOnClickListener(this);

        return view;
    }


    private BroadcastReceiver BTDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(BTDataIntent);
            Log.d("receiver", "Got message: " + message);

            //dataTv.append(message + " ");

            try {
                String opCode = message.substring(0, 3);

                final float opVal = Float.parseFloat(message.substring(message.indexOf(":") + 1));

                Log.d("receiver", "Got opCode: " + opCode + ", opVal: " + opVal);

                switch (opCode) {
                    case "bpm":
                        bpmSeries.appendData(new DataPoint(bpmSeries.getHighestValueX() + 0.1, opVal), true, 60 * 10);
                        break;
                    case "tmp":
                        tmpSeries.appendData(new DataPoint(tmpSeries.getHighestValueX() + 0.1, opVal), true, 60 * 10);
                        //Log.d("appendData", "getHighestValueX() " + tmpSeries.getHighestValueX());
                        break;
                }


            } catch (Exception e) {
                Log.d("exceptionUserFragmentBT", e.toString());
            }


        }
    };


    private void stepsChart_create() {
        stepsSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorSteps));
        stepsSeries.setSpacing(30);

        //stepsChart.getViewport().setScalable(true);
        //stepsChart.getViewport().setScalableY(true);

        stepsChart.getGridLabelRenderer().setNumHorizontalLabels(7);
        stepsChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        stepsChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        stepsChart.getGridLabelRenderer().setTextSize(25f);


        stepsChart.getViewport().setXAxisBoundsManual(true);
        stepsChart.getViewport().setMinX(1);
        stepsChart.getViewport().setMaxX(7);
        stepsChart.getViewport().setYAxisBoundsManual(true);
        stepsChart.getViewport().setMaxY(300);
        stepsChart.getViewport().setMinY(0);

        stepsChart.addSeries(stepsSeries);
    }

    private void actChart_create() {
        actSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorAct));
        actSeries.setSpacing(30);

        //actChart.getViewport().setScalable(true);
        //actChart.getViewport().setScalableY(true);

        actChart.getGridLabelRenderer().setNumHorizontalLabels(6);
        actChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        actChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        actChart.getGridLabelRenderer().setTextSize(25f);

        actChart.getViewport().setXAxisBoundsManual(true);
        actChart.getViewport().setMinX(1);
        actChart.getViewport().setMaxX(6);
        actChart.getViewport().setYAxisBoundsManual(true);
        actChart.getViewport().setMaxY(100);
        actChart.getViewport().setMinY(0);

        actChart.addSeries(actSeries);

    }


    private void tmpChart_create() {
        tmpSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorTmp));
        tmpSeries.setThickness(4);

        //tmpChart.getViewport().setScalable(true);
        //tmpChart.getViewport().setScalableY(true);

        tmpChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        tmpChart.getGridLabelRenderer().setNumVerticalLabels(10);
        tmpChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        tmpChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        tmpChart.getGridLabelRenderer().setTextSize(25f);

        tmpChart.getViewport().setXAxisBoundsManual(true);
        tmpChart.getViewport().setMinX(0);
        tmpChart.getViewport().setMaxX(60);
        tmpChart.getViewport().setYAxisBoundsManual(true);
        tmpChart.getViewport().setMaxY(45);
        tmpChart.getViewport().setMinY(0);

        tmpChart.addSeries(tmpSeries);
    }

    private void bpmChart_create() {
        bpmSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorBPM));
        bpmSeries.setThickness(4);

        bpmChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        bpmChart.getGridLabelRenderer().setNumVerticalLabels(10);
        bpmChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        bpmChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        bpmChart.getGridLabelRenderer().setTextSize(25f);
        bpmChart.getGridLabelRenderer().reloadStyles();

        //bpmChart.getViewport().setScalable(true);
        //bpmChart.getViewport().setScalableY(true);

        bpmChart.getViewport().setXAxisBoundsManual(true);
        bpmChart.getViewport().setMinX(0);
        bpmChart.getViewport().setMaxX(60);
        bpmChart.getViewport().setYAxisBoundsManual(true);
        bpmChart.getViewport().setMaxY(180);
        bpmChart.getViewport().setMinY(0);

        bpmChart.addSeries(bpmSeries);
    }

    @Override
    public void onClick(View v) {
        Log.d("onClickUserFragment", "onClick");
        switch (v.getId()) {


            case R.id.bpmChart:
                Bundle bpmBundle = new Bundle();
                bpmBundle.putString("zoomMessage", "bpm");
                Fragment zoomFragmentBPM = new ZoomFragment();
                zoomFragmentBPM.setArguments(bpmBundle);;

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragmentBPM).
                        addToBackStack("userFragment").
                        commitAllowingStateLoss();
                break;

            case R.id.tmpChart:
                Bundle tmpBundle = new Bundle();
                tmpBundle.putString("zoomMessage", "tmp");
                Fragment zoomFragmentTmp = new ZoomFragment();
                zoomFragmentTmp.setArguments(tmpBundle);;

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragmentTmp).
                        addToBackStack("userFragment").
                        commitAllowingStateLoss();
                break;

            case R.id.actCard:
                // do your code
                break;

            case R.id.stepsCard:
                // do your code
                break;


            default:
                break;
        }
    }
}
