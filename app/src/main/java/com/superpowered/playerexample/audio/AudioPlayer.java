package com.superpowered.playerexample.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.superpowered.playerexample.R;

import java.io.IOException;

public class AudioPlayer {

    public AudioPlayer(Context context){
        System.out.println("Version: " + Build.VERSION.SDK_INT);
        // Get the device's sample rate and buffer size to enable
        // low-latency Android audio output, if available.
        String samplerateString = null, buffersizeString = null;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            samplerateString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
            buffersizeString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        }
        if (samplerateString == null) samplerateString = "48000";
        if (buffersizeString == null) buffersizeString = "480";
        int samplerate = Integer.parseInt(samplerateString);
        int buffersize = Integer.parseInt(buffersizeString);

        // Files under res/raw are not zipped, just copied into the APK.
        // Get the offset and length to know where our file is located.
        AssetFileDescriptor fd = context.getResources().openRawResourceFd(R.raw.wooden_click_mid_long);
        int fileOffset = (int) fd.getStartOffset();
        int fileLength = (int) fd.getLength();
        try {
            fd.getParcelFileDescriptor().close();
        } catch (IOException e) {
            Log.e("PlayerExample", "Close error.");
        }
        String path = context.getPackageResourcePath();         // get path to APK package
        System.loadLibrary("PlayerExample");    // load native library
        NativeInit(samplerate, buffersize, context.getCacheDir().getAbsolutePath()); // start audio engine
        OpenFileFromAPK(path, fileOffset, fileLength);  // open audio file from APK
        // If the application crashes, please disable Instant Run under Build, Execution, Deployment in preferences.

    }



    // Functions implemented in the native library.
    private native void NativeInit(int samplerate, int buffersize, String tempPath);

    private native void OpenFileFromAPK(String path, int offset, int length);

    public native boolean onUserInterfaceUpdate();

    private native void TogglePlayback();

    private native void onForeground();

    private native void onBackground();

    public native void Cleanup();

    public native void setPosition(int mill);

    public native void pause();

    public native void silentLoop(int start, int length);

    public native double getLatency();

    public native void play();

    public native void restartAudio();

    public native boolean getBuffering();

    public native boolean EOF();

    public native void cachePosition(int milli);

    private boolean playing = false;

    private boolean isPlaying = false;


}
