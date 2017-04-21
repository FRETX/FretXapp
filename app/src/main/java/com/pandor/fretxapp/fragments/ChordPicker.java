package com.pandor.fretxapp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pandor.fretxapp.R;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * FretXapp for FretX
 * Created by pandor on 19/04/17 00:24.
 */

public class ChordPicker extends Fragment {
    private static final String ROOT = "root";
    private static final String TYPE = "type";

    private Activity activity;
    private LinearLayout rootNoteView;
    private LinearLayout chordTypeView;
    private ArrayList<String> rootArray;
    private ArrayList<String> typeArray;
    private String root;
    private String type;
    private int primary;
    private int tertiaryText;

    public interface ChordPickerListener {
        void onChordSelected(String root, String type);
    }

    public static ChordPicker newInstance(ChordPickerListener listener, String[] rootNotes, String[] chordTypes) {
        ChordPicker chordPicker = new ChordPicker();
        chordPicker.setTargetFragment((Fragment) listener, 1234);
        Bundle args = new Bundle();
        args.putStringArrayList(ROOT, new ArrayList<>(Arrays.asList(rootNotes)));
        args.putStringArrayList(TYPE, new ArrayList<>(Arrays.asList(chordTypes)));
        chordPicker.setArguments(args);
        return chordPicker;
    }

    public ChordPicker() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootArray = getArguments().getStringArrayList(ROOT);
        typeArray = getArguments().getStringArrayList(TYPE);
        //getArguments().remove(ROOT);
        //getArguments().remove(TYPE);

        return inflater.inflate(R.layout.fragment_chord_picker, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        activity = getActivity();

        final TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        primary = typedValue.data;

        activity.getTheme().resolveAttribute(R.attr.colorButtonNormal, typedValue, true);
        tertiaryText = typedValue.data;

        rootNoteView = (LinearLayout) view.findViewById(R.id.chordPickerRootNoteView);
        chordTypeView = (LinearLayout) view.findViewById(R.id.chordPickerTypeView);

        //root note on click listener
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clean(view, rootNoteView);
                root = ((TextView) view).getText().toString();
                update();
            }
        };

        //root note linear layout
        fillLinearLayout(rootNoteView, rootArray, onClickListener);

        //note type on click listener
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clean(view, chordTypeView);
                type = ((TextView) view).getText().toString();
                update();
            }
        };

        //note type linear layout
        fillLinearLayout(chordTypeView, typeArray, onClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();

        TextView initialRoot = (TextView) rootNoteView.getChildAt(0);
        TextView initialType = (TextView) chordTypeView.getChildAt(0);
        initialRoot.setBackgroundResource(R.drawable.view_chordpicker_background);
        initialType.setBackgroundResource(R.drawable.view_chordpicker_background);
        root = initialRoot.getText().toString();
        type = initialType.getText().toString();
        update();
    }

    private void fillLinearLayout(LinearLayout layout, ArrayList<String> content, View.OnClickListener listener) {
        for (String str : content) {
            TextView tmpTextView = new TextView(activity);
            tmpTextView.setText(str);
            tmpTextView.setTextSize(26);
            tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.addView(tmpTextView);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
            params.setMargins(30, 0, 30, 0);
            tmpTextView.setLayoutParams(params);
            tmpTextView.setBackgroundColor(primary);
            tmpTextView.setTextColor(tertiaryText);
            tmpTextView.setOnClickListener(listener);
        }
    }

    private void clean(View view, LinearLayout layout) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View v = layout.getChildAt(i);
            if (v instanceof TextView) {
                v.setBackgroundResource(0);
                v.setBackgroundColor(primary);
            }
        }
        view.setBackgroundResource(R.drawable.view_chordpicker_background);
    }

    private void update() {
        Fragment parentFragment = getTargetFragment();
        ((ChordPickerListener) parentFragment).onChordSelected(root, type);
    }
}
