package com.pandor.fretxapp.pages.chord;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pandor.fretxapp.fragments.ChordPicker;
import com.pandor.fretxapp.R;
import com.pandor.fretxapp.utils.Bluetooth.Bluetooth;
import com.pandor.fretxapp.utils.Firebase;
import com.pandor.fretxapp.utils.Midi;
import com.pandor.fretxapp.views.FretboardView;

import rocks.fretx.audioprocessing.Chord;

public class ChordFragment extends Fragment implements ChordPicker.ChordPickerListener {
    private Chord currentChord;
    private TextView textChord;
    private FretboardView fretboardView;

	public ChordFragment(){}

	public void onChordSelected(String root, String type) {
		currentChord = new Chord(root, type);
		fretboardView.setFretboardPositions(currentChord.getFingerPositions());
		textChord.setText(root + " " + type);
        Bluetooth.getInstance().setMatrix(currentChord);
	}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //firebase log event
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Chords");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "TAB");
        Firebase.getAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //view
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ChordPicker chordPicker = ChordPicker.newInstance(this, Chord.ALL_ROOT_NOTES, Chord.ALL_CHORD_TYPES);
        ft.replace(R.id.chordPicker_place, chordPicker);
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        ft.commit();
        return inflater.inflate(R.layout.pages_chord, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        textChord = (TextView) view.findViewById(R.id.textChord);
        fretboardView = (FretboardView) view.findViewById(R.id.fretboardView);
        Button playChordButton = (Button) view.findViewById(R.id.playChordButton);
        playChordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Midi.getInstance().playChord(currentChord);
            }
        });
    }
}