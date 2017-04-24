package com.pandor.fretxapp.pages.learn.scale;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pandor.fretxapp.R;
import com.pandor.fretxapp.fragments.ChordPicker;
import com.pandor.fretxapp.utils.Bluetooth.Bluetooth;
import com.pandor.fretxapp.views.FretboardView;

import rocks.fretx.audioprocessing.Scale;

public class LearnScaleExerciseFragment extends Fragment implements ChordPicker.ChordPickerListener {
	private FretboardView fretboardView;
	private TextView textChord;

    public LearnScaleExerciseFragment(){}

    public void onChordSelected(String root, String type) {
        Scale currentScale = new Scale(root, type);
        fretboardView.setFretboardPositions(currentScale.getFretboardPositions());
        textChord.setText(root + " " + type);
        Bluetooth.getInstance().setMatrix(currentScale);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ChordPicker chordPicker = ChordPicker.newInstance(this, Scale.ALL_ROOT_NOTES, Scale.ALL_SCALE_TYPES);
        ft.replace(R.id.chordPicker_place, chordPicker);
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        ft.addToBackStack(null);
        ft.commit();
        return inflater.inflate(R.layout.pages_learn_scale, container, false);
	}

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        textChord = (TextView) view.findViewById(R.id.textChord);
        fretboardView = (FretboardView) view.findViewById(R.id.fretboardView);
    }

}
