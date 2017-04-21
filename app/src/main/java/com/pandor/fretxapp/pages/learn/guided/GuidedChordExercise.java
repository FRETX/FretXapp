package com.pandor.fretxapp.pages.learn.guided;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.Chord;

public class GuidedChordExercise {
	private String name;
	private String id;
	private final ArrayList<Chord> chords = new ArrayList<>();
	private int nRepetitions;

	GuidedChordExercise(JSONObject chordExercise){
		JSONObject chordJson;
		JSONArray tmpChordsArray;
		try {
			this.name = chordExercise.getString("name");
			this.id = chordExercise.getString("id");
			this.nRepetitions = chordExercise.getInt("nRepetitions");
			tmpChordsArray = chordExercise.getJSONArray("chords");
			for (int j = 0; j < tmpChordsArray.length(); j++) {
				chordJson = tmpChordsArray.getJSONObject(j);
				Log.d("adding chord", chordJson.getString("root") + chordJson.getString("type"));
				this.chords.add(new Chord(chordJson.getString("root"), chordJson.getString("type")));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public int getRepetition() {
		return nRepetitions;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public ArrayList<Chord> getChords() {
		return chords;
	}
}
