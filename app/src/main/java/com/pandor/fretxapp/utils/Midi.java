package com.pandor.fretxapp.utils;

import android.os.Handler;
import android.util.Log;

import java.util.Arrays;

import org.billthefarmer.mididriver.GeneralMidiConstants;
import org.billthefarmer.mididriver.MidiDriver;

import rocks.fretx.audioprocessing.Chord;

/**
 * FretXapp for FretX
 * Created by pandor on 14/04/17 14:20.
 */

public class Midi extends MidiDriver implements MidiDriver.OnMidiStartListener {
    private static final String TAG = "KJKP6_MIDI_UTIL";

    private final Handler handler = new Handler();
    private int notesIndex;
    private int noteDelay = 30;
    private int sustainDelay = 500;
    private boolean enabled;

    /* = = = = = = = = = = = = = = = = = SINGLETON PATTERN = = = = = = = = = = = = = = = = = = = */
    private static class Holder {
        private static final Midi instance = new Midi();
    }

    private Midi() {}

    public static Midi getInstance() {
        return Holder.instance;
    }

    /* = = = = = = = = = = = = = = = = = = = MIDI PLAYER = = = = = = = = = = = = = = = = = = = = */
    @Override
    public void onMidiStart() {
        //todo: remove allocation
        byte[] event = new byte[2];
        event[0] = (byte) 0xC0; //"Program Change" event for channel 1
        event[1] = GeneralMidiConstants.ACOUSTIC_GUITAR_NYLON; //set instrument
        write(event);
    }

    public void init() {
        Log.d(TAG, "init");
        setOnMidiStartListener(this);
        enabled = true;
    }

    @Override
    public void start() {
        Log.d(TAG, "start");
        super.start();
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop");
        super.start();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void playChord(Chord chord) {
        final int[] notes = chord.getMidiNotes();
        notesIndex = 0;

        Log.d(TAG, "Playing " + chord.toString() + "(" + Arrays.toString(notes) + ")");

        //todo: remove allocation
        final Runnable turnOffAllNotes = new Runnable() {
            @Override
            public void run() {
                //stop all notes of the chord
                for (int note: notes) {
                    stopNote(note);
                }
            }
        };

        //todo: remove allocation
        final Runnable playNoteSequence = new Runnable() {
            @Override
            public void run() {
                //play the chord
                if(notesIndex < notes.length) {
                    playNote(notes[notesIndex++]);
                    handler.postDelayed(this, noteDelay);
                }
                //stop the chord
                else {
                    handler.postDelayed(turnOffAllNotes, sustainDelay);
                }
            }
        };

        handler.post(playNoteSequence);
    }

    private void playNote(int note){
        //todo: remove allocation
        byte[] event = new byte[3];
        event[0] = (byte) (0x90);  // 0x9* = note On, 0x*0 = channel 1
        event[1] =  Byte.parseByte(Integer.toString(note));
        event[2] = (byte) 0x7F;  // 0x7F = the maximum velocity (127)
        write(event);

        Log.d(TAG, "playing note: " + Integer.toString(note));
    }

    private void stopNote(int note) {
        //todo: remove allocation
        byte[] event = new byte[3];
        event[0] = (byte) (0x80);  // 0x8* = note Off, 0x*0 = channel 1
        event[1] = Byte.parseByte(Integer.toString(note));
        event[2] = (byte) 0x00;  // 0x00 = the minimum velocity (0)
        write(event);

        Log.d(TAG, "stopping note: " + Integer.toString(note));
    }
}
