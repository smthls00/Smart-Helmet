package com.example.smarthelmet.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smarthelmet.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import org.w3c.dom.Text;


public class ZoomFragment extends Fragment {

    private final String BTDataIntent = "BTDataIntent";

    GraphView actChart;
    BarGraphSeries<DataPoint> actSeries;

    GraphView stepsChart;
    BarGraphSeries<DataPoint> stepsSeries;


    GraphView zoomChart;
    LineGraphSeries<DataPoint> zoomSeries;


    ImageView zoomIv;
    TextView zoomTv;

    String currChart;


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
        zoomSeries = new LineGraphSeries<>();


        ZoomChartCommon_create();

        currChart = this.getArguments().getString("zoomMessage");



        switch (currChart){
            case "tmp":
                tmpUIUpdate();
                tempChart_create();
                break;
            case "bpm":
                bpmUIUpdate();
                bpmChart_create();
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

                if(currChart.equals(opCode)){
                    zoomSeries.appendData(new DataPoint(zoomSeries.getHighestValueX() + 0.1, opVal), true, 60 * 10);
                }

            } catch (Exception e) {
                Log.d("exceptionUserFragmentBT", e.toString());
            }


        }
    };

    private void tmpUIUpdate(){
        zoomTv.setText(R.string.temperature);
        zoomIv.setImageResource(R.drawable.ic_ac_unit_black_24dp);
    }

    private void bpmUIUpdate(){
        zoomTv.setText(R.string.heartRate);
        zoomIv.setImageResource(R.drawable.ic_favorite_black_24dp);

    }

    private void ZoomChartCommon_create(){
        zoomSeries.setThickness(4);

        zoomChart.getViewport().setXAxisBoundsManual(true);
        zoomChart.getViewport().setYAxisBoundsManual(true);

//        zoomChart.getViewport().setScalable(true);
//        zoomChart.getViewport().setScalableY(true);

        zoomChart.getViewport().setMinY(0);
        zoomChart.getViewport().setMinX(0);

        zoomChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        zoomChart.getGridLabelRenderer().setNumVerticalLabels(10);
        zoomChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        zoomChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        zoomChart.getGridLabelRenderer().setTextSize(35f);

        zoomChart.addSeries(zoomSeries);
    }


    private void tempChart_create() {
        zoomSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorTmp));

        zoomChart.getViewport().setMaxX(60);
        zoomChart.getViewport().setMaxY(45);
    }

    private void bpmChart_create() {
        zoomSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorBPM));


        zoomChart.getViewport().setMaxX(60);
        zoomChart.getViewport().setMaxY(180);
    }
}
