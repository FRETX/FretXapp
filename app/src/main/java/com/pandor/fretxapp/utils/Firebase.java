package com.pandor.fretxapp.utils;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pandor.fretxapp.activities.BaseActivity;

import android.util.Log;

/**
 * FretXapp for FretX
 * Created by pandor on 14/04/17 14:20.
 */

public class Firebase {
    private static final String TAG = "KJKP6_FIREBASE";
    private FirebaseAnalytics analytics;

    private boolean enabled;

    /* = = = = = = = = = = = = = = = = = SINGLETON PATTERN = = = = = = = = = = = = = = = = = = = */
    private static class Holder {
        private static final Firebase instance = new Firebase();
    }

    private Firebase() {}

    public static Firebase getInstance() {
        return Holder.instance;
    }

    public static FirebaseAnalytics getAnalytics() {
        return Firebase.getInstance().analytics;
    }

    /* = = = = = = = = = = = = = = = = = = = = FIREBASE = = = = = = = = = = = = = = = = = = = = = */
    public void init() {
        analytics = FirebaseAnalytics.getInstance(BaseActivity.getActivity());
        Log.d(TAG, "init");
        enabled = true;
    }

    public void start() {
        Log.d(TAG, "start");
    }

    public void stop() {
        Log.d(TAG, "stop");
    }

    public boolean isEnabled() {
        return enabled;
    }
}
