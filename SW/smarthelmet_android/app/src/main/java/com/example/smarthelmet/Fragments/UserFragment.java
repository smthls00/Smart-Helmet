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
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.smarthelmet.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class UserFragment extends Fragment {

    private final String BTDataIntent = "BTDataIntent";

    TextView dataTv;
    LineChart bpmChart;
    LineChart tempChart;
    PieChart actChart;
    BarChart stepsChart;


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
        dataTv = view.findViewById(R.id.dataTv);

        bpmChart = view.findViewById(R.id.bpmChart);
        stepsChart = view.findViewById(R.id.stepsChart);
        actChart = view.findViewById(R.id.actChart);
        tempChart = view.findViewById(R.id.tempChart);


        bpmChart_create();
        stepsChart_create();
        actChart_create();
        tempChart_create();

        return view;
    }

    private BroadcastReceiver BTDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(BTDataIntent);
            Log.d("receiver", "Got message: " + message);

            dataTv.append(message + " ");
        }
    };

    private void tempChart_create() {
        ArrayList<Entry> values = new ArrayList<>();

        values.add(new Entry(0, 36.6f));
        values.add(new Entry(1, 36.7f));
        values.add(new Entry(2, 36.0f));
        values.add(new Entry(3, 36.8f));
        values.add(new Entry(4, 37f));
        values.add(new Entry(5, 37.1f));
        values.add(new Entry(6, 37f));
        values.add(new Entry(7, 36.8f));
        values.add(new Entry(9, 36.7f));

        LineDataSet set1;


        if (tempChart.getData() != null &&
                tempChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) tempChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            tempChart.getData().notifyDataChanged();
            tempChart.notifyDataSetChanged();
        } else {

            set1 = new LineDataSet(values, "Temp");
            set1.setDrawIcons(false);
            //set1.enableDashedLine(10f, 5f, 0f);
            //set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLUE);
            set1.setCircleColor(Color.CYAN);
            set1.setLineWidth(1.5f);
            set1.setCircleRadius(1f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(false);
            set1.setFormLineWidth(1f);
            set1.setDrawValues(false);
            set1.setHighlightEnabled(true);
            set1.setDrawVerticalHighlightIndicator(false);
            //set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            //set1.setFormSize(15.f);

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            LineData data = new LineData(dataSets);

            tempChart.setDrawBorders(false);
            tempChart.setData(data);
            tempChart.setTouchEnabled(true);
            tempChart.setPinchZoom(false);
            tempChart.setDescription(null);
            tempChart.setDoubleTapToZoomEnabled(false);

            IMarker markerOnTap = new MarkerOnTapTemp(getActivity(), R.layout.graph_content_viewer);
            tempChart.setMarker(markerOnTap);
            tempChart.setHighlightPerTapEnabled(true);

            //Axis
            tempChart.getXAxis().setDrawGridLines(false);
            tempChart.getAxisLeft().setDrawGridLines(false);
            tempChart.getAxisRight().setDrawGridLines(false);
            tempChart.getAxisLeft().setDrawLabels(true);
            tempChart.getAxisRight().setDrawLabels(false);

            tempChart.getXAxis().setDrawLabels(false);
            tempChart.getAxisLeft().setAxisMaximum(45);
            tempChart.getAxisLeft().setAxisMinimum(30);


            XAxis xAxis = tempChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setEnabled(true);
            xAxis.setDrawLabels(true);

            xAxis.setValueFormatter(new TimeFormatter());
            YAxis yAxis = tempChart.getAxisLeft();
            yAxis.setValueFormatter(new TemperatureFormatter());
            yAxis.setEnabled(true);
            YAxis yAxis2 = tempChart.getAxisRight();

            yAxis2.setEnabled(false);

            //Legend
            tempChart.getLegend().setEnabled(false);
        }
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
            set1.setColor(getContext().getColor(R.color.lightColor));
            set1.setValueFormatter(new IntegerFormatter());


            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);

            stepsChart.setData(data);
        }
    }

    private void bpmChart_create() {
        ArrayList<Entry> values = new ArrayList<>();

        values.add(new Entry(0, 60));
        values.add(new Entry(4, 77));
        values.add(new Entry(5, 120));
        values.add(new Entry(6, 60));
        values.add(new Entry(7 + 1, 55));
        values.add(new Entry(8 + 2, 97));
        values.add(new Entry(9 + 3, 59));
        values.add(new Entry(10 + 4, 77));
        values.add(new Entry(11 + 5, 120));

        LineDataSet set1;

        if (bpmChart.getData() != null &&
                bpmChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) bpmChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            bpmChart.getData().notifyDataChanged();
            bpmChart.notifyDataSetChanged();
        } else {

            set1 = new LineDataSet(values, "BPM");
            set1.setDrawIcons(false);
            //set1.enableDashedLine(10f, 5f, 0f);
            //set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.RED);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1.5f);
            set1.setCircleRadius(1f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(false);
            set1.setFormLineWidth(1f);
            set1.setDrawValues(false);
            set1.setHighlightEnabled(true);
            set1.setDrawVerticalHighlightIndicator(false);
            //set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            //set1.setFormSize(15.f);

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            LineData data = new LineData(dataSets);

            bpmChart.setDrawBorders(false);
            bpmChart.setData(data);
            bpmChart.setTouchEnabled(true);
            bpmChart.setPinchZoom(false);
            bpmChart.setDescription(null);
            bpmChart.setDoubleTapToZoomEnabled(false);

            IMarker markerOnTap = new MarkerOnTap(getActivity(), R.layout.graph_content_viewer);
            bpmChart.setMarker(markerOnTap);
            bpmChart.setHighlightPerTapEnabled(true);

            //Axis
            bpmChart.getXAxis().setDrawGridLines(false);
            bpmChart.getAxisLeft().setDrawGridLines(false);
            bpmChart.getAxisRight().setDrawGridLines(false);
            bpmChart.getAxisLeft().setDrawLabels(true);
            bpmChart.getAxisRight().setDrawLabels(false);
            bpmChart.getXAxis().setDrawLabels(false);
            bpmChart.getAxisLeft().setAxisMaximum(200);
            bpmChart.getAxisLeft().setAxisMinimum(20);

            XAxis xAxis = bpmChart.getXAxis();
            xAxis.setAxisMinimum(0);
            xAxis.setAxisMaximum(20);
            xAxis.setEnabled(false);
            YAxis yAxis = bpmChart.getAxisLeft();
            yAxis.setEnabled(true);
            YAxis yAxis2 = bpmChart.getAxisRight();
            yAxis2.setEnabled(false);

            //Legend
            bpmChart.getLegend().setEnabled(false);
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
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(pieDataSet);

        actChart.setData(pieData);
        actChart.setEntryLabelTextSize(10f);
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
