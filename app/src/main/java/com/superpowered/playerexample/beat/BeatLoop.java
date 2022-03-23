package com.superpowered.playerexample.beat;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.SystemClock;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.superpowered.playerexample.MainActivity;
import com.superpowered.playerexample.MainInterface;
import com.superpowered.playerexample.R;
import com.superpowered.playerexample.audio.AudioPlayer;

import java.nio.charset.Charset;

public class BeatLoop implements MainInterface {

    //Interface
    private boolean windowActive;

    //Classes
    MainActivity main;
    BeatDraw beatDraw;

    //Audio
    int[] customBeatSounds;
    int currSound;
    AudioPlayer audioPlayer;

    //Metronome
    int bpm;
    int numerator;
    int currBeat;

    //Loop
    boolean active;
    int speedInMilli;
    long start;
    private final int playDelay = 500;
    private long BTStartTime;
    private TapTempo tapTempo;

    //Custom Beats
    CustomBeatPlayer cbp;
    private boolean customBeatSelected;

    public BeatLoop(MainActivity main) {

        //Variables
        bpm = 100;
        numerator = 5;
        currBeat = 0;
        speedInMilli = 60000 / bpm;
        customBeatSounds = new int[17];
        currSound = 0;

        //Classes
        audioPlayer = new AudioPlayer(main);


        //Functions
        //beatDraw Is Initiated in focus changed function

        //Seeker And BPM Setup
        this.main = main;
        ((TextView) main.findViewById(R.id.tempoText)).setText("" + bpm);
        ((SeekBar) main.findViewById(R.id.BPMSeekBar)).setProgress(bpm);
        ((SeekBar) main.findViewById(R.id.BPMSeekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSeeker(0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Setting Up Start Button Listener
        ((Button) main.findViewById(R.id.startClickButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopMetronome();
            }
        });

        //Setting Up Custom Beat Stuff
        setupCustomBeatSound();
        cbp = new CustomBeatPlayer(this);



    }

    public void beatLoop() {
        if (active) {
            long time = SystemClock.elapsedRealtime() - start;
            if (time >= speedInMilli) {
                start += speedInMilli;
                //Updates Beat Number !!!Before The Marker Listener!!!!!!
                updateBeat(1);
                audioPlayer.restartAudio();
            }
        }
    }

    public void startStopMetronome() {
        if (!tapTempo.isActive()) {
            if (!active) {
                activateMetronome();
            } else {
                deactivateMetronomeLoop();
            }
        }
    }


    public void updateSeeker(int num) {
        if (num > 0) {
            ((SeekBar) main.findViewById(R.id.BPMSeekBar)).setProgress(bpm);
        } else {
            bpm = ((SeekBar) main.findViewById(R.id.BPMSeekBar)).getProgress();
            ((TextView) main.findViewById(R.id.tempoText)).setText("" + bpm);
            speedInMilli = 60000 / bpm;
        }
    }


    private void updateStartButton() {
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (active) {

                    ((Button) main.findViewById(R.id.startClickButton)).setText("stop");

                } else {

                    ((Button) main.findViewById(R.id.startClickButton)).setText("start");

                }
            }
        });
    }

    public void BTActivateMetronome(long startTime) {
        long phoneTimeDiff = main.getBlueTooth().getClocks().getPhoneTimeDif();
        System.out.println("Time Diff: " + main.getBlueTooth().getClocks().getPhoneTimeDif());
        main.getBlueTooth().setConnected(true);
        start = startTime + phoneTimeDiff;
        active = true;
        audioPlayer.setPosition(100);
        audioPlayer.play();
        updateStartButton();
    }


    private void updateBeat(int num) {
        if (num != -1) {
            currBeat += num;
            if (currBeat > numerator) {
                currBeat = 1;
                //Updating The Custom Beats
                if (customBeatSelected) {
                    cbp.finishedNumerator();
                }
            }
        } else {
            currBeat = 0;
        }
    }

    void activateMetronome() {

        //if not connected to other devices
        if (!main.getBlueTooth().getConnected()) {
            start = SystemClock.elapsedRealtime();
            active = true;
            updateStartButton();
            audioPlayer.setPosition(0);
            audioPlayer.play();
            updateBeat(1);
            // updateBeatDraw();
        }
        //if connected to other devices
        else {
            byte[] bytes = ("p" + (SystemClock.elapsedRealtime() + playDelay)).getBytes(Charset.defaultCharset());
            main.getBlueTooth().getBluetoothConnection().write(bytes);
            System.out.println("Time Diff: " + main.getBlueTooth().getClocks().getPhoneTimeDif());
            start = SystemClock.elapsedRealtime() + playDelay;
            updateStartButton();
            audioPlayer.setPosition(0);
            audioPlayer.play();
            active = true;
        }
    }

    void deactivateMetronomeLoop() {
        active = false;
        audioPlayer.pause();
        //System.out.println("Audio Skipped: " + main.getAudioStartMs());
        updateBeat(-1);
        updateStartButton();
    }

    public void setBPM(int beatsPerMinute) {
        speedInMilli = 60000 / beatsPerMinute;
        this.bpm = beatsPerMinute;
        if (main != null) {
            main.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) main.findViewById(R.id.tempoText)).setText(String.valueOf(bpm));
                }
            });
        }
        updateSeeker(bpm);
    }

    public void onWindowFocusChanged() {
        setBPM(bpm);
        tapTempo = new TapTempo(this);
        beatDraw = new BeatDraw(main);
    }

    private int getCurrBeatSound() {

        return customBeatSounds[currBeat - 1];
    }

    public void setSound(int sound) {
        currSound = sound;
    }

    private void setupCustomBeatSound() {
        for (int i = 0; i <= numerator; i++) {
            if (numerator % 3 == 0 && numerator % 4 != 0) {
                if (i % 6 == 0 || i == 0) {
                    customBeatSounds[i] = 1;
                } else if ((double) i % (double) 3 == 0) {
                    customBeatSounds[i] = 3;
                } else {
                    customBeatSounds[i] = 2;
                }
            } else {
                if (i % 8 == 0 || i == 0) {
                    customBeatSounds[i] = 1;
                } else if ((double) i % (double) 4 == 0) {
                    customBeatSounds[i] = 3;
                } else {
                    customBeatSounds[i] = 2;
                }
            }
        }
    }

    public void customBeatSelected(boolean selected) {
        customBeatSelected = selected;
    }

    public int getCurrBeatNum() {
        return currBeat;
    }

    public void setNumerator(int num) {
        numerator = num;
    }

    public void onDestroy() {
        audioPlayer.Cleanup();
    }


    @Override
    public boolean getWindowActive() {
        return windowActive;
    }

    @Override
    public void setWindowActive(boolean active) {
        windowActive = active;
    }

    @Override
    public void update() {
        beatLoop();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void draw() {
        beatDraw = new BeatDraw(main, beatDraw.getBlackCircles(),
                (int) (main.findViewById(R.id.BPMSeekBar).getX()),
                (int) (main.findViewById(R.id.BPMSeekBar).getY()),
                (int) (main.findViewById(R.id.BPMSeekBar).getWidth()),
                numerator, currBeat, beatDraw.getLastCurrBeat()
        );
        main.runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                main.findViewById(R.id.beatImage).setForeground(beatDraw);
            }
        });
    }
}
