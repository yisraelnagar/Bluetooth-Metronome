package com.superpowered.playerexample.beat;

import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import com.superpowered.playerexample.R;


public class TapTempo {

    private CountDownTimer countDownTimer;
    BeatLoop beatLoop;
    private long averageTempo;
    private long hitTime;
    private int hitCount;
    private Button tapButton;
    private long finishTime;
    private final long delayTime = 1000;
    private int numerator;
    private int startBeatAt;

    public TapTempo(BeatLoop bl) {
        this.beatLoop = bl;
        averageTempo = 0;
        hitTime = 0;
        hitCount = 0;
        finishTime = 0;
        numerator = beatLoop.numerator;
        setNumerator(numerator);
        tapButton = bl.main.findViewById(R.id.tapTempo);
        tapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - finishTime > delayTime) {
                    hitTempo();
                }
            }
        });
    }

    public void hitTempo() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        long millisInFuture = 3000;

        if (hitCount == 0) {
            beatLoop.deactivateMetronomeLoop();
        } else if (hitCount == 1) {
            averageTempo = SystemClock.elapsedRealtime() - hitTime;
            millisInFuture = (long) (averageTempo * 1.25);
        } else {
            averageTempo = ((averageTempo * (hitCount - 1)) + (SystemClock.elapsedRealtime() - hitTime)) / (hitCount);
            millisInFuture = (long) (averageTempo * 1.25);
        }

        hitCount++;
        //Start Metronome When Surpass The numerator
        if (hitCount >= startBeatAt) {
            int bpm = (int) (60000 / averageTempo);
            if (bpm > 400) {
                bpm = 400;
            }
            beatLoop.setBPM(bpm);
            if (hitCount == startBeatAt) {
                beatLoop.activateMetronome();
            }
        }
        tapButton.setText("  ");

        hitTime = SystemClock.elapsedRealtime();

        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                finishTime = SystemClock.elapsedRealtime();

                if (hitCount < 2) {
                    averageTempo = 0;
                    hitTime = 0;
                    hitCount = 0;
                    tapButton.setText("TAP");
                } else {
                    int bpm = (int) (60000 / averageTempo);
                    if (bpm > 400) {
                        bpm = 400;
                    }
                    beatLoop.setBPM(bpm);
                    if (hitCount < startBeatAt) {
                        beatLoop.activateMetronome();
                    }
                    averageTempo = 0;
                    hitTime = 0;
                    hitCount = 0;
                    tapButton.setText("TAP");
                    countDownTimer.cancel();
                }
            }
        };
        countDownTimer.start();

    }

    public void setNumerator(int num){
        this.numerator = num;
        if(numerator == 2){
            startBeatAt = numerator;
        }
        else if(numerator%3 == 0 && numerator%4 != 0){
            startBeatAt = 3;
        }
        else{
            startBeatAt = 4;
        }
    }

    public boolean isActive(){
        if(hitCount > 0){
            return true;
        }
        return false;
    }
}
