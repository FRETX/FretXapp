package com.pandor.fretxapp.pages.learn;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pandor.fretxapp.R;
import com.pandor.fretxapp.pages.learn.custom.LearnCustomBuilderFragment;
import com.pandor.fretxapp.pages.learn.guided.LearnGuidedListFragment;
import com.pandor.fretxapp.pages.learn.scale.LearnScaleExerciseFragment;

public class LearnButtonsFragment extends Fragment {

    public LearnButtonsFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pages_learn_buttons, container, false);
    }

	@Override
    public void onViewCreated(View v, Bundle savedInstanceState){
        final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        final CardView btExerciseOne = (CardView)v.findViewById(R.id.btExerciseOne);
        btExerciseOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.learn_container, new LearnCustomBuilderFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                fragmentManager.executePendingTransactions();
            }
        });
        final CardView btExerciseTwo = (CardView)v.findViewById(R.id.btExerciseTwo);
        btExerciseTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.learn_container, new LearnScaleExerciseFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                fragmentManager.executePendingTransactions();
            }
        });

        final CardView btGuidedChordExercise = (CardView)v.findViewById(R.id.btGuidedChordExercises);
        btGuidedChordExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.learn_container, new LearnGuidedListFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                fragmentManager.executePendingTransactions();
            }
        });
    }
}