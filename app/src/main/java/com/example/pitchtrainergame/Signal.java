package com.example.pitchtrainergame;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.content.Context;

import androidx.core.content.ContextCompat;

import java.lang.Math;
public class Signal {
    private AudioRecord ar = null;
    private int minSize;
    private boolean permitted = false;

    static final int sampleRate = 80000;
    static final int samplesMs = 10;

    public void test()
    {
        FFT.main_test(64);
    }

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
        return FFT.fftCalculator(minSize, convertStoD(buffer), convertStoD(buffer2), length);
        //return DFT.dftCalculator(convertStoD(buffer), length);
    }

    public static double[] convertStoD(short[] shortData) {
        int size = shortData.length;
        double[] doubleData = new double[size];
        for (int i = 2; i < size-2; i++) { //Add a low pass filter
            doubleData[i] = (shortData[i-2]+shortData[i-1]+shortData[i]+shortData[i+1]+shortData[i+2]) / (5*32768.0);
        }
        //filter(doubleData, size);
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
    public double GetFreq(double[] out_buffer, int threshold)
    {
        double max_val = 0;
        double max_idx = 0;
        for(int idx = 0; idx < out_buffer.length; idx++)
        {
            if(out_buffer[idx] > max_val)
            {
                max_val = out_buffer[idx];
                max_idx = idx;
            }
        }
        if(max_val < threshold)
            return 0;

        //Interpolate
        max_idx =  42.609 * Math.exp(0.0403 * max_idx);

        return max_idx;
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

    public String getNote(double freq) {
        double position = 0;
        String note = "";
        final double[] octave2 = {
                65.41,
                69.30,
                73.42,
                77.78,
                82.41,
                87.31,
                92.50,
                98.00,
                103.83,
                110.00,
                116.54,
                123.47
        };
        if(freq > (octave2[0] - (octave2[1] - octave2[0])/2) && freq < (octave2[1] + octave2[0])/2)
        {note = "C2"; position = (freq - octave2[0]) / (2*(octave2[1] - octave2[0]));}
        else if(freq > (octave2[1] + octave2[0])/2 && freq < (octave2[1] + octave2[2])/2)
        {note = "C#2"; position = (freq - octave2[1]) / ((octave2[2] - octave2[0]));}
        else if(freq > (octave2[2] + octave2[1])/2 && freq < (octave2[2] + octave2[3])/2)
        {note = "D2"; position = (freq - octave2[2]) / ((octave2[3] - octave2[1]));}
        else if(freq > (octave2[3] + octave2[2])/2 && freq < (octave2[3] + octave2[4])/2)
        {note = "D#2"; position = (freq - octave2[3]) / ((octave2[4] - octave2[2]));}
        else if(freq > (octave2[4] + octave2[3])/2 && freq < (octave2[4] + octave2[5])/2)
        {note = "E2"; position = (freq - octave2[4]) / ((octave2[5] - octave2[3]));}
        else if(freq > (octave2[5] + octave2[4])/2 && freq < (octave2[5] + octave2[6])/2)
        {note = "F2"; position = (freq - octave2[5]) / ((octave2[6] - octave2[4]));}
        else if(freq > (octave2[6] + octave2[5])/2 && freq < (octave2[6] + octave2[7])/2)
        {note = "F#2"; position = (freq - octave2[6]) / ((octave2[7] - octave2[5]));}
        else if(freq > (octave2[7] + octave2[6])/2 && freq < (octave2[7] + octave2[8])/2)
        {note = "G2"; position = (freq - octave2[7]) / ((octave2[8] - octave2[6]));}
        else if(freq > (octave2[8] + octave2[7])/2 && freq < (octave2[8] + octave2[9])/2)
        {note = "G#2"; position = (freq - octave2[8]) / ((octave2[9] - octave2[7]));}
        else if(freq > (octave2[9] + octave2[8])/2 && freq < (octave2[9] + octave2[10])/2)
        {note = "A2"; position = (freq - octave2[9]) / ((octave2[10] - octave2[8]));}
        else if(freq > (octave2[10] + octave2[9])/2 && freq < (octave2[10] + octave2[11])/2)
        {note = "A#2"; position = (freq - octave2[10]) / ((octave2[11] - octave2[9]));}
        else if(freq > (octave2[11] + octave2[10])/2 && freq < (octave2[11] + (octave2[11] - octave2[10])/2))
        {note = "B2"; position = (freq - octave2[11]) / (2*(octave2[11] - octave2[2]));}


        final double[] octave3 = {
                130.81,
                138.59,
                146.83,
                155.56,
                164.81,
                174.61,
                185.00,
                196.00,
                207.65,
                220.00,
                233.08,
                246.94
        };
        if(freq > (octave3[0] - (octave3[1] - octave3[0])/2) && freq < (octave3[1] + octave3[0])/2)
        {note = "C3"; position = (freq - octave3[0]) / (2*(octave3[1] - octave3[0]));}
        else if(freq > (octave3[1] + octave3[0])/2 && freq < (octave3[1] + octave3[2])/2)
        {note = "C#3"; position = (freq - octave3[1]) / ((octave3[2] - octave3[0]));}
        else if(freq > (octave3[2] + octave3[1])/2 && freq < (octave3[2] + octave3[3])/2)
        {note = "D3"; position = (freq - octave3[2]) / ((octave3[3] - octave3[1]));}
        else if(freq > (octave3[3] + octave3[2])/2 && freq < (octave3[3] + octave3[4])/2)
        {note = "D#3"; position = (freq - octave3[3]) / ((octave3[4] - octave3[2]));}
        else if(freq > (octave3[4] + octave3[3])/2 && freq < (octave3[4] + octave3[5])/2)
        {note = "E3"; position = (freq - octave3[4]) / ((octave3[5] - octave3[3]));}
        else if(freq > (octave3[5] + octave3[4])/2 && freq < (octave3[5] + octave3[6])/2)
        {note = "F3"; position = (freq - octave3[5]) / ((octave3[6] - octave3[4]));}
        else if(freq > (octave3[6] + octave3[5])/2 && freq < (octave3[6] + octave3[7])/2)
        {note = "F#3"; position = (freq - octave3[6]) / ((octave3[7] - octave3[5]));}
        else if(freq > (octave3[7] + octave3[6])/2 && freq < (octave3[7] + octave3[8])/2)
        {note = "G3"; position = (freq - octave3[7]) / ((octave3[8] - octave3[6]));}
        else if(freq > (octave3[8] + octave3[7])/2 && freq < (octave3[8] + octave3[9])/2)
        {note = "G#3"; position = (freq - octave3[8]) / ((octave3[9] - octave3[7]));}
        else if(freq > (octave3[9] + octave3[8])/2 && freq < (octave3[9] + octave3[10])/2)
        {note = "A3"; position = (freq - octave3[9]) / ((octave3[10] - octave3[8]));}
        else if(freq > (octave3[10] + octave3[9])/2 && freq < (octave3[10] + octave3[11])/2)
        {note = "A#3"; position = (freq - octave3[10]) / ((octave3[11] - octave3[9]));}
        else if(freq > (octave3[11] + octave3[10])/2 && freq < (octave3[11] + (octave3[11] - octave3[10])/2))
        {note = "B3"; position = (freq - octave3[11]) / (2*(octave3[11] - octave3[2]));}


        final double[] octave4 = {
                261.63,
                277.18,
                293.66,
                311.13,
                329.63,
                349.23,
                369.99,
                392.00,
                415.30,
                440.00,
                466.16,
                493.88
        };
        if(freq > (octave4[0] - (octave4[1] - octave4[0])/2) && freq < (octave4[1] + octave4[0])/2)
        {note = "C4"; position = (freq - octave4[0]) / (2*(octave4[1] - octave4[0]));}
        else if(freq > (octave4[1] + octave4[0])/2 && freq < (octave4[1] + octave4[2])/2)
        {note = "C#4"; position = (freq - octave4[1]) / ((octave4[2] - octave4[0]));}
        else if(freq > (octave4[2] + octave4[1])/2 && freq < (octave4[2] + octave4[3])/2)
        {note = "D4"; position = (freq - octave4[2]) / ((octave4[3] - octave4[1]));}
        else if(freq > (octave4[3] + octave4[2])/2 && freq < (octave4[3] + octave4[4])/2)
        {note = "D#4"; position = (freq - octave4[3]) / ((octave4[4] - octave4[2]));}
        else if(freq > (octave4[4] + octave4[3])/2 && freq < (octave4[4] + octave4[5])/2)
        {note = "E4"; position = (freq - octave4[4]) / ((octave4[5] - octave4[3]));}
        else if(freq > (octave4[5] + octave4[4])/2 && freq < (octave4[5] + octave4[6])/2)
        {note = "F4"; position = (freq - octave4[5]) / ((octave4[6] - octave4[4]));}
        else if(freq > (octave4[6] + octave4[5])/2 && freq < (octave4[6] + octave4[7])/2)
        {note = "F#4"; position = (freq - octave4[6]) / ((octave4[7] - octave4[5]));}
        else if(freq > (octave4[7] + octave4[6])/2 && freq < (octave4[7] + octave4[8])/2)
        {note = "G4"; position = (freq - octave4[7]) / ((octave4[8] - octave4[6]));}
        else if(freq > (octave4[8] + octave4[7])/2 && freq < (octave4[8] + octave4[9])/2)
        {note = "G#4"; position = (freq - octave4[8]) / ((octave4[9] - octave4[7]));}
        else if(freq > (octave4[9] + octave4[8])/2 && freq < (octave4[9] + octave4[10])/2)
        {note = "A4"; position = (freq - octave4[9]) / ((octave4[10] - octave4[8]));}
        else if(freq > (octave4[10] + octave4[9])/2 && freq < (octave4[10] + octave4[11])/2)
        {note = "A#4"; position = (freq - octave4[10]) / ((octave4[11] - octave4[9]));}
        else if(freq > (octave4[11] + octave4[10])/2 && freq < (octave4[11] + (octave4[11] - octave4[10])/2))
        {note = "B4"; position = (freq - octave4[11]) / (2*(octave4[11] - octave4[2]));}


        final double[] octave5 = {
                523.25,
                554.37,
                587.33,
                622.25,
                659.25,
                698.46,
                739.99,
                783.99,
                830.61,
                880.00,
                932.33,
                987.77
        };
        if(freq > (octave5[0] - (octave5[1] - octave5[0])/2) && freq < (octave5[1] + octave5[0])/2)
        {note = "C5"; position = (freq - octave5[0]) / (2*(octave5[1] - octave5[0]));}
        else if(freq > (octave5[1] + octave5[0])/2 && freq < (octave5[1] + octave5[2])/2)
        {note = "C#5"; position = (freq - octave5[1]) / ((octave5[2] - octave5[0]));}
        else if(freq > (octave5[2] + octave5[1])/2 && freq < (octave5[2] + octave5[3])/2)
        {note = "D5"; position = (freq - octave5[2]) / ((octave5[3] - octave5[1]));}
        else if(freq > (octave5[3] + octave5[2])/2 && freq < (octave5[3] + octave5[4])/2)
        {note = "D#5"; position = (freq - octave5[3]) / ((octave5[4] - octave5[2]));}
        else if(freq > (octave5[4] + octave5[3])/2 && freq < (octave5[4] + octave5[5])/2)
        {note = "E5"; position = (freq - octave5[4]) / ((octave5[5] - octave5[3]));}
        else if(freq > (octave5[5] + octave5[4])/2 && freq < (octave5[5] + octave5[6])/2)
        {note = "F5"; position = (freq - octave5[5]) / ((octave5[6] - octave5[4]));}
        else if(freq > (octave5[6] + octave5[5])/2 && freq < (octave5[6] + octave5[7])/2)
        {note = "F#5"; position = (freq - octave5[6]) / ((octave5[7] - octave5[5]));}
        else if(freq > (octave5[7] + octave5[6])/2 && freq < (octave5[7] + octave5[8])/2)
        {note = "G5"; position = (freq - octave5[7]) / ((octave5[8] - octave5[6]));}
        else if(freq > (octave5[8] + octave5[7])/2 && freq < (octave5[8] + octave5[9])/2)
        {note = "G#5"; position = (freq - octave5[8]) / ((octave5[9] - octave5[7]));}
        else if(freq > (octave5[9] + octave5[8])/2 && freq < (octave5[9] + octave5[10])/2)
        {note = "A5"; position = (freq - octave5[9]) / ((octave5[10] - octave5[8]));}
        else if(freq > (octave5[10] + octave5[9])/2 && freq < (octave5[10] + octave5[11])/2)
        {note = "A#5"; position = (freq - octave5[10]) / ((octave5[11] - octave5[9]));}
        else if(freq > (octave5[11] + octave5[10])/2 && freq < (octave5[11] + (octave5[11] - octave5[10])/2))
        {note = "B5"; position = (freq - octave5[11]) / (2*(octave5[11] - octave5[2]));}

        note+="\n";
        for(double i = -0.7; i < 0.8; i+=0.1)
        {
            if(Math.abs(position-i) > 0.05)
                note+="-";
            else
                note+="|";
        }
        return note;
    }

    public static void getLPCoefficientsButterworth2Pole(int samplerate, double cutoff, double[]  ax, double[] by)
    {
        double PI    = 3.1415926535897932385;
        double sqrt2 = 1.4142135623730950488;

        double QcRaw  = (2 * PI * cutoff) / samplerate; // Find cutoff frequency in [0..PI]
        double QcWarp = Math.tan(QcRaw); // Warp cutoff frequency

        double gain = 1 / (1+sqrt2/QcWarp + 2/(QcWarp*QcWarp));
        by[2] = (1 - sqrt2/QcWarp + 2/(QcWarp*QcWarp)) * gain;
        by[1] = (2 - 2 * 2/(QcWarp*QcWarp)) * gain;
        by[0] = 1;
        ax[0] = 1 * gain;
        ax[1] = 2 * gain;
        ax[2] = 1 * gain;
    }

    public static void filter(double[] samples, int count)
    {
        double[] ax = {0,0,0};
        double[] by = {0,0,0};

        double[] xv = {0,0,0};
        double[] yv = {0,0,0};

        getLPCoefficientsButterworth2Pole(44100, 5000, ax, by);

        for (int i=0;i<count;i++)
        {
            xv[2] = xv[1]; xv[1] = xv[0];
            xv[0] = samples[i];
            yv[2] = yv[1]; yv[1] = yv[0];

            yv[0] =   (ax[0] * xv[0] + ax[1] * xv[1] + ax[2] * xv[2]
                    - by[1] * yv[0]
                    - by[2] * yv[1]);

            samples[i] = yv[0];
        }
    }
}



