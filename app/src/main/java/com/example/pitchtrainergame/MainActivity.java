package com.example.pitchtrainergame;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.widget.RelativeLayout;
import android.widget.Button;
import android.widget.TextView;
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
import android.view.Gravity;
import android.view.View;
import java.util.Random;

public class MainActivity extends Activity {

    private Signal sProc = new Signal();
    private Button gameButton;
    private TextView freqOut;
    private TextView noteOut;
    private TextView gameOut;
    private BarChart barChart;
    private BarDataSet barDataSet;
    private BarData barData;

    private static boolean gameRunning = false;
    private static boolean gameStopped = true;

    static final int maxDataPts = 600;

    @SuppressLint("ResourceType")
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
        //AssetManager assetManager = getAssets();
        //Bitmap bitmap;
        //try {
        //    InputStream ims1 = assetManager.open("notewheel.jpg");
        //    bitmap = BitmapFactory.decodeStream(ims1);
        //}
        //catch(IOException e) {
        //    e.printStackTrace();
        //    return;
        //}

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        RelativeLayout gameLayout = new RelativeLayout(this);

        //Add the note wheel
        //ImageView gameWheel = new ImageView(this);
        //.setImageBitmap(bitmap);
        //gameWheel.setId(1);
        //RelativeLayout.LayoutParams wheelDetails = new RelativeLayout.LayoutParams(
        //        RelativeLayout.LayoutParams.MATCH_PARENT,
        //        RelativeLayout.LayoutParams.MATCH_PARENT
        //);
        //wheelDetails.addRule(RelativeLayout.CENTER_HORIZONTAL);
        //wheelDetails.addRule(RelativeLayout.CENTER_VERTICAL);

        //Add the frequency display
        noteOut = new TextView(this);
        freqOut = new TextView(this);
        gameOut = new TextView(this);
        noteOut.setText("");
        freqOut.setText("");
        gameOut.setText("");
        noteOut.setId(3);
        noteOut.setGravity(Gravity.CENTER);
        noteOut.setTextSize(60);
        freqOut.setTextSize(40);
        gameOut.setTextSize(60);
        RelativeLayout.LayoutParams noteOutDetails = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        noteOutDetails.addRule(RelativeLayout.CENTER_HORIZONTAL);
        noteOutDetails.addRule(RelativeLayout.CENTER_VERTICAL);
        gameLayout.addView(noteOut, noteOutDetails);

        RelativeLayout.LayoutParams gameOutDetails = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        gameOutDetails.addRule(RelativeLayout.CENTER_HORIZONTAL);
        gameOutDetails.addRule(RelativeLayout.BELOW, noteOut.getId());
        gameOut.setTextColor(Color.DKGRAY);
        gameLayout.addView(gameOut, gameOutDetails);

        RelativeLayout.LayoutParams freqOutDetails = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        freqOutDetails.addRule(RelativeLayout.CENTER_HORIZONTAL);
        freqOutDetails.addRule(RelativeLayout.ABOVE, noteOut.getId());
        gameLayout.addView(freqOut, freqOutDetails);


        //Add the start Button
        gameButton = new Button(this);
        gameButton.setId(2);
        gameButton.setText("Start");
        gameButton.setBackgroundColor(Color.DKGRAY);
        gameLayout.setBackgroundColor(Color.LTGRAY);
        RelativeLayout.LayoutParams buttonDetails = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        buttonDetails.addRule(RelativeLayout.CENTER_HORIZONTAL);
        buttonDetails.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //buttonDetails.setMargins(0,0,700,0);
        gameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!gameRunning && gameStopped) {
                    gameRunning = true;
                    final Thread t = new Thread(this::gameThread);
                    t.start();
                }
                else
                {
                    gameRunning = false;
                }
            }

            private void gameThread() {
                while(gameRunning){
                    gameStopped = false;
                    Random r = new Random();
                    int result = r.nextInt(12-0) + 0;
                    String[] notes = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
                    try {
                        textGameUpdater.setText(notes[result]);
                        textGameUpdaterHandler.post(textGameUpdater);
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                gameStopped = true;
                textGameUpdater.setText("stop");
                textGameUpdaterHandler.post(textGameUpdater);
            }
        });
        gameLayout.addView(gameButton, buttonDetails);

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
        chartDetails.addRule(RelativeLayout.ABOVE, gameButton.getId());
        chartDetails.width = 1000;
        chartDetails.height = 300;
        gameLayout.addView(barChart, chartDetails);

        //gameLayout.addView(gameWheel, wheelDetails);
        setContentView(gameLayout);

        sProc.setBuffer(this);

        //sProc.test();

        final Thread t = new Thread(this::soundThread);
        t.start();
    }

    private void soundThread(){

        while(true){
            double[] out_buffer = sProc.Capture(this, maxDataPts);
            double freq = sProc.GetFreq(out_buffer, 10000);

            textViewUpdater.setText(freq);
            textViewUpdaterHandler.post(textViewUpdater);
            chartUpdater.setVals(out_buffer);
            chartUpdaterHandler.post(chartUpdater);
        }
    };


    TextViewUpdater textViewUpdater = new TextViewUpdater();
    Handler textViewUpdaterHandler = new Handler(Looper.getMainLooper());
    private class TextViewUpdater implements Runnable{
        private double freq;
        @Override
        public void run() {
            if(freq > 1) {
                freqOut.setText(String.valueOf((int)freq));
                noteOut.setText(sProc.getNote(freq));
            }
        }
        public void setText(double freq){
            this.freq = freq;
        }
    }



    chartUpdater chartUpdater = new chartUpdater();
    Handler chartUpdaterHandler = new Handler(Looper.getMainLooper());
    private class chartUpdater implements Runnable{
        private double[] vals;
        @Override
        public void run() {
            for(int i = 0; i < vals.length; i++)
                barDataSet.getEntryForIndex(i).setY( (float)vals[i]/10000f);
            barDataSet.notifyDataSetChanged();
            barChart.notifyDataSetChanged();
            barChart.invalidate();
        }
        public void setVals(double[] vals){
            this.vals = vals;
        }
    }


    TextGameUpdater textGameUpdater = new TextGameUpdater();
    Handler textGameUpdaterHandler = new Handler(Looper.getMainLooper());
    private class TextGameUpdater implements Runnable{
        private String txt;
        @Override
        public void run() {
            if(txt == "stop") {
                gameButton.setText("START");
                gameOut.setText("");
            }
            else {
                gameButton.setText("STOP");
                gameOut.setText("Sing: " + txt);
            }
        }
        public void setText(String txt){
            this.txt = txt;
        }
    }


}

