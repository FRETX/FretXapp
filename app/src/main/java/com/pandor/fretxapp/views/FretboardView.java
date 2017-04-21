package com.pandor.fretxapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.pandor.fretxapp.R;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.FretboardPosition;

/**
 * FretXapp for FretX
 * Created by onurb_000 on 03/12/16.
 */

public class FretboardView extends View {
    //private static final String TAG = "KJKP6_FRETBOARDVIEW";
    private static final int NB_FRETS = 4;
    private static final int NB_STRINGS = 6;
    private static final float LED_RADIUS = 0.05f;

    //Fretboard positions
	private ArrayList<FretboardPosition> fretboardPositions;
    private final int[] fretPositions = new int[NB_FRETS + 1];
    private final int[] stringPositions = new int[NB_STRINGS];

    //fretboard
    private final Drawable fretboard;
    private final Rect fretboardBounds = new Rect();
    private int fretboardHeight;
    private int fretboardWidth;

    //leds
    private final Drawable redLed;
    private final Drawable blueLed;
    private int ledRadius;

	public FretboardView(Context context, AttributeSet attrs){
		super(context,attrs);

        //fretboard
        fretboard = getContext().getResources().getDrawable(R.drawable.view_fretboard, null);
        fretboardWidth = fretboard.getIntrinsicWidth();
        fretboardHeight = fretboard.getIntrinsicHeight();

        //finger positions
        redLed = getContext().getResources().getDrawable(R.drawable.view_fretboard_red_led, null);
        blueLed = getContext().getResources().getDrawable(R.drawable.view_fretboard_blue_led, null);
    }

	public void setFretboardPositions(ArrayList<FretboardPosition> fps){
		fretboardPositions = fps;
		invalidate();
	}

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        final float ratioX = (float) w / fretboardWidth;
        final float ratioY = (float) h / fretboardHeight;

        //compute the fretboard bounds
        int fretboardLeft;
        int fretboardTop;
        int fretboardRight;
        int fretboardBottom;
        if (ratioX < ratioY) {
            fretboardLeft = 0;
            fretboardTop = (int)((h - fretboardHeight * ratioX) / 2);
            fretboardRight = w;
            fretboardBottom = (int)(fretboardTop + fretboardHeight * ratioX);
            ledRadius = (int)(w * LED_RADIUS);
        } else {
            fretboardLeft = (int)((w - fretboardWidth * ratioY) / 2);
            fretboardTop = 0;
            fretboardRight = (int)(fretboardLeft + fretboardWidth * ratioY);
            fretboardBottom = h;
            ledRadius = (int)(h * LED_RADIUS);
        }
        fretboardBounds.set(fretboardLeft, fretboardTop, fretboardRight, fretboardBottom);
        fretboard.setBounds(fretboardBounds);

        //compute the finger positions
        fretPositions[0] = fretboardLeft + (int)((fretboardRight - fretboardLeft) * 0.045);
        for (int i = 1; i < NB_FRETS + 1; ++i) {
            fretPositions[i] = fretboardLeft + (int)((i - 0.4) * (fretboardRight - fretboardLeft) / NB_FRETS);
        }
        for (int i = 0; i < NB_STRINGS; ++i) {
            stringPositions[i] = fretboardTop + (int)(((fretboardBottom - fretboardTop) / (NB_STRINGS + 0.6)) * (i + 0.9));
        }
    }

    @Override
	protected void onDraw(Canvas canvas){
        //draw fretboard
        fretboard.draw(canvas);

        //draw finger positions
		if(fretboardPositions != null){
			for (int i = 0; i < fretboardPositions.size(); i++) {
                FretboardPosition fp = fretboardPositions.get(i);
				int fret = fp.getFret();
                int string = fp.getString() - 1;

                if(fret == -1 || fret > NB_FRETS)
				    continue;

                Drawable currentLed = fret == 0 ? blueLed : redLed;
                currentLed.setBounds(fretPositions[fret] - ledRadius,
                        stringPositions[string] - ledRadius,
                        fretPositions[fret] + ledRadius,
                        stringPositions[string] + ledRadius);
				currentLed.draw(canvas);
			}
		}
	}
}