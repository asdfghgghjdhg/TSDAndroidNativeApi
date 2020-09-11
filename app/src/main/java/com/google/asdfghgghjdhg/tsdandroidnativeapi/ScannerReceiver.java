package com.google.asdfghgghjdhg.tsdandroidnativeapi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ScannerReceiver {
    private static final String TAG = "ScannerReceiver";
    private static final String EVENT_SOURCE = "ScannerReceiver";

    private long v8 = 0;
    private Activity v8Activity = null;
    private String barcodeBuff = "";

    private class Receiver extends BroadcastReceiver {
        private final char[] CHINA   = new char[]  {0x90aa, 0x659c, 0x80c1, 0x8c10, 0x5199, 0x68b0, 0x8922, 0x5378, 0x87f9, 0x61c8,
                                                    0x6cfb, 0x8c22, 0x5c51, 0x85aa, 0x82af, 0x950c, 0x8909, 0x890b, 0x890c, 0x890d,
                                                    0x890e, 0x890f, 0x8911, 0x8914, 0x8915, 0x8916, 0x891c, 0x8918, 0x8917, 0x891d,
                                                    0x891e, 0x891f,
                                                    0x8897, 0x8898, 0x8899, 0x889a, 0x889b, 0x889d, 0x887c, 0x889e, 0x889f, 0x88a0,
                                                    0x88a3, 0x88a5, 0x88a6, 0x88a7, 0x88a8, 0x88a9, 0x88aa, 0x5c0f, 0x5b5d, 0x6821,
                                                    0x8096, 0x5578, 0x7b11, 0x6548, 0x6954, 0x4e9b, 0x978b, 0x874e, 0x6b47, 0x534f,
                                                    0x631f, 0x643a};
        private final String RUS     = "абвгдеёжзиклмнопрстуфхцчшщьыъэюяАБВГДЕЁЖЗИКЛМНОПРСТУФХЦЧШЩЬЫЪЭЮЯ";

        private Timer timer = null;

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(TAG, "Received " + action);

            if (action.equals("app.dsic.barcodetray.BARCODE_BR_DECODING_DATA")) {
                String barcode = intent.getStringExtra("EXTRA_BARCODE_DECODED_DATA");

                char[] chars = barcode.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    for (int j = 0; j < CHINA.length; j++) {
                        if (chars[i] == CHINA[j]) {
                            chars[i] = RUS.charAt(j);
                            break;
                        }
                    }
                }
                barcode = String.valueOf(chars);

                barcodeBuff = barcodeBuff + barcode;

                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        sendExternalEvent("Штрихкод", barcodeBuff);

                        barcodeBuff = "";
                        timer = null;
                    }
                };
                if (timer == null) {
                    timer = new Timer();
                    timer.schedule(task, 200);
                }
            }
        }
    }

    private Receiver receiver;

    private static native void doExternalEvent(long v8object, String source, String event, String data);

    public ScannerReceiver(Activity v8Activity, long v8) {
        System.loadLibrary("org_google_asdfghgghjdhg_TSDAndroidReceiver");

        this.v8 = v8;
        this.v8Activity = v8Activity;
        receiver = new Receiver();

        Log.v(TAG, "Initialized");
    }

    public void enableReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction("app.dsic.barcodetray.BARCODE_BR_DECODING_DATA");
        v8Activity.registerReceiver(receiver, filter);

        Log.v(TAG, "Receiver registered");
    }

    public void disableReceiver() {
        v8Activity.unregisterReceiver(receiver);

        Log.v(TAG, "Receiver unregistered");
    }

    public String getDeviceId() {
        //String androidId = android.provider.Settings.Secure.getString(v8Activity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        //return new UUID(androidId.hashCode(), androidId.hashCode()).toString();
        return Build.SERIAL;
    }

    public void sendExternalEvent(String event, String data) {
        Log.v(TAG, "Sending external event: " + event + ", " + data);

        if (v8 != 0) {
            doExternalEvent(v8, EVENT_SOURCE, event, data);
        }
    }
}
