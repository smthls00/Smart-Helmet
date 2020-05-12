package com.example.smarthelmet;

import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;

public class SeriesDataHolder implements Serializable {

    private LineGraphSeries<DataPoint> lineSeries;
    private BarGraphSeries<DataPoint> barSeries;

    public SeriesDataHolder(LineGraphSeries<DataPoint> lineSeries) {
        this.lineSeries = lineSeries;
    }

    public SeriesDataHolder(BarGraphSeries<DataPoint> barSeries) {
        this.barSeries = barSeries;
    }

    public LineGraphSeries<DataPoint> getLineSeries() {
        if (lineSeries == null)
            return new LineGraphSeries<>();
        else
            return lineSeries;
    }

    public BarGraphSeries<DataPoint> getBarSeries() {
        if (barSeries == null)
            return new BarGraphSeries<>();
        else
            return barSeries;
    }
}