package com.pandor.fretxapp.utils;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.pandor.fretxapp.activities.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.MusicUtils;
import rocks.fretx.audioprocessing.Scale;

import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * FretXapp for FretX
 * Created by pandor on 14/04/17 14:20.
 */

public class Bluetooth {
    private static final String TAG = "KJKP6_BLUETOOTH_UTIL";
    private static final int SCAN_DELAY = 8000;
    private static final int TURN_ON_INTERVAL = 100;
    private static final int TURN_ON_DELAY = 2000;
    private static final String DEVICE_NAME = "FretX";
    private static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    private boolean enabled;
    private boolean scanning;
    private BluetoothAdapter adapter;
    //private final SparseArray<BluetoothDevice> devices = new SparseArray<>();
    private final Handler handler = new Handler();
    private BluetoothGatt connectedGatt;
    private BluetoothGattCharacteristic RxChar;
    private final byte[] clear = new byte[]{0};
    private ProgressDialog mProgress;
    private IOnUpdate onUpdate;
    private int turn_on_try = 0;

    private HashMap<String,FingerPositions> chordFingerings;

    /* = = = = = = = = = = = = = = = = = SINGLETON PATTERN = = = = = = = = = = = = = = = = = = = */
    private static class Holder {
        private static final Bluetooth instance = new Bluetooth();
    }

    private Bluetooth() {
    }

    public static Bluetooth getInstance() {
        return Holder.instance;
    }

    /* = = = = = = = = = = = = = = = = = = = BLUETOOTH = = = = = = = = = = = = = = = = = = = = = */
    public void init() {
        Log.d(TAG, "init");
        enabled = true;
        chordFingerings = MusicUtils.parseChordDb();
        BluetoothManager manager = (BluetoothManager) BaseActivity.getActivity().getSystemService(BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        if (adapter == null) {
            Log.d(TAG, "No bluetooth adapter");
            enabled = false;
        } else if (!BaseActivity.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "No Bluetooth low energy");
            enabled = false;
        } else {
            enabled = true;
        }
    }

    public void start() {
        Log.d(TAG, "start");
    }

    public void stop(){
        handler.removeCallbacksAndMessages(null);
        if (scanning)
            stopScan();
        mProgress.dismiss();
        Log.d(TAG, "stop");
    }

    public boolean isEnabled() {
        return enabled;
    }

    /* = = = = = = = = = = = = = = = = = = = SCANNING = = = = = = = = = = = = = = = = = = = = = */
    public void scan() {
        mProgress = new ProgressDialog(BaseActivity.getActivity());
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);
        mProgress.setMessage("Enabling...");
        mProgress.show();

