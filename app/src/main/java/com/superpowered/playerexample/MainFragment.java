package com.superpowered.playerexample;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.superpowered.playerexample.beat.BeatLoop;
import com.superpowered.playerexample.bluetooth.BlueTooth;
import com.superpowered.playerexample.graphics.LoadGraphics;

import java.util.LinkedList;


public class MainFragment extends Fragment {
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

    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loadGraphics = new LoadGraphics(this);
        init();
        SlidingPagesAdapter slidingPagesAdapter = new SlidingPagesAdapter(getActivity());
        ViewPager2 viewPager2 = getView().findViewById(R.id.viewPager);
        viewPager2.setAdapter(slidingPagesAdapter);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }


    //Init
    public void init() {

        blueTooth = new BlueTooth(this);
        beatLoop = new BeatLoop(getContext());
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
