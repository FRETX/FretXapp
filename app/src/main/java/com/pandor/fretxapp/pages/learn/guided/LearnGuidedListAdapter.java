package com.pandor.fretxapp.pages.learn.guided;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pandor.fretxapp.R;
import com.pandor.fretxapp.activities.BaseActivity;
import com.pandor.fretxapp.pages.learn.exercise.LearnExerciseFragment;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.Chord;

class LearnGuidedListAdapter extends ArrayAdapter<GuidedChordExercise> {
    private int layoutResourceId;
	private ArrayList<GuidedChordExercise> data;

	LearnGuidedListAdapter(int layoutResourceId, ArrayList<GuidedChordExercise> data){
		super(BaseActivity.getActivity(), layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.data = data;
	}

	@NonNull
	public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
		View row = convertView;
		RecordHolder holder;

		if (row == null) {
			LayoutInflater inflater = BaseActivity.getActivity().getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new RecordHolder();
			holder.name = (TextView) row.findViewById(R.id.guidedChordExerciseName);
			holder.chords = (TextView) row.findViewById(R.id.guidedChordExerciseChords);
			row.setTag(holder);
		} else {
			holder = (RecordHolder) row.getTag();
		}
		final GuidedChordExercise item = data.get(position);
		holder.name.setText(item.getName());
		String chordsString = "";
		for (Chord chord: item.getChords()) {
			chordsString += chord.toString() + " ";
		}
		holder.chords.setText(chordsString);
		row.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                FragmentManager fragmentManager = BaseActivity.getActivity().getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.replace(R.id.learn_container, LearnExerciseFragment.newInstance(data, position));
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
                fragmentManager.executePendingTransactions();
			}
		});
		return row;
	}

	private static class RecordHolder{
		TextView name;
		TextView chords;
	}
}
