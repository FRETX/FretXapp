package com.pandor.fretxapp.pages.learn.exercise;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pandor.fretxapp.R;
import com.pandor.fretxapp.pages.learn.guided.GuidedChordExercise;
import com.pandor.fretxapp.utils.Audio.Audio;
import com.pandor.fretxapp.utils.Audio.AudioListener;
import com.pandor.fretxapp.utils.Midi;
import com.pandor.fretxapp.utils.TimeUpdater;
import com.pandor.fretxapp.views.FretboardView;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.Chord;

public class LearnExerciseFragment extends Fragment implements AudioListener,
        LearnExerciseDialog.LearnGuidedChordExerciseListener {
    private final static String LIST_INDEX = "list_index";
    private final static String LIST = "list";
    private final static String CHORDS = "chords";

	//view
	private FretboardView fretboardView;
    private TextView chordText;
    private TextView chordNextText;
	private TextView positionText;
    private Button playButton;
    private ProgressBar chordProgress;
    private ImageView thresholdImage;

	//chords
	int nRepetitions;
    int chordIndex;
	ArrayList<Chord> exerciseChords;
    private TimeUpdater timeUpdater;
    private AlertDialog dialog;
    private ArrayList<GuidedChordExercise> exerciseList;
    private int listIndex;

    public LearnExerciseFragment() {}

    public static LearnExerciseFragment newInstance(ArrayList<GuidedChordExercise> exerciseList, int listIndex) {
        LearnExerciseFragment fragment = new LearnExerciseFragment();
        Bundle args = new Bundle();
        args.putInt(LIST_INDEX, listIndex);
        args.putSerializable(LIST, exerciseList);
        fragment.setArguments(args);
        return fragment;
    }

    public static LearnExerciseFragment newInstance(@NonNull ArrayList<Chord> chords) {
        LearnExerciseFragment fragment = new LearnExerciseFragment();
        Bundle args = new Bundle();
        args.putSerializable(CHORDS, chords);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                exerciseChords = (ArrayList<Chord>) getArguments().getSerializable(CHORDS);
        if (exerciseChords != null) {
            getArguments().remove(CHORDS);
        } else {
            exerciseList = (ArrayList<GuidedChordExercise>) getArguments().getSerializable(LIST);
            listIndex = getArguments().getInt(LIST_INDEX);
            getArguments().remove(LIST);
            getArguments().remove(LIST_INDEX);

            GuidedChordExercise exercise = exerciseList.get(listIndex);

            this.nRepetitions = exercise.getRepetition();
            ArrayList<Chord> repeatedChords = new ArrayList<>();
            for (int i = 0; i < exercise.getRepetition(); i++) {
                repeatedChords.addAll(exercise.getChords());
            }
            exerciseChords = (ArrayList<Chord>) repeatedChords.clone();
        }

        Audio.getInstance().setAudioDetectorListener(this);

        return inflater.inflate(R.layout.pages_learn_exercise_layout, container, false);
	}

    @Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
        fretboardView = (FretboardView) v.findViewById(R.id.fretboardView);
        positionText = (TextView) v.findViewById(R.id.position);
        chordText = (TextView) v.findViewById(R.id.textChord);
        chordNextText = (TextView) v.findViewById(R.id.textNextChord);
        playButton = (Button) v.findViewById(R.id.playChordButton);
        chordProgress = (ProgressBar) v.findViewById(R.id.chord_progress);
        thresholdImage = (ImageView) v.findViewById(R.id.audio_thresold);

        chordIndex = 0;

        TextView timeText = (TextView) v.findViewById(R.id.time);
        timeUpdater = new TimeUpdater(timeText);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stop listening
                playButton.setClickable(false);
                Audio.getInstance().stopListening();

                //check if music volume is up
                AudioManager audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) < 5) {
                    Toast.makeText(getActivity(), "Volume is low", Toast.LENGTH_SHORT).show();
                }

                //play the chord
                Midi.getInstance().playChord(exerciseChords.get(chordIndex));

                //start listening after delay
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playButton.setClickable(true);
                        Audio.getInstance().startListening();
                    }
                }, 1500);
            }
        });
	}

	private void resumeAll() {
        if (exerciseChords.size() > 0 && chordIndex < exerciseChords.size()) {
            Audio.getInstance().setTargetChords(exerciseChords);
            setChord();
            timeUpdater.resumeTimer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeAll();
    }

    private void pauseAll() {
        timeUpdater.pauseTimer();
        Audio.getInstance().stopListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseAll();
    }

    //retrieve result of the finished exercise dialog
    @Override
    public void onUpdate(boolean replay) {
        //replay the actual exercise
        if (replay) {
            chordIndex = 0;
            timeUpdater.resetTimer();
            resumeAll();
        }
        //goes to the next exercise
        else {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.learn_container, LearnExerciseFragment.newInstance(exerciseList, listIndex + 1));
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            fragmentManager.executePendingTransactions();
        }
    }

    public void onProgress() {
        double progress = Audio.getInstance().getProgress();
        //chord totally played
        if (progress >= 100) {
            chordProgress.setProgress(100);
            ++chordIndex;

            //end of the exercise
            if (chordIndex == exerciseChords.size()) {
                pauseAll();
                setPosition();
                LearnExerciseDialog dialog = LearnExerciseDialog.newInstance(this,
                        timeUpdater.getMinute(), timeUpdater.getSecond(),
                        exerciseList == null || listIndex == exerciseList.size() - 1);
                dialog.show(getFragmentManager(), "dialog");
            }
            //middle of an exercise
            else {
                setChord();
            }
        }
        //chord in progress
        else {
            chordProgress.setProgress((int)progress);
        }
    }

    public void onLowVolume() {
        thresholdImage.setImageResource(android.R.drawable.presence_audio_busy);
    }

    public void onHighVolume() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        thresholdImage.setImageResource(android.R.drawable.presence_audio_online);
    }

    public void onTimeout() {
        dialog = audioHelperDialog(getActivity());
        dialog.show();
    }

    //setup everything according actual chord
    private void setChord() {
        Chord actualChord = exerciseChords.get(chordIndex);
        Log.d("DEBUG_YOLO", "setChord " + actualChord.toString());

        //update chord title
        chordText.setText(actualChord.toString());
        if (chordIndex + 1 < exerciseChords.size())
            chordNextText.setText(exerciseChords.get(chordIndex + 1).toString());
        else
            chordNextText.setText("");
        //update finger position
        fretboardView.setFretboardPositions(actualChord.getFingerPositions());
		//update positionText
		setPosition();
        //update chord listener
        Audio.getInstance().setTargetChord(actualChord);
        Audio.getInstance().startListening();
        //setup the progress bar\
        chordProgress.setProgress(0);
        //update led
        //byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(actualChord.toString(), chordDb);
        //BluetoothClass.sendToFretX(bluetoothArray);
    }

    //display chord position
    private void setPosition() {
        positionText.setText(chordIndex + "/" + exerciseChords.size());
    }

    //create a audio helper dialog
    private AlertDialog audioHelperDialog(Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //todo change text of dialog
        alertDialogBuilder.setTitle("Audio Detector")
                .setMessage("Common guys . . .")
                .setCancelable(false)
                .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder.create();
    }
}
