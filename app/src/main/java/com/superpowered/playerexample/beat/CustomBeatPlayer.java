package com.superpowered.playerexample.beat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.superpowered.playerexample.R;

import java.util.ArrayList;

public class CustomBeatPlayer {
    private String customBeat;
    private Button doneBtn;
    ArrayList<Beat> allBeats;
    int currentBeat;
    int numRepeated;

    BeatLoop beatLoop;


    public CustomBeatPlayer(BeatLoop loop){
        customBeat = "b200 t4 r2 b140 t2 r2 b60 t6 r1 b300 t11 r1";
        beatLoop = loop;
        currentBeat = 0;
        loadBeat();
    }

    public void loadBeat() {
        String[] beats = customBeat.split("\\s+");
        ArrayList<Beat> allBeats = new ArrayList<>();
        Beat beat = null;
        int bpm = 0;
        for (int i = 0; i < beats.length; i++) {
            String str = beats[i];
            if ("b".equals(str.substring(0, 1))) {
                bpm = Integer.parseInt(str.substring(1));
            } else if ("t".equals(str.substring(0, 1))) {
                int ts = Integer.parseInt(str.substring(1));
                beat = new Beat(ts, ts, bpm);
                allBeats.add(beat);
            } else if ("r".equals(str.substring(0, 1))) {
                int repeat = Integer.parseInt(str.substring(1));
                if (beat != null) {
                    beat.setNumRepeat(repeat);
                }
            }
        }
        this.allBeats = allBeats;
        beatLoop.customBeatSelected(true);
        beatLoop.setNumerator(allBeats.get(currentBeat).getNumerator());
        beatLoop.setBPM(allBeats.get(currentBeat).getBpm());
    }

    public void finishedNumerator(){
        numRepeated++;
        if(numRepeated >= allBeats.get(currentBeat).getNumRepeat()) {
            currentBeat++;
            numRepeated = 0;
            if (currentBeat < allBeats.size()) {
                beatLoop.setBPM(allBeats.get(currentBeat).getBpm());
                beatLoop.setNumerator(allBeats.get(currentBeat).getNumerator());
            }
            else{
                beatLoop.deactivateMetronomeLoop();
                currentBeat = 0;
                numRepeated = 0;
                beatLoop.setNumerator(allBeats.get(currentBeat).getNumerator());
                beatLoop.setBPM(allBeats.get(currentBeat).getBpm());
            }
        }
    }

    public void stopPlay(View view) {
        if (!beatLoop.active) {
            beatLoop.activateMetronome();
        } else {
            beatLoop.deactivateMetronomeLoop();
        }
    }
}
