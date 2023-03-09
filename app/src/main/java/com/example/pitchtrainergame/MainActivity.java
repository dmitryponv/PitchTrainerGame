package com.example.pitchtrainergame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.widget.RelativeLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Color;
import android.content.res.AssetManager;
import java.io.InputStream;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private Signal sProc = new Signal();
    private Button gameButton;
    private BarChart barChart;
    private BarDataSet barDataSet;
    private BarData barData;

    static final int maxDataPts = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int MY_PERMISSIONS_REQUEST_MICROPHONE=0;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO))
            {

            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_MICROPHONE );
            }
        }

        //Load Image
        AssetManager assetManager = getAssets();
        Bitmap bitmap;
        try {
            InputStream ims1 = assetManager.open("notewheel.jpg");
            bitmap = BitmapFactory.decodeStream(ims1);
        }
        catch(IOException e) {
            e.printStackTrace();
            return;
        }

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        RelativeLayout gameLayout = new RelativeLayout(this);
        gameButton = new Button(this);
        ImageView gameWheel = new ImageView(this);
        gameWheel.setImageBitmap(bitmap);

        //Add the note wheel
        //gameWheel.setId(1);
        RelativeLayout.LayoutParams wheelDetails = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        wheelDetails.addRule(RelativeLayout.CENTER_HORIZONTAL);
        wheelDetails.addRule(RelativeLayout.CENTER_VERTICAL);

        //Add the start Button
        //gameWheel.setId(2);
        gameButton.setText("Start");
        gameButton.setBackgroundColor(Color.MAGENTA);
        gameLayout.setBackgroundColor(Color.LTGRAY);
        RelativeLayout.LayoutParams buttonDetails = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        buttonDetails.addRule(RelativeLayout.CENTER_HORIZONTAL);
        buttonDetails.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //buttonDetails.setMargins(0,0,700,0);

        //Add a bar chart for FFT
        barChart = new BarChart(this);
        // creating a new bar data set.
        ArrayList barEntriesArrayList = new ArrayList<>();
        for(int i = 0; i <maxDataPts; i++)
           barEntriesArrayList.add(new BarEntry((float)i, (float)(0)));
        barDataSet = new BarDataSet(barEntriesArrayList, "");
        barData = new BarData(barDataSet);
        barChart.setData(barData);
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        //barDataSet.setValueTextColor(Color.BLACK);
        //barDataSet.setValueTextSize(16f);
        barChart.getAxisLeft().setAxisMinimum(0F);
        barChart.getAxisLeft().setAxisMaximum(3F);
        barChart.getDescription().setEnabled(false);
        RelativeLayout.LayoutParams chartDetails = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        chartDetails.addRule(RelativeLayout.CENTER_HORIZONTAL);
        chartDetails.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        chartDetails.width = 1000;
        chartDetails.height = 300;

        gameLayout.addView(gameWheel, wheelDetails);
        gameLayout.addView(barChart, chartDetails);
        gameLayout.addView(gameButton, buttonDetails);
        setContentView(gameLayout);

        sProc.setBuffer(this);

        final Thread t = new Thread(this::soundThread);
        t.start();
    }

    private void soundThread(){

        //for(int i = 0; i < 10; i++) {
        //    sProc.start(this);
        //    try {
        //        Thread.sleep(1000);
        //        double tt = sProc.getAmplitude();
        //        textViewUpdater.setText(String.valueOf(tt));
        //        textViewUpdaterHandler.post(textViewUpdater);
        //    } catch (InterruptedException e) {
        //        throw new RuntimeException(e);
        //    }
        //    sProc.stop();
        //}

        while(true){
            double[] out_buffer = sProc.Capture(this, maxDataPts);
            double max_val = 0;
            int max_idx = 0;
            for(int idx = 0; idx < out_buffer.length; idx++)
            {
                if(out_buffer[idx] > max_val)
                {
                    max_val = out_buffer[idx];
                    max_idx = idx;
                }
            }
            max_val = sProc.getAmplitude();
            textViewUpdater.setText(String.valueOf(max_idx));
            textViewUpdaterHandler.post(textViewUpdater);
            chartUpdater.setVals(out_buffer);
            chartUpdaterHandler.post(chartUpdater);
        }
    };


    TextViewUpdater textViewUpdater = new TextViewUpdater();
    Handler textViewUpdaterHandler = new Handler(Looper.getMainLooper());
    private class TextViewUpdater implements Runnable{
        private String txt;
        @Override
        public void run() {
            gameButton.setText(txt);
        }
        public void setText(String txt){
            this.txt = txt;
        }
    }

    chartUpdater chartUpdater = new chartUpdater();
    Handler chartUpdaterHandler = new Handler(Looper.getMainLooper());
    private class chartUpdater implements Runnable{
        private double[] vals;
        @Override
        public void run() {
            for(int i = 0; i < vals.length; i++)
                barDataSet.getEntryForIndex(i).setY( (float)vals[i]/1000f);
            barDataSet.notifyDataSetChanged();
            barChart.notifyDataSetChanged();
            barChart.invalidate();
        }
        public void setVals(double[] vals){
            this.vals = vals;
        }
    }


}

