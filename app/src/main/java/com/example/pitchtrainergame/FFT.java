package com.example.pitchtrainergame;

/*
 *  Copyright 2006-2007 Columbia University.
 *
 *  This file is part of MEAPsoft.
 *
 *  MEAPsoft is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *
 *  MEAPsoft is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MEAPsoft; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA
 *
 *  See the file "COPYING" for the text of the license.
 */

public class FFT {

    static int n = 0, m;

    // Lookup tables.  Only need to recompute when size of FFT changes.
    static double[] cos;
    static double[] sin;

    static double[] window;

    protected static void makeWindow() {
        // Make a blackman window:
        // w(n)=0.42-0.5cos{(2*PI*n)/(N-1)}+0.08cos{(4*PI*n)/(N-1)};
        window = new double[n];
        for(int i = 0; i < window.length; i++)
            window[i] = 0.42 - 0.5 * Math.cos(2*Math.PI*i/(n-1))
                    + 0.08 * Math.cos(4*Math.PI*i/(n-1));
    }

    public static double[] getWindow() {
        return window;
    }


    /***************************************************************
     * fft.c
     * Douglas L. Jones
     * University of Illinois at Urbana-Champaign
     * January 19, 1992
     * http://cnx.rice.edu/content/m12016/latest/
     *
     *   fft: in-place radix-2 DIT DFT of a complex input
     *
     *   input:
     * n: length of FFT: must be a power of two
     * m: n = 2**m
     *   input/output
     * x: double array of length n with real part of data
     * y: double array of length n with imag part of data
     *
     *   Permission to copy and use this program is granted
     *   as long as this header is included.
     ****************************************************************/
    public static void fft(double[] x, double[] y)
    {
        int i,j,k,n1,n2,a;
        double c,s,e,t1,t2;


        // Bit-reverse
        j = 0;
        n2 = n/2;
        for (i=1; i < n - 1; i++) {
            n1 = n2;
            while ( j >= n1 ) {
                j = j - n1;
                n1 = n1/2;
            }
            j = j + n1;

            if (i < j) {
                t1 = x[i];
                x[i] = x[j];
                x[j] = t1;
                t1 = y[i];
                y[i] = y[j];
                y[j] = t1;
            }
        }

        // FFT
        n1 = 0;
        n2 = 1;

        for (i=0; i < m; i++) {
            n1 = n2;
            n2 = n2 + n2;
            a = 0;

            for (j=0; j < n1; j++) {
                c = cos[a];
                s = sin[a];
                a +=  1 << (m-i-1);

                for (k=j; k < n; k=k+n2) {
                    t1 = c*x[k+n1] - s*y[k+n1];
                    t2 = s*x[k+n1] + c*y[k+n1];
                    x[k+n1] = x[k] - t1;
                    y[k+n1] = y[k] - t2;
                    x[k] = x[k] + t1;
                    y[k] = y[k] + t2;
                }
            }
        }
    }

    // Test the FFT to make sure it's working
    public static void main_test(int cnt) {

        init(cnt);

        //FFT fft = new FFT(N);

        double[] window = getWindow();
        double[] re = new double[cnt];
        double[] im = new double[cnt];

        // Impulse
        re[0] = 1; im[0] = 0;
        for(int i=1; i<cnt; i++)
            re[i] = im[i] = 0;
        beforeAfter(cnt, re, im);

        // Nyquist
        for(int i=0; i<cnt; i++) {
            re[i] = Math.pow(-1, i);
            im[i] = 0;
        }
        beforeAfter(cnt, re, im);

        // Single sin
        for(int i=0; i<cnt; i++) {
            re[i] = Math.cos(2*Math.PI*i / cnt);
            im[i] = 0;
        }
        beforeAfter(cnt, re, im);

        // Ramp
        for(int i=0; i<cnt; i++) {
            re[i] = i;
            im[i] = 0;
        }
        beforeAfter(cnt, re, im);

        long time = System.currentTimeMillis();
        double iter = 30000;
        for(int i=0; i<iter; i++)
            fft(re,im);
        time = System.currentTimeMillis() - time;
        System.out.println("Averaged " + (time/iter) + "ms per iteration");
    }

    protected static void beforeAfter(int cnt, double[] re, double[] im) {
        System.out.println("Before: ");
        printReIm(re, im);
        fft(re, im);
        System.out.println("After: ");
        printReIm(re, im);
    }

    protected static void printReIm(double[] re, double[] im) {
        System.out.print("Re: [");
        for(int i=0; i<re.length; i++)
            System.out.print(((int)(re[i]*1000)/1000.0) + " ");

        System.out.print("]\nIm: [");
        for(int i=0; i<im.length; i++)
            System.out.print(((int)(im[i]*1000)/1000.0) + " ");

        System.out.println("]");
    }

    public static void init(int cnt)
    {
        FFT.n = cnt;
        FFT.m = (int) (Math.log(n) / Math.log(2));

        // Make sure n is a power of 2
        if (cnt != (1 << m))
            throw new RuntimeException("FFT length must be power of 2");

        // precompute tables
        cos = new double[cnt / 2];
        sin = new double[cnt / 2];

        //for(int i=0; i<n/4; i++) {
        //    cos[i] = Math.cos(-2*Math.PI*i/n);
        //    sin[n/4-i] = cos[i];
        //    cos[n/2-i] = -cos[i];
        //    sin[n/4+i] = cos[i];
        //    cos[n/2+i] = -cos[i];
        //    sin[n*3/4-i] = -cos[i];
        //    cos[n-i]   = cos[i];
        //    sin[n*3/4+i] = -cos[i];
        //}

        for (int i = 0; i < cnt / 2; i++) {
            cos[i] = Math.cos(-2 * Math.PI * i / cnt);
            sin[i] = Math.sin(-2 * Math.PI * i / cnt);
        }

        makeWindow();
    }

    public static double[] fftCalculator(int cnt, double[] re, double[] im, int length) {
        if(FFT.n == 0)
            init(cnt);

        if (re.length != im.length) return null;
        if(length > re.length)
            length = re.length;
        fft(re, im);
        double[] fftMag = new double[length];
        for (int i = 0; i < length; i++) {
            fftMag[i] = Math.pow(re[i], 2) + Math.pow(im[i], 2);
        }
        return fftMag;
    }
}