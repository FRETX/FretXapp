package com.pandor.fretxapp.pages.learn.exercise;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pandor.fretxapp.R;

import java.util.Locale;

/**
 * Created by pandor on 3/7/17.
 */

public class LearnExerciseDialog extends DialogFragment
{
    private static final String ELAPSED_TIME_MIN = "elapsed_time_min";
    private static final String ELAPSED_TIME_SEC = "elapsed_time_sec";
    private static final String LAST_EXERCISE = "last_exercise";

    private Dialog dialog;
    private boolean replay = true;

    interface LearnGuidedChordExerciseListener {
        void onUpdate(boolean replay);
    }

    public static LearnExerciseDialog newInstance(LearnGuidedChordExerciseListener listener, int min, int sec, boolean last) {
        LearnExerciseDialog dialog = new LearnExerciseDialog();
        dialog.setTargetFragment((Fragment) listener, 4321);
        Bundle args = new Bundle();
        args.putInt(ELAPSED_TIME_MIN, min);
        args.putInt(ELAPSED_TIME_SEC, sec);
        args.putBoolean(LAST_EXERCISE, last);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.pages_learn_exercise_dialog);

        //retrieve time from arguments
        int min = getArguments().getInt(ELAPSED_TIME_MIN);
        int sec = getArguments().getInt(ELAPSED_TIME_SEC);
        boolean last = getArguments().getBoolean(LAST_EXERCISE);
        getArguments().remove(ELAPSED_TIME_MIN);
        getArguments().remove(ELAPSED_TIME_SEC);
        getArguments().remove(LAST_EXERCISE);

        //display elapsed time
        TextView timeText = (TextView) dialog.findViewById(R.id.finishedElapsedTimeText);
        timeText.setText(String.format(Locale.getDefault(), "%1$02d:%2$02d", min, sec));

        //set button listeners
        Button button = (Button) dialog.findViewById(R.id.finishedBackButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        button = (Button) dialog.findViewById(R.id.finishedNextExerciseButton);
        if (last) {
            button.setVisibility(View.GONE);
        } else {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replay = false;
                    dialog.dismiss();
                }
            });
        }

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);

        Fragment parentFragment = getTargetFragment();
        ((LearnGuidedChordExerciseListener) parentFragment).onUpdate(replay);
    }
}