package com.superpowered.playerexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import android.os.Bundle;
import android.os.Handler;

import com.superpowered.playerexample.beat.BeatLoop;
import com.superpowered.playerexample.bluetooth.BlueTooth;
import com.superpowered.playerexample.graphics.LoadGraphics;

import java.io.IOException;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    Thread thread;
    boolean focusChanged = false;
    final long drawFPS = 10;
    long start;

    //BeatLoop
    BeatLoop beatLoop;

    //Bluetooth
    BlueTooth blueTooth;

    //Images
    LoadGraphics loadGraphics;

    //List Of Windows
    LinkedList<MainInterface> windows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadGraphics = new LoadGraphics(this);
        init();
        SlidingPagesAdapter slidingPagesAdapter = new SlidingPagesAdapter(this);
        ViewPager2 viewPager2 = findViewById(R.id.viewPager);
        viewPager2.setAdapter(slidingPagesAdapter);
    }


    //Init
    public void init() {

        blueTooth = new BlueTooth(this);
        beatLoop = new BeatLoop(this);
        beatLoop.setWindowActive(true);
        windows = new LinkedList<MainInterface>();
        windows.add(beatLoop);

        start = SystemClock.elapsedRealtime();
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    update();

                    if (focusChanged) {
                        if (SystemClock.elapsedRealtime() > start + drawFPS) {
                            start += drawFPS;
                            draw();
                        }
                    }
                }
            }
        });
        thread.start();
    }

    public void update() {
        for (int i = 0; i < windows.size(); i++) {
            if (windows.get(i).getWindowActive()) {
                windows.get(i).update();
            }
        }
    }

    public void draw() {
        for (int i = 0; i < windows.size(); i++) {
            if (windows.get(i).getWindowActive()) {
                windows.get(i).draw();
            }
        }
    }

    public BlueTooth getBlueTooth() {
        return blueTooth;
    }

    public BeatLoop getBeatLoop() {
        return beatLoop;
    }

    public LoadGraphics getGraphics(){
        return loadGraphics;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        beatLoop.onWindowFocusChanged();
        focusChanged = true;
    }

    protected void onDestroy() {
        super.onDestroy();
        beatLoop.onDestroy();
        blueTooth.onDestroy();
    }

}
