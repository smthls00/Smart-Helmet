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

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class UserFragment extends Fragment {

    private final String BTDataIntent = "BTDataIntent";

    //TextView dataTv;


    PieChart actChart;
    BarChart stepsChart;

    LineChart bpmChart;
    LineDataSet bpmSet;
    LineData bpmData;

    LineChart tmpChart;
    LineDataSet tmpSet;
    LineData tmpData;



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

                float opVal = Float.parseFloat(message.substring(message.indexOf(":") + 1));

                Log.d("receiver", "Got opCode: " + opCode + ", opVal: " + opVal);

                switch (opCode) {
                    case "bpm":
                        bpmUpdate(opVal);
                        break;
                    case "tmp":
                        tmpUpdate(opVal);
                        break;
                }


            } catch (Exception e) {
                Log.d("exceptionUserFragmentBT", e.toString());
            }


        }
    };


    private void bpmUpdate(final float bpmVal) {
        int bpmMinute = bpmSet.getEntryCount();

        if (bpmMinute >= 60) {

            bpmMinute = 60;
            bpmSet.removeFirst();
            for (Entry entry : bpmSet.getValues())
                entry.setX(entry.getX() - 1);
        }

        bpmSet.addEntry(new Entry(bpmMinute, bpmVal));

        Log.d("subVal", "Got subValTemp: " + bpmVal + ", per Hour: " + bpmMinute);

        bpmData.addDataSet(bpmSet);
        bpmChart.notifyDataSetChanged();
        bpmChart.moveViewToX(bpmMinute);
    }

    private void tmpUpdate(final float tmpVal) {
        int tmpHour = tmpSet.getEntryCount();

        if (tmpHour >= 30) {

            tmpHour = 30;
            tmpSet.removeFirst();
            for (Entry entry : tmpSet.getValues())
                entry.setX(entry.getX() - 1);
        }

        tmpSet.addEntry(new Entry(tmpHour, tmpVal));

        Log.d("subVal", "Got subValTemp: " + tmpVal + ", per Hour: " + tmpHour);

        tmpData.addDataSet(tmpSet);
        tmpChart.notifyDataSetChanged();
        tmpChart.moveViewToX(tmpHour);
    }

    private void tmpChart_create() {

            tmpSet = new LineDataSet(null, "TMP");
            tmpSet.setColor(getContext().getColor(R.color.colorTmp));
            tmpSet.setDrawCircles(false);
            tmpSet.setLineWidth(2f);
            tmpSet.setDrawValues(false);
            tmpSet.setDrawVerticalHighlightIndicator(false);
            tmpData = new LineData(tmpSet);

            tmpChart.setDrawBorders(false);
            tmpChart.setData(tmpData);
            tmpChart.setTouchEnabled(true);
            tmpChart.setPinchZoom(false);
            tmpChart.setDescription(null);
            tmpChart.setDoubleTapToZoomEnabled(false);

            IMarker markerOnTap = new MarkerOnTapTemp(getActivity(), R.layout.graph_content_viewer);
            tmpChart.setMarker(markerOnTap);
            tmpChart.setHighlightPerTapEnabled(true);


            XAxis xAxis = tmpChart.getXAxis();
            xAxis.setDrawLabels(false);
            xAxis.setDrawGridLines(false);
            xAxis.setAxisMinimum(0);
            xAxis.setAxisMaximum(30);
            xAxis.setEnabled(false);
            xAxis.setValueFormatter(new TimeFormatter());

            YAxis yAxis = tmpChart.getAxisLeft();
            yAxis.setAxisMaximum(45f);
            yAxis.setAxisMinimum(25f);
            yAxis.setDrawGridLines(false);
            yAxis.setDrawLabels(true);
            yAxis.setEnabled(true);
            yAxis.setValueFormatter(new TemperatureFormatter());
            yAxis.setTextColor(getContext().getColor(R.color.textColor));

            YAxis yAxis2 = tmpChart.getAxisRight();
            yAxis2.setDrawLabels(false);
            yAxis2.setDrawGridLines(false);
            yAxis2.setEnabled(false);

            //Legend
            tmpChart.getLegend().setEnabled(false);
            tmpChart.fitScreen();
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

    private void bpmChart_create() {
            bpmSet = new LineDataSet(null, "BPM");
            bpmSet.setColor(getContext().getColor(R.color.colorBPM));
            bpmSet.setDrawCircles(false);
            bpmSet.setLineWidth(2f);
            bpmSet.setDrawValues(false);
            bpmSet.setDrawVerticalHighlightIndicator(false);
            bpmData = new LineData(bpmSet);

            bpmChart.setDrawBorders(false);
            bpmChart.setData(bpmData);
            bpmChart.setTouchEnabled(true);
            bpmChart.setPinchZoom(false);
            bpmChart.setDescription(null);
            bpmChart.setDoubleTapToZoomEnabled(false);

            IMarker markerOnTap = new MarkerOnTap(getActivity(), R.layout.graph_content_viewer);
            bpmChart.setMarker(markerOnTap);
            bpmChart.setHighlightPerTapEnabled(true);

            XAxis xAxis = bpmChart.getXAxis();
            xAxis.setDrawLabels(false);
            xAxis.setDrawGridLines(false);
            xAxis.setAxisMinimum(0);
            xAxis.setAxisMaximum(60);
            xAxis.setEnabled(false);
            xAxis.setValueFormatter(new TimeFormatter());

            YAxis yAxis = bpmChart.getAxisLeft();
            yAxis.setAxisMaximum(180);
            yAxis.setAxisMinimum(30);
            yAxis.setDrawGridLines(false);
            yAxis.setDrawLabels(true);
            yAxis.setEnabled(true);
            yAxis.setValueFormatter(new IntegerFormatter());
            yAxis.setTextColor(getContext().getColor(R.color.textColor));

            YAxis yAxis2 = bpmChart.getAxisRight();
            yAxis2.setDrawLabels(false);
            yAxis2.setDrawGridLines(false);
            yAxis2.setEnabled(false);

            //Legend
            bpmChart.getLegend().setEnabled(false);
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

    public class TimeFormatter extends ValueFormatter {

        private DecimalFormat mFormat;

        public TimeFormatter() {

            // format values to 1 decimal digit
            mFormat = new DecimalFormat("###,###,##0");
        }


        @Override
        public String getFormattedValue(float value) {
            // "value" represents the position of the label on the axis (x or y)
            if (value >= 0 && value < 10)
                return '0' + mFormat.format(value) + ":00";
            else
                return mFormat.format(value) + ":00";
        }
    }

    public class TemperatureFormatter extends ValueFormatter {

        private DecimalFormat mFormat;

        public TemperatureFormatter() {

            // format values to 1 decimal digit
            mFormat = new DecimalFormat("###,###,##0");
        }


        @Override
        public String getFormattedValue(float value) {
            // "value" represents the position of the label on the axis (x or y)
            return mFormat.format(value) + "°C";
        }
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


    public class MarkerOnTap extends MarkerView {

        private TextView tvContent;
        private MPPointF mOffset;

        public MarkerOnTap(Context context, int layoutResource) {
            super(context, layoutResource);

            // find your layout components
            tvContent = findViewById(R.id.tvContent);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
// content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {

            tvContent.setText("" + (int) e.getY());

            // this will perform necessary layouting
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {

            if (mOffset == null) {
                // center the marker horizontally and vertically
                mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
            }

            return mOffset;
        }
    }

    public class MarkerOnTapTemp extends MarkerView {

        private TextView tvContent;
        private MPPointF mOffset;

        public MarkerOnTapTemp(Context context, int layoutResource) {
            super(context, layoutResource);

            // find your layout components
            tvContent = findViewById(R.id.tvContent);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
// content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {

            if (e.getX() >= 0 && e.getX() < 10)
                tvContent.setText("" + e.getY() + "°C" + "; 0" + (int) e.getX() + ":00");
            else
                tvContent.setText("" + e.getY() + "°C" + "; " + (int) e.getX() + ":00");

            // this will perform necessary layouting
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {

            if (mOffset == null) {
                // center the marker horizontally and vertically
                mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
            }

            return mOffset;
        }
    }
}
