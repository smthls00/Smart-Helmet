package com.example.smarthelmet.Fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.smarthelmet.R;
import com.example.smarthelmet.SeriesDataHolder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import static com.example.smarthelmet.Constants.BTDataIntent;
import static com.example.smarthelmet.Constants.BTUserIntent;
import static com.example.smarthelmet.Constants.actCommand;
import static com.example.smarthelmet.Constants.bpmCommand;
import static com.example.smarthelmet.Constants.stepsCommand;
import static com.example.smarthelmet.Constants.userFragmentTag;
import static com.example.smarthelmet.Constants.utpCommand;
import static com.example.smarthelmet.Constants.zoomMessageBundle;
import static com.example.smarthelmet.Constants.zoomSeriesBundle;

public class UserFragment extends Fragment implements View.OnClickListener {

    GraphView actChart;
    BarGraphSeries<DataPoint> actSeries;

    GraphView stepsChart;
    BarGraphSeries<DataPoint> stepsSeries;


    GraphView tmpChart;
    LineGraphSeries<DataPoint> tmpSeries;

    GraphView bpmChart;
    LineGraphSeries<DataPoint> bpmSeries;

//    int height;
//    int width;
//    int minHeight;
//    int minWidth;
//
//    CardView bpmCard;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(BTDataReceiver,
                new IntentFilter(BTDataIntent));

        tmpSeries = new LineGraphSeries<>();
        bpmSeries = new LineGraphSeries<>();

        actSeries = new BarGraphSeries<>(new DataPoint[]{
                new DataPoint(1, 14),
                new DataPoint(2, 20),
                new DataPoint(3, 45),
                new DataPoint(4, 5),
                new DataPoint(5, 10),
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


//        WindowManager windowmanager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics dimension = new DisplayMetrics();
//        windowmanager.getDefaultDisplay().getMetrics(dimension);
//        height = dimension.heightPixels;
//        width = dimension.widthPixels;

        super.onCreate(savedInstanceState);
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

        bpmChart_create();
        stepsChart_create();
        actChart_create();
        tmpChart_create();

//        bpmCard = view.findViewById(R.id.bpmCard);
//
//        bpmCard.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                bpmCard.getViewTreeObserver().removeOnPreDrawListener(this);
//                minHeight = bpmCard.getHeight();
//                minWidth = bpmCard.getWidth();
//                ViewGroup.LayoutParams layoutParams = bpmCard.getLayoutParams();
//                layoutParams.height = minHeight;
//                layoutParams.width = minWidth;
//
//                bpmCard.setLayoutParams(layoutParams);
//
//                return true;
//            }
//        });


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
                return Color.rgb((int) Math.abs(data.getY() * 255 / 2), 200, (int) Math.abs(data.getY() * 255 / 4));
            }
        });

        stepsSeries.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) Math.abs(data.getY() * 255 / 8), 140, 255);
            }
        });

        tmpChart.setOnClickListener(this);
        bpmChart.setOnClickListener(this);
        actChart.setOnClickListener(this);
        stepsChart.setOnClickListener(this);

        return view;
    }



