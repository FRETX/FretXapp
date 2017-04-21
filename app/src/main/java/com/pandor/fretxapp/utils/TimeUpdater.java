package com.pandor.fretxapp.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.widget.TextView;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * FretXapp for FretX
 * Created by pandor on 11/04/17 14:56.
 */

public class TimeUpdater {
    //private static final String TAG = "KJKP6_TIMEUPDATER";
    private Timer timer;
    private final TextView timeText;
    private final Handler handler = new Handler();
    private TimerTask timerTask;
    private int second;
    private int minute;

    @SuppressLint("SetTextI18n")
    public TimeUpdater(TextView timeText) {
        this.timeText = timeText;
        if (timeText != null)
            timeText.setText("00:00");
    }

    @SuppressLint("SetTextI18n")
    public void resetTimer() {
        //zero time
        second = 0;
        minute = 0;
        timeText.setText("00:00");
    }

    public void pauseTimer() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timerTask.cancel();
            timer.cancel();
            timer = null;
        }
    }

    public void resumeTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run update text view
                handler.post(new Runnable() {
                    public void run() {
                        //update time
                        ++second;
                        if (second == 60) {
                            ++minute;
                            second = 0;
                        }
                        if (minute == 60) {
                            minute = 0;
                        }

                        //update textView
                        timeText.setText(String.format(Locale.getDefault(),
                                "%1$02d:%2$02d", minute, second));
                    }
                });
            }
        };

        //schedule the timer
        timer.schedule(timerTask, 1000, 1000); //
    }

    public int getSecond() {
        return second;
    }

    public int getMinute() {
        return minute;
    }
}
