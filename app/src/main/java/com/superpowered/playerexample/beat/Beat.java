package com.superpowered.playerexample.beat;

/*
This class represents a beat object. every beat has a time signature (numerator and denominator)
and a bpm
 */
public class Beat {

    private final int numerator;
    private final int denominator;
    private final int bpm;

    public int getNumRepeat() {
        return numRepeat;
    }

    private int numRepeat = 1;

    public Beat(final int n, final int d, final int b) {
        numerator = n;
        denominator = d;
        bpm = b;
    }

    public int getNumerator() {
        return numerator;
    }

    public int getDenominator() {
        return denominator;
    }

    public int getBpm() {
        return bpm;
    }

    public void setNumRepeat(int repeat){
        numRepeat = repeat;
    }


}
