package com.superpowered.playerexample.bluetooth;

import android.os.SystemClock;

import java.nio.charset.Charset;

public class EstablishClocks {

    BlueTooth blueTooth;

    long sentTime;
    long receivedTime;
    private long phoneTimeDif;
    final long playDelay = 300;
    long btStartTime;


    public EstablishClocks(BlueTooth blueTooth) {
        this.blueTooth = blueTooth;
        sentTime = 0;
        receivedTime = 0;
        phoneTimeDif = 0;
        btStartTime = 0;
    }

    protected void timeSet(long miliTime) {

        if (miliTime != 0) {
            double ping = SystemClock.elapsedRealtime() - sentTime;
            System.out.println("ping: " + ping + ":");
            if (ping <= 7) {
                receivedTime = miliTime + (long)(ping / 2);
                phoneTimeDif = SystemClock.elapsedRealtime() - receivedTime;
                byte[] bytes = ("s" + phoneTimeDif).getBytes(Charset.defaultCharset());
                blueTooth.mBluetoothConnection.write(bytes);
                sentTime = 0;
            }
        }
        if (receivedTime == 0) {
            sentTime = SystemClock.elapsedRealtime();
            byte[] bytes = Long.toString(sentTime).getBytes(Charset.defaultCharset());
            blueTooth.mBluetoothConnection.write(bytes);
        }
    }

    public long getPhoneTimeDif() {
        return phoneTimeDif;
    }

    public void setPhoneTimeDif(long phoneTimeDif) {
        this.phoneTimeDif = phoneTimeDif;
        blueTooth.setConnected(true);
    }
}
