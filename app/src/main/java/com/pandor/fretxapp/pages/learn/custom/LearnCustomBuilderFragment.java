package com.pandor.fretxapp.pages.learn.custom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.pandor.fretxapp.R;
import com.pandor.fretxapp.fragments.ChordPicker;
import com.pandor.fretxapp.pages.learn.exercise.LearnExerciseFragment;
import com.pandor.fretxapp.utils.Bluetooth;
import com.pandor.fretxapp.views.FretboardView;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.Chord;

/**
 * Created by onurb_000 on 15/12/16.
 */

public class LearnCustomBuilderFragment extends Fragment
        implements LearnCustomBuilderDialog.LearnCustomChordExerciseListener,
        ChordPicker.ChordPickerListener {
	//view
	private FretboardView fretboardView;
    private TextView chordText;

	//chords
	private Chord currentChord;
	private ArrayList<Sequence> sequences;
	private int currentSequenceIndex;

	public LearnCustomBuilderFragment(){}

    public void onChordSelected(String root, String type) {
        currentChord = new Chord(root, type);
        fretboardView.setFretboardPositions(currentChord.getFingerPositions());
        chordText.setText(root + " " + type);
        Bluetooth.getInstance().setMatrix(currentChord);
    }

    public void onUpdate(ArrayList<Sequence> sequences, int currentSequenceIndex){
        this.sequences = sequences;
        if (this.sequences.size() == 0)
        {
            sequences.add(new Sequence(null, new ArrayList<Chord>()));
            this.currentSequenceIndex = 0;
        } else {
            this.currentSequenceIndex = currentSequenceIndex;
        }
    }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//retrieve saved sequences
		sequences = LearnCustomBuilderJson.load(getContext());

		//add a new empty sequence
		sequences.add(0, new Sequence(null, new ArrayList<Chord>()));
		currentSequenceIndex = 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ChordPicker chordPicker = ChordPicker.newInstance(this, Chord.ALL_ROOT_NOTES, Chord.ALL_CHORD_TYPES);
        ft.replace(R.id.chordPicker_place, chordPicker);
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        ft.addToBackStack(null);
        ft.commit();

		//setup view
        return inflater.inflate(R.layout.pages_learn_custom_builder, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle b){
        fretboardView = (FretboardView) v.findViewById(R.id.fretboardView);
        chordText = (TextView) v.findViewById(R.id.textChord);
        Button addButton = (Button) v.findViewById(R.id.addChordButton);
        Button addedButton = (Button) v.findViewById(R.id.addedChordButton);
        Button startButton = (Button) v.findViewById(R.id.startExerciseButton);

        final LearnCustomBuilderDialog.LearnCustomChordExerciseListener listener = this;

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sequences.get(currentSequenceIndex).addChord(currentChord);
                Toast.makeText(getContext(), currentChord.toString() + " added", Toast.LENGTH_SHORT).show();
            }
        });

        addedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LearnCustomBuilderDialog dialog =
                        LearnCustomBuilderDialog.newInstance(listener, sequences,
                                currentSequenceIndex);
                dialog.show(getFragmentManager(), "dialog");
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Chord> chords = sequences.get(currentSequenceIndex).getChords();
                if(chords.size() < 1) {
                    Toast.makeText(getContext(), "Sequence is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.learn_container, LearnExerciseFragment.newInstance(chords));
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                fragmentManager.executePendingTransactions();
            }
        });

	}
}
