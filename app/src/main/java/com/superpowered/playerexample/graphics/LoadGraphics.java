package com.superpowered.playerexample.graphics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.superpowered.playerexample.MainActivity;
import com.superpowered.playerexample.MainFragment;
import com.superpowered.playerexample.R;

public class LoadGraphics {

    final public static int BLACK_CIRCLE = 0;

    private Bitmap[] images;

    public LoadGraphics(MainFragment main){
        images = new Bitmap[1];
        images[BLACK_CIRCLE] = BitmapFactory.decodeResource(main.getResources(),R.drawable.blackcircle);
    }

    public Bitmap getImages(int imageNum){
        return images[imageNum];
    }
}
