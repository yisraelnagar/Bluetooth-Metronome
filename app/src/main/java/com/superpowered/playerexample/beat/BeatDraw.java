package com.superpowered.playerexample.beat;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.superpowered.playerexample.MainActivity;
import com.superpowered.playerexample.R;
import com.superpowered.playerexample.graphics.LoadGraphics;

import java.util.LinkedList;

public class BeatDraw extends Drawable {
    Paint paint = new Paint();
    int x, y, width, height, length, numerator;
    double diff;
    int currBeat;
    int lastCurrBeat;
    Rect beatSize;
    MainActivity main;
    LinkedList<BlackCircle> blackCircles;

    public BeatDraw(MainActivity main, LinkedList<BlackCircle> circles, int x, int y, int length, int numerator, int currBeat, int lastCurrBeat) {
        this.main = main;
        this.blackCircles = circles;
        this.x = x; this.y = y; this.length = length;
        this.currBeat = currBeat;
        beatSize = new Rect(0,0,width,height);
        setDiff(numerator);
        this.lastCurrBeat = lastCurrBeat;
    }

    public BeatDraw(MainActivity main){
        blackCircles = new LinkedList<BlackCircle>();
        for(int i = 0; i < 16; i++){
            blackCircles.add(new BlackCircle(main));
        }
    }

    public void setDiff(int newNumerator){

        this.diff = length/newNumerator;
        this.numerator = newNumerator;

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void draw(@NonNull Canvas canvas) {
        paint.setStrokeWidth(3);
        paint.setColor(Color.BLACK);
        for(int i = 0; i < numerator; i++) {
            int dx = (((int) diff - blackCircles.get(i).width)/2) + x + ((int) diff * i);
            blackCircles.get(i).update(dx);

            if(i+1 == currBeat && currBeat != lastCurrBeat) {
                lastCurrBeat = currBeat;
                blackCircles.get(i).animate();
            }

            canvas.drawBitmap(blackCircles.get(i).circle, null, blackCircles.get(i).getCircleSize(),paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    public LinkedList<BlackCircle> getBlackCircles(){
        return blackCircles;
    }

    public int getLastCurrBeat(){
        return lastCurrBeat;
    }

}