//    private void toggleCardViewnHeight(int height) {
//
//        if (bpmCard.getHeight() == minHeight) {
//            // expand
//
//            expandView(height); //'height' is the height of screen which we have measured already.
//
//        } else {
//            // collapse
//            collapseView();
//
//        }
//    }
//
//    public void collapseView() {
//
//        ValueAnimator anim = ValueAnimator.ofInt(bpmCard.getMeasuredHeightAndState(),
//                minHeight);
//        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                int val = (Integer) valueAnimator.getAnimatedValue();
//                ViewGroup.LayoutParams layoutParams = bpmCard.getLayoutParams();
//                layoutParams.height = val;
//                bpmCard.setLayoutParams(layoutParams);
//
//            }
//        });
//        anim.start();
//    }
//    public void expandView(int height) {
//
//        ValueAnimator anim = ValueAnimator.ofInt(bpmCard.getMeasuredHeightAndState(),
//                height);
//        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                int val = (Integer) valueAnimator.getAnimatedValue();
//                ViewGroup.LayoutParams layoutParams = bpmCard.getLayoutParams();
//                layoutParams.height = val;
//                bpmCard.setLayoutParams(layoutParams);
//            }
//        });
//        anim.start();
//    }
//
//    private Animator getViewScaleAnimator() {
//        // height resize animation
//        AnimatorSet animatorSet = new AnimatorSet();
//        ValueAnimator heightAnimator = ValueAnimator.ofInt(minHeight, height);
//        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                ViewGroup.LayoutParams params = bpmCard.getLayoutParams();
//                params.height = (int) animation.getAnimatedValue();
//                bpmCard.setLayoutParams(params);
//            }
//        });
//        animatorSet.play(heightAnimator);
//
//        ValueAnimator widthAnimator = ValueAnimator.ofInt(minWidth, width);
//        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                ViewGroup.LayoutParams params = bpmCard.getLayoutParams();
//                params.width = (int) animation.getAnimatedValue();
//                bpmCard.setLayoutParams(params);
//            }
//        });
//        animatorSet.play(widthAnimator);
//        return animatorSet;
//    }


    private BroadcastReceiver BTDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(BTUserIntent);

            if (message == null)
                return;

            Log.d("receiverUser", "message: " + message);

            try {
                float tmpVal = Float.parseFloat(message.substring(message.indexOf(utpCommand) + 1, message.indexOf(bpmCommand)));
                float bpmVal = Float.parseFloat(message.substring(message.indexOf(bpmCommand) + 1));


                tmpSeries.appendData(new DataPoint(tmpSeries.getHighestValueX() + 0.1, tmpVal), true, 60 * 10);
                bpmSeries.appendData(new DataPoint(bpmSeries.getHighestValueX() + 0.1, bpmVal), true, 60 * 10);

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

        stepsChart.getViewport().scrollToEnd();
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

        actChart.getViewport().scrollToEnd();
    }


    private void tmpChart_create() {
        tmpSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorTmp));
        tmpSeries.setThickness(6);

        //tmpChart.getViewport().setScalable(true);
        //tmpChart.getViewport().setScalableY(true);

        tmpChart.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        tmpChart.getGridLabelRenderer().setNumVerticalLabels(10);
        tmpChart.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        tmpChart.getGridLabelRenderer().setGridColor(ContextCompat.getColor(getActivity(), R.color.textColor));
        tmpChart.getGridLabelRenderer().setTextSize(25f);

        tmpChart.getViewport().setXAxisBoundsManual(true);
        tmpChart.getViewport().setMinX(0);
        tmpChart.getViewport().setMaxX(6);
        tmpChart.getViewport().setYAxisBoundsManual(true);
        tmpChart.getViewport().setMaxY(50);
        tmpChart.getViewport().setMinY(-20);

        tmpChart.addSeries(tmpSeries);

        tmpChart.getViewport().scrollToEnd();
    }

    private void bpmChart_create() {
        bpmSeries.setColor(ContextCompat.getColor(getActivity(), R.color.colorBPM));
        bpmSeries.setThickness(6);

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
        bpmChart.getViewport().setMaxX(6);
        bpmChart.getViewport().setYAxisBoundsManual(true);
        bpmChart.getViewport().setMaxY(180);
        bpmChart.getViewport().setMinY(0);

        bpmChart.addSeries(bpmSeries);

        bpmChart.getViewport().scrollToEnd();
    }

    @Override
    public void onClick(View v) {
        Log.d("onClickUserFragment", "onClick");

        Bundle zoomBundle = new Bundle();
        Fragment zoomFragment = new ZoomFragment();

        switch (v.getId()) {

            case R.id.bpmChart:
                //toggleCardViewnHeight(height);
                //getViewScaleAnimator().setDuration(100).start();
                zoomBundle.putString(zoomMessageBundle, bpmCommand);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(bpmSeries));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        addToBackStack(userFragmentTag).
                        replace(R.id.frame_container, zoomFragment).
                        commitAllowingStateLoss();
                break;

            case R.id.tmpChart:
                zoomBundle.putString(zoomMessageBundle, utpCommand);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(tmpSeries));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragment).
                        addToBackStack(userFragmentTag).
                        commitAllowingStateLoss();
                break;

            case R.id.actChart:
                zoomBundle.putString(zoomMessageBundle, actCommand);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(actSeries));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragment).
                        addToBackStack(userFragmentTag).
                        commitAllowingStateLoss();
                break;

            case R.id.stepsChart:
                zoomBundle.putString(zoomMessageBundle, stepsCommand);
                zoomBundle.putSerializable(zoomSeriesBundle, new SeriesDataHolder(stepsSeries));
                zoomFragment.setArguments(zoomBundle);

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_container, zoomFragment).
                        addToBackStack(userFragmentTag).
                        commitAllowingStateLoss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {

        Log.d(userFragmentTag, "onDestroy");

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(BTDataReceiver);

        super.onDestroy();
    }
}
