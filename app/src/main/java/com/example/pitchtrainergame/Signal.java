package com.example.pitchtrainergame;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.content.Context;

import androidx.core.content.ContextCompat;

public class Signal {
    private AudioRecord ar = null;
    private int minSize;
    private boolean permitted = false;

    static final int sampleRate = 8000;
    static final int samplesMs = 100;


    private FFT fftCalc;

    public double[] Capture(Context main_context, int length)
    {
        start(main_context);
        try {
            Thread.sleep(samplesMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        short[] buffer = new short[minSize];
        short[] buffer2 = new short[minSize];
        ar.read(buffer, 0, minSize);
        stop();
        //return (new double[]{});
        //return fftCalc.fftCalculator(convertStoD(buffer), convertStoD(buffer2), length);
        return DFT.dftCalculator(convertStoD(buffer), length);
    }

    public static double[] convertStoD(short[] shortData) {
        int size = shortData.length;
        double[] doubleData = new double[size];
        for (int i = 0; i < size; i++) {
            doubleData[i] = shortData[i] / 32768.0;
        }
        return doubleData;
    }
    public void setBuffer(Context main_context)
    {
        if (ContextCompat.checkSelfPermission(main_context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            permitted = true;
            minSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            int temp = 2;
            while(temp < minSize - 1)
            {
                temp*=2;
            }
            minSize = temp;
            fftCalc = new FFT(minSize);
        }
    }
    public void start(Context main_context) {
        if (ContextCompat.checkSelfPermission(main_context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            ar = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);
            ar.startRecording();
        }
    }

    public void stop() {
        if (ar != null && permitted) {
            ar.stop();
        }
    }

    public double getAmplitude() {
        short[] buffer = new short[minSize];
        ar.read(buffer, 0, minSize);
        int max = 0;
        for (short s : buffer)
        {
            if (Math.abs(s) > max)
            {
                max = Math.abs(s);
            }
        }
        //max = buffer.sum();
        return max;
    }
}



