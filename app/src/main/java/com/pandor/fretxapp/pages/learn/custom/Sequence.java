package com.pandor.fretxapp.pages.learn.custom;

import android.support.annotation.Nullable;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.Chord;


/**
 * Created by pandor on 3/7/17.
 */

class Sequence {
    private String name;
    private ArrayList<Chord> chords;

    Sequence(@Nullable String name, ArrayList<Chord> chords) {
        this.name = name;
        this.chords = chords;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Chord> getChords() {
        return chords;
    }

    public void setChords (ArrayList<Chord> chords) {
        this.chords = chords;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addChord(Chord chord) {
        chords.add(chord);
    }
}
