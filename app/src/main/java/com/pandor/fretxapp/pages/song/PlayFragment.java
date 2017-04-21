package com.pandor.fretxapp.pages.song;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pandor.fretxapp.R;


public class PlayFragment extends Fragment {

    public PlayFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pages_play, container, false);
    }
}