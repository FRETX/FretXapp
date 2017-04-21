package com.pandor.fretxapp.pages.learn.guided;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.pandor.fretxapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LearnGuidedListFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.pages_learn_guided_list, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle b){
		final GridView gridView = (GridView) v.findViewById(R.id.guidedChordExerciseList);
		final ArrayList<GuidedChordExercise> exercises = new ArrayList<>();

		//// TODO: 19/04/17 implement this in backend and use AppCache.getFromCache
		InputStream is = getActivity().getResources().openRawResource(R.raw.guided_chord_exercises_json);

		//// TODO: 19/04/17 move this code to json in utils
		StringBuilder contents = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String text;
		try {
			while ((text = reader.readLine()) != null) {
				contents.append(text).append(System.getProperty("line.separator"));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			JSONArray guidedExercises = new JSONArray(contents.toString());
			for (int i = 0; i < guidedExercises.length(); i++) {
				JSONObject exerciseJson = guidedExercises.getJSONObject(i);
				GuidedChordExercise exercise = new GuidedChordExercise(exerciseJson);
				exercises.add(exercise);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		gridView.setAdapter(new LearnGuidedListAdapter(R.layout.pages_learn_guided_list_item, exercises));
	}
}
