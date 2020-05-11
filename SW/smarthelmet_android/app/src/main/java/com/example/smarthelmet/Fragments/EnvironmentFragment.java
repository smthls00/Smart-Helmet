package com.example.smarthelmet.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.smarthelmet.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class EnvironmentFragment extends Fragment {

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
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

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

        gasSeries = new LineGraphSeries<>();
        co2Series = new LineGraphSeries<>();
        smokeSeries = new LineGraphSeries<>();
        outTempSeries = new LineGraphSeries<>();
        presSeries = new LineGraphSeries<>();
        humSeries = new LineGraphSeries<>();
        altSeries = new LineGraphSeries<>();

//        gasChart_create();
//        stepsChart_create();
//        actChart_create();
//        tmpChart_create();

        return view;
    }

//    private void gasChart_create() {
//        stepsSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorSteps));
//        stepsSeries.setSpacing(30);
//
//        //stepsChart.getViewport().setScalable(true);
//        //stepsChart.getViewport().setScalableY(true);
//
//        stepsChart.getViewport().setXAxisBoundsManual(true);
//        stepsChart.getViewport().setMinX(1);
//        stepsChart.getViewport().setMaxX(7);
//        stepsChart.getViewport().setYAxisBoundsManual(true);
//        stepsChart.getViewport().setMaxY(300);
//        stepsChart.getViewport().setMinY(0);
//
//        stepsChart.getGridLabelRenderer().setNumHorizontalLabels(7);
//        stepsChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
//        stepsChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
//        stepsChart.getGridLabelRenderer().setTextSize(35f);
//
//        stepsChart.addSeries(stepsSeries);
//    }
}
