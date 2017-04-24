package com.pandor.fretxapp.utils.Audio;

/**
 * FretXapp for FretX
 * Created by pandor on 24/04/17 19:44.
 */

public interface AudioListener {
    void onProgress();
    void onLowVolume();
    void onHighVolume();
    void onTimeout();
}