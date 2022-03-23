package com.superpowered.playerexample.beat;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.superpowered.playerexample.MainActivity;
import com.superpowered.playerexample.R;
import com.superpowered.playerexample.graphics.LoadGraphics;

public class BlackCircle {

    private boolean animating;
    private int volume;
    Bitmap circle;
    int width, height;
    Rect circleSize;

    double animationSize;
    double maxAnimationSize;

    public BlackCircle(MainActivity main) {
        animating = false;
        animationSize = 0;
        maxAnimationSize = 20;
        circle = main.getGraphics().getImages(LoadGraphics.BLACK_CIRCLE);
        width = (main.findViewById(R.id.beatImage)).getMeasuredHeight()/2;
        height = width;
        circleSize = new Rect(0, (int)maxAnimationSize, width, (int)maxAnimationSize + height);
    }

    public void update(int dx) {
        int left = dx - (int) (animationSize / 2);
        int right = left + width + (int) (animationSize);
        int top = (int)maxAnimationSize - (int)(animationSize/2);
        int bottom = top + height + (int)(animationSize);
        circleSize.set(left, top, right, bottom);
        if (animating) {
            if (animationSize <= maxAnimationSize) {
                animationSize += 10;
            } else {
                animating = false;
            }
        } else {
            if (animationSize > 0) {
                animationSize -= 10;
            } else if (animationSize < 0)
                animationSize = 0;
        }
    }
    public void animate() {
        if (!animating) {
            animating = true;
        }
    }

    public void setVolume() {
        volume++;
        if (volume > 2) {
            volume = 0;
        }
    }

    public Rect getCircleSize() {
        return circleSize;
    }
}