        Log.d(TAG, "enabling...");
        if(!adapter.isEnabled()) {
            adapter.enable();
            handler.postDelayed(enable, TURN_ON_INTERVAL);
        } else {
            startScan();
        }
    }

    //// TODO: 21/04/17 use 2 runnable different to avoid if forest
    private Runnable enable = new Runnable() {
        @Override
        public void run() {
            if (adapter.getBluetoothLeScanner() == null) {
                ++turn_on_try;
                if (turn_on_try * TURN_ON_INTERVAL <= TURN_ON_DELAY) {
                    Log.d(TAG, "Postpone startScan");
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(enable, TURN_ON_INTERVAL);
                } else {
                    Log.d(TAG, "Enable failed");
                    mProgress.dismiss();
                    if (onUpdate != null)
                        onUpdate.onFailure();
                }
            } else {
                mProgress.setMessage("Scanning...");
                startScan();
            }
        }
    };

    private void startScan() {
        Log.d(TAG, "scanning...");
        mProgress.setMessage("Scanning...");
        //devices.clear();
        ScanSettings settings = new ScanSettings.Builder().build();
        ScanFilter filter = new ScanFilter.Builder().setDeviceName(DEVICE_NAME).build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(filter);
        scanning = true;
        adapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
        handler.postDelayed(endOfScan, SCAN_DELAY);
    }

    private void stopScan() {
        Log.d(TAG, "stop scanning");
        scanning = false;
        adapter.getBluetoothLeScanner().stopScan(scanCallback);
    }

    public boolean isScanning() {
        return scanning;
    }

    private Runnable endOfScan = new Runnable() {
        @Override
        public void run() {

            /*
            if (devices.size() == 1) {
                stopScan();
                connect(devices.valueAt(0));
            } else {
                Log.d(TAG, "no device found");
                if (onUpdate != null) {
                    mProgress.dismiss();
                    onUpdate.onFailure();
                }
            }
            */
        }
    };

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            stopScan();
            handler.removeCallbacksAndMessages(null);
            connect(result.getDevice());

            //BluetoothDevice device = result.getDevice();
            //devices.put(device.hashCode(), device);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "New BLE Devices");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            if (errorCode == SCAN_FAILED_ALREADY_STARTED)
                Log.d(TAG, "Scan failed: SCAN_FAILED_ALREADY_STARTED");
            else if (errorCode == SCAN_FAILED_APPLICATION_REGISTRATION_FAILED)
                Log.d(TAG, "Scan failed: SCAN_FAILED_ALREADY_STARTED");
            else if (errorCode == SCAN_FAILED_FEATURE_UNSUPPORTED)
                Log.d(TAG, "Scan failed: SCAN_FAILED_ALREADY_STARTED");
            else if (errorCode == SCAN_FAILED_INTERNAL_ERROR)
                Log.d(TAG, "Scan failed: SCAN_FAILED_ALREADY_STARTED");

            mProgress.dismiss();

            if (onUpdate != null) {
                onUpdate.onFailure();
            }
        }
    };

    /* = = = = = = = = = = = = = = = = = = = CONNECTING = = = = = = = = = = = = = = = = = = = = = */
    private void connect(BluetoothDevice device) {
        Log.d(TAG, "connecting");
        mProgress.setMessage("Connecting...");
        connectedGatt = device.connectGatt(BaseActivity.getActivity(), false, gattCallback);
    }

    public void disconnect() {
        if (connectedGatt != null) {
            Log.d(TAG, "disconnecting");
            connectedGatt.disconnect();
            connectedGatt = null;
        }
    }

    public boolean isConnected() {
        return !(connectedGatt == null);
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                mProgress.setMessage("Discovering services...");
                Log.d(TAG, "connected");
                gatt.discoverServices();
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "disconnected");
                connectedGatt = null;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "failure, disconnecting");
                gatt.disconnect();
                connectedGatt = null;
                mProgress.dismiss();
                if (onUpdate != null) {
                    onUpdate.onFailure();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            BluetoothGattService RxService = connectedGatt.getService(RX_SERVICE_UUID);
            RxChar = RxService.getCharacteristic(RX_CHAR_UUID);

            mProgress.dismiss();
            if (onUpdate != null) {
                onUpdate.onSuccess();
            }
        }
    };

    /* = = = = = = = = = = = = = = = = = = = = = MATRIX = = = = = = = = = = = = = = = = = = = = = */
    public void setMatrix(Chord chord) {
        if (connectedGatt == null || RxChar == null)
            return;
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(chord.toString(), chordFingerings);
        RxChar.setValue(bluetoothArray);
        connectedGatt.writeCharacteristic(RxChar);
    }

    public void setMatrix(Scale scale) {
        if (connectedGatt == null || RxChar == null)
            return;
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(scale.toString(), chordFingerings);
        RxChar.setValue(bluetoothArray);
        connectedGatt.writeCharacteristic(RxChar);
    }

    public void clearMatrix() {
        if (connectedGatt == null || RxChar == null)
            return;
        RxChar.setValue(clear);
        connectedGatt.writeCharacteristic(RxChar);
    }

    /* = = = = = = = = = = = = = = = = = = = LISTENERS = = = = = = = = = = = = = = = = = = = = = */
    public void setOnUpdate(@Nullable IOnUpdate onUpdate) {
        this.onUpdate = onUpdate;
    }

    public interface IOnUpdate {
        void onFailure();
        void onSuccess();
    }
}
