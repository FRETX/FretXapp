package com.pandor.fretxapp.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.pandor.fretxapp.utils.Audio;
import com.pandor.fretxapp.utils.Bluetooth;
import com.pandor.fretxapp.utils.Midi;

import java.lang.ref.WeakReference;

/**
 * FretXapp for FretX
 * Created by pandor on 14/04/17 15:00.
 */

public class BaseActivity extends AppCompatActivity {
    private final static String TAG = "KJKP6_BASEACTIVITY";
    private static WeakReference<AppCompatActivity> activity = null;

    private boolean wasScanning = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "============================== ON CREATE ==============================");
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Set weak reference");
        activity = new WeakReference<AppCompatActivity>(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        Log.d(TAG, "========================= ON CREATE PERSISTABLE ========================");
        super.onCreate(savedInstanceState, persistentState);

        Log.d(TAG, "Set weak reference");
        activity = new WeakReference<AppCompatActivity>(this);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "============================== ON RESUME ==============================");
        super.onResume();

        Log.d(TAG, "Set weak reference");
        activity = new WeakReference<AppCompatActivity>(this);

        if (Midi.getInstance().isEnabled())
            Midi.getInstance().start();
        if (Audio.getInstance().isEnabled())
            Audio.getInstance().start();
        if (Bluetooth.getInstance().isEnabled()) {
            Bluetooth.getInstance().start();
            if (wasScanning)
                Bluetooth.getInstance().scan();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "============================== ON PAUSE ===============================");
        super.onPause();

        if (Midi.getInstance().isEnabled())
            Midi.getInstance().stop();
        if (Audio.getInstance().isEnabled())
            Audio.getInstance().stop();
        if (Bluetooth.getInstance().isEnabled()) {
            wasScanning = Bluetooth.getInstance().isScanning();
            Bluetooth.getInstance().stop();
            Bluetooth.getInstance().clearMatrix();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "=============================== ON STOP ===============================");
        super.onStop();

        Log.d(TAG, "Set weak reference to null");
        activity = null;
    }

    public static AppCompatActivity getActivity() {
        return activity == null ? null : activity.get();
    }
}
