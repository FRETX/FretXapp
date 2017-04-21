package com.pandor.fretxapp.pages.learn.custom;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import rocks.fretx.audioprocessing.Chord;

/**
 * Created by pandor on 3/7/17.
 */

class LearnCustomBuilderJson {
    private final static String FILENAME = "customChordExerciseSaves.json";

    private static void chordToJson(StringBuffer sb, Chord chord) {
        sb.append("{\"root\":\"");
        sb.append(chord.getRoot());
        sb.append("\",\"type\":\"");
        sb.append(chord.getType());
        sb.append("\"}");
    }

    private static StringBuffer chordArrayToJson(StringBuffer sb, ArrayList<Chord> chords) {
        sb.append("[");
        for (int index = 0; index < chords.size(); index++) {
            chordToJson(sb, chords.get(index));
            if (index + 1 != chords.size()) {
                sb.append(",");
            } else {
                sb.append("]");
            }
        }
        return sb;
    }

    private static StringBuffer sequenceToJson(StringBuffer sb, Sequence sequence) {
        sb.append("{\"name\":\"");
        sb.append(sequence.getName());
        sb.append("\",\"chords\":");
        chordArrayToJson(sb, sequence.getChords());
        sb.append("}");
        return sb;
    }

    private static StringBuffer sequenceArrayToJson(StringBuffer sb, ArrayList<Sequence> sequences) {
        sb.append("[");
        for (int index = 0; index < sequences.size(); index++) {
            sequenceToJson(sb, sequences.get(index));
            if (index + 1 != sequences.size()) {
                sb.append(",");
            } else {
                sb.append("]");
            }
        }
        return sb;
    }

    static void save(Context context, ArrayList<Sequence> sequences) {
        StringBuffer sb = new StringBuffer("{\"sequences\":");
        sequenceArrayToJson(sb, sequences);
        sb.append("}");

        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            outputStream.write(sb.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static ArrayList<Sequence> load(Context context) {
        String jsonSave;
        ArrayList<Sequence> sequences = new ArrayList<>();

        try {
            FileInputStream fis = context.openFileInput(FILENAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            isr.close();
            fis.close();
            jsonSave = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return sequences;
        }

        try {
            JSONObject jsonRoot = new JSONObject(jsonSave);
            JSONArray jsonSequences = jsonRoot.getJSONArray("sequences");
            for (int sequenceIndex = 0; sequenceIndex < jsonSequences.length(); sequenceIndex++) {
                JSONObject jsonSequence = jsonSequences.getJSONObject(sequenceIndex);
                JSONArray jsonChords = jsonSequence.getJSONArray("chords");
                String sequenceName = jsonSequence.getString("name");
                ArrayList<Chord> sequenceChords = new ArrayList<>();
                for (int chordIndex = 0; chordIndex < jsonChords.length(); chordIndex++) {
                    JSONObject jsonChord = jsonChords.getJSONObject(chordIndex);
                    String chordRoot = jsonChord.getString("root");
                    String chordType = jsonChord.getString("type");
                    Chord chord = new Chord(chordRoot, chordType);
                    sequenceChords.add(chord);
                }
                Sequence sequence = new Sequence(sequenceName, sequenceChords);
                sequences.add(sequence);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return sequences;
        }
        return sequences;
    }
}
