package com.example.smarthelmet.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.smarthelmet.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class UserFragment extends Fragment {

    private final String BTDataIntent = "BTDataIntent";

    //TextView dataTv;


    PieChart actChart;
    BarChart stepsChart;


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
        //dataTv = view.findViewById(R.id.dataTv);

        bpmChart = view.findViewById(R.id.bpmChart);
        stepsChart = view.findViewById(R.id.stepsChart);
        actChart = view.findViewById(R.id.actChart);
        tmpChart = view.findViewById(R.id.tmpChart);

        tmpSeries = new LineGraphSeries<>();
        bpmSeries = new LineGraphSeries<>();


        tmpSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(tmpChart.getContext(), String.format("%.1f", dataPoint.getY()) + "°C", Toast.LENGTH_SHORT).show();
            }
        });

        bpmSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(bpmChart.getContext(), String.format("%.0f", dataPoint.getY()) + " BPM", Toast.LENGTH_SHORT).show();
            }
        });

        bpmChart_create();
        stepsChart_create();
        actChart_create();
        tmpChart_create();

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
                        bpmSeries.appendData(new DataPoint(bpmSeries.getHighestValueX() + 1, opVal), true, 60);
                        break;
                    case "tmp":
                        tmpSeries.appendData(new DataPoint(tmpSeries.getHighestValueX() + 1, opVal), true, 60);
                        break;
                }


            } catch (Exception e) {
                Log.d("exceptionUserFragmentBT", e.toString());
            }


        }
    };


    private void tmpChart_create(){
        tmpSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorTmp));
        tmpSeries.setThickness(6);
        tmpChart.addSeries(tmpSeries);

        tmpChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        tmpChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        tmpChart.getGridLabelRenderer().setTextSize(35f);
        //tmpChart.getViewport().setScalable(true);
        //tmpChart.getViewport().setScalableY(true);
        tmpChart.getViewport().setDrawBorder(true);
        tmpChart.getViewport().setXAxisBoundsManual(true);
        tmpChart.getViewport().setMinX(0);
        tmpChart.getViewport().setMaxX(60);
        tmpChart.getViewport().setYAxisBoundsManual(true);
        tmpChart.getViewport().setMaxY(45);
        tmpChart.getViewport().setMinY(25);
    }

    private void bpmChart_create(){
        bpmSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorBPM));
        bpmSeries.setThickness(6);
        bpmChart.addSeries(bpmSeries);

        bpmChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        bpmChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        bpmChart.getGridLabelRenderer().setTextSize(35f);
        //bpmChart.getViewport().setScalable(true);
        //bpmChart.getViewport().setScalableY(true);
        bpmChart.getViewport().setDrawBorder(true);
        bpmChart.getViewport().setXAxisBoundsManual(true);
        bpmChart.getViewport().setMinX(0);
        bpmChart.getViewport().setMaxX(60);
        bpmChart.getViewport().setYAxisBoundsManual(true);
        bpmChart.getViewport().setMaxY(180);
        bpmChart.getViewport().setMinY(30);
    }


    private void stepsChart_create() {
        stepsChart.setDrawBarShadow(false);
        stepsChart.setDrawValueAboveBar(true);
        stepsChart.setTouchEnabled(false);
        stepsChart.setDoubleTapToZoomEnabled(false);

        stepsChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        stepsChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        stepsChart.setPinchZoom(false);

        stepsChart.setDrawGridBackground(false);
        stepsChart.getLegend().setEnabled(false);

        // chart.setDrawYLabels(false);


        XAxis xAxis = stepsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        //xAxis.setEnabled(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setTextColor(getContext().getColor(R.color.textColor));


        YAxis leftAxis = stepsChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(new IntegerFormatter());
        leftAxis.setEnabled(false);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = stepsChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(new IntegerFormatter());
        rightAxis.setEnabled(false);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = stepsChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        ArrayList<BarEntry> values = new ArrayList<>();

        values.add(new BarEntry(15, 60));
        values.add(new BarEntry(16, 77));
        values.add(new BarEntry(17, 120));
        values.add(new BarEntry(18, 60));
        values.add(new BarEntry(19 + 1, 55));
        values.add(new BarEntry(20 + 2, 97));
        values.add(new BarEntry(21 + 3, 59));

        BarDataSet set1;

        if (stepsChart.getData() != null &&
                stepsChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) stepsChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            stepsChart.getData().notifyDataChanged();
            stepsChart.notifyDataSetChanged();

        } else {
            set1 = new BarDataSet(values, "Steps");

            set1.setDrawIcons(false);
            set1.setColor(Color.MAGENTA);
            set1.setValueFormatter(new IntegerFormatter());
            set1.setGradientColor(ContextCompat.getColor(getActivity(), R.color.colorStartSteps), ContextCompat.getColor(getActivity(), R.color.colorEndSteps));
            set1.setValueTextColor(getContext().getColor(R.color.textColor));


            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);

            stepsChart.setData(data);
        }
    }

    private void actChart_create() {

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(55, "Walking"));
        entries.add(new PieEntry(5, "Jogging"));
        entries.add(new PieEntry(18, "Upstairs"));
        entries.add(new PieEntry(12, "Downstairs"));
        entries.add(new PieEntry(5, "Sitting"));
        entries.add(new PieEntry(15, "Standing"));

        PieDataSet pieDataSet = new PieDataSet(entries, "Inducesmile");
        pieDataSet.setValueTextSize(10);
        pieDataSet.setValueFormatter(new PercentFormatter());
        pieDataSet.setValueTextColor(getContext().getColor(R.color.textColor));
        pieDataSet.setColors(ColorTemplate.PASTEL_COLORS);
        pieDataSet.setValueTextColor(getContext().getColor(R.color.textColor));
        PieData pieData = new PieData(pieDataSet);

        actChart.setData(pieData);
        actChart.setEntryLabelColor(getContext().getColor(R.color.textColor));
        actChart.setEntryLabelTextSize(10f);
        actChart.setHoleColor(Color.TRANSPARENT);
        actChart.getLegend().setEnabled(false);
        actChart.getDescription().setEnabled(false);
        //actChart.animateXY(5000, 5000);
    }



    public class PercentFormatter extends ValueFormatter {

        private DecimalFormat mFormat;

        public PercentFormatter() {

            // format values to 1 decimal digit
            mFormat = new DecimalFormat("###,###,##0");
        }


        @Override
        public String getFormattedValue(float value) {
            // "value" represents the position of the label on the axis (x or y)
            return mFormat.format(value) + '%';
        }
    }

    public class IntegerFormatter extends ValueFormatter {

        private DecimalFormat mFormat;

        public IntegerFormatter() {

            // format values to 1 decimal digit
            mFormat = new DecimalFormat("###,###,##0");
        }


        @Override
        public String getFormattedValue(float value) {
            // "value" represents the position of the label on the axis (x or y)
            return mFormat.format(value);
        }
    }


}
