package com.pandor.fretxapp.utils;

import android.os.CountDownTimer;
import android.util.Log;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.AudioProcessing;
import rocks.fretx.audioprocessing.Chord;

/**
 * FretXapp for FretX
 * Created by pandor on 14/04/17 14:20.
 */

public class Audio {
    private final String TAG = "KJKP6_AUDIO_UTIL";

    //audio settings
    static private final int FS = 16000;
    static private final double BUFFER_SIZE_S = 0.1;
    static private final long TIMER_TICK = 20;
    static private final long ONSET_IGNORE_DURATION_MS = 0;
    static private final long CHORD_LISTEN_DURATION_MS = 500;
    static private final long TIMER_DURATION_MS = ONSET_IGNORE_DURATION_MS + CHORD_LISTEN_DURATION_MS;
    static private final long CORRECTLY_PLAYED_DURATION_MS = 160;
    static private final double VOLUME_THRESHOLD = -9;
    static private final int TIMEOUT_THRESHOLD = 20;

    //audio
    private boolean enabled;
    private AudioProcessing audio;
    private Chord targetChord;
    private double correctlyPlayedAccumulator;
    private boolean upsideThreshold;
    private int timeoutCounter;
    private boolean timeoutNotified;

    //listener
    private AudioDetectorListener listener;

    /* = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = */
    private static class Holder {
        private static final Audio instance = new Audio();
    }

    private Audio() {
    }

    public static Audio getInstance() {
        return Holder.instance;
    }

    /* = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = */

    public void init() {
        Log.d(TAG, "init");
        audio = new AudioProcessing();
        enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void start() {
        Log.d(TAG, "start");
        if (!audio.isInitialized())
            audio.initialize(FS, BUFFER_SIZE_S);
        if (!audio.isProcessing())
            audio.start();
    }

    public void stop() {
        Log.d(TAG, "stop");
        if (audio.isProcessing() ) {
            audio.stop();
        }
    }

    public void setTargetChord(Chord chord) {
        targetChord = chord;
    }

    public void setTargetChords(ArrayList<Chord> chords) {
        audio.setTargetChords(chords);
    }

    public void startListening() {
        correctlyPlayedAccumulator = 0;
        timeoutCounter = 0;
        chordTimer.cancel();
        chordTimer.start();
        Log.d(TAG, "starting the countdownTimer");
    }

    public void stopListening() {
        chordTimer.cancel();
    }

    public double getProgress() {
        return correctlyPlayedAccumulator / CORRECTLY_PLAYED_DURATION_MS * 100;
    }

    //todo replace with a handler to avoid restart of countdown timer
    private CountDownTimer chordTimer = new CountDownTimer(TIMER_DURATION_MS, TIMER_TICK) {
        public void onTick(long millisUntilFinished) {

            //todo remove this 2 or 3 checks - should not happen
            if (!audio.isInitialized()) {
                //Log.d("USELESSSTUF", "not initialized");
                return;
            }
            if (!audio.isProcessing()) {
                //Log.d("USELESSSTUF", "not processing");
                return;
            }

            if (!audio.isBufferAvailable()) {
                return;
            }

            //nothing heard
            if (audio.getVolume() < VOLUME_THRESHOLD) {
                if (upsideThreshold) {
                    upsideThreshold = false;
                    correctlyPlayedAccumulator = 0;
                    listener.onLowVolume();
                }
                //Log.d(TAG, "prematurely canceled due to low volume");
            }
            //chord heard
            else {
                if (!upsideThreshold) {
                    upsideThreshold = true;
                    listener.onHighVolume();
                }

                //update progress
                if (millisUntilFinished <= CHORD_LISTEN_DURATION_MS) {
                    Chord playedChord = audio.getChord();
                    Log.d(TAG, "played:" + playedChord.toString());

                    if (targetChord.toString().equals(playedChord.toString())) {
                        correctlyPlayedAccumulator += TIMER_TICK;
                        Log.d(TAG, "correctly played acc -> " + correctlyPlayedAccumulator);
                    } else {
                        correctlyPlayedAccumulator = 0;
                        Log.d(TAG, "not correctly played acc");
                    }
                    listener.onProgress();
                }

                //stop the count down timer
                if (correctlyPlayedAccumulator >= CORRECTLY_PLAYED_DURATION_MS) {
                    //Log.d(TAG, "- - - - - chord detected - - - - -");
                    this.cancel();
                }
            }
        }

        public void onFinish() {
            //Log.d(TAG, "finished without hearing enough of correct chords");
            correctlyPlayedAccumulator = 0;
            listener.onProgress();
            timeoutCounter += 1;
            if (!timeoutNotified && timeoutCounter >= TIMEOUT_THRESHOLD) {
                listener.onTimeout();
                timeoutNotified = true;
            }
            chordTimer.start();
        }
    };

    /* = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = */
    public void setAudioDetectorListener(AudioDetectorListener listener) {
        this.listener = listener;
    }

    public interface AudioDetectorListener {
        void onProgress();
        void onLowVolume();
        void onHighVolume();
        void onTimeout();
    }
}
