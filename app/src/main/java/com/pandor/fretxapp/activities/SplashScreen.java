package com.pandor.fretxapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.greysonparrelli.permiso.IOnComplete;
import com.greysonparrelli.permiso.IOnPermissionResult;
import com.greysonparrelli.permiso.IOnRationaleProvided;
import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.ResultSet;

import com.pandor.fretxapp.R;
import com.pandor.fretxapp.utils.Audio;
import com.pandor.fretxapp.utils.Bluetooth;
import com.pandor.fretxapp.utils.Midi;

public class SplashScreen extends BaseActivity {
    private static final String TAG = "KJKP6_PERMISO";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);
        Permiso.getInstance().setActivity(this);

        //Permiso callback called when the permission process is done
        Permiso.getInstance().setOnComplete(new IOnComplete() {
            public void onComplete(){
                Log.d(TAG, "Complete!");
                if (Bluetooth.getInstance().isEnabled()) {
                    Bluetooth.getInstance().scan();
                } else {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        //bluetooth callback called when the scan is done
        Bluetooth.getInstance().setOnUpdate(new Bluetooth.IOnUpdate() {
            public void onSuccess() {
                Log.d(TAG, "Success!");
                Bluetooth.getInstance().setOnUpdate(null);
                Bluetooth.getInstance().clearMatrix();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }

            public void onFailure() {
                Log.d(TAG, "Failure!");
                Bluetooth.getInstance().setOnUpdate(null);
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });

        //ask for permissions
        Permiso.getInstance().requestPermissions(
                new IOnPermissionResult() {
                    @Override
                    public void onPermissionResult(ResultSet resultSet) {
                        if (resultSet.isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
                            Log.d(TAG,"Record Audio permissions granted");
                            // Audio permission granted!
                            initAudio();
                        }
                        if (resultSet.isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
                            Log.d(TAG,"Phone permissions granted");
                            // Phone permission granted!
                            initAudio();
                        }
                        if (resultSet.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            // Location permission granted!
                            Log.d(TAG,"Location permissions granted");
                            initBluetooth();
                        }
                    }
                    @Override
                    public void onRationaleRequested(IOnRationaleProvided callback, String... permissions) {
                        Permiso.getInstance().showRationaleInDialog("FretX Permissions",
                                "These permissions are requested to connect to the FretX, to detect your chords and play sounds", null, callback);
                    }
                },
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        //initialize midi
        if (!Midi.getInstance().isEnabled()) {
            Midi.getInstance().init();
            Midi.getInstance().start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Permiso.getInstance().setActivity(this);
    }

    //redirect callback to Permiso
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
        Permiso.getInstance().setActivity(this);
    }

    //init audio
    private void initAudio() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && !Audio.getInstance().isEnabled()) {
            Audio.getInstance().init();
            Audio.getInstance().start();
        }
    }

    //init bluetooth
    private void initBluetooth() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && !Bluetooth.getInstance().isEnabled()) {
            Bluetooth.getInstance().init();
            Bluetooth.getInstance().start();
        }
    }
}