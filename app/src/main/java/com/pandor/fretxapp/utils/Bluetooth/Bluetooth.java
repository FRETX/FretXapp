package com.pandor.fretxapp.utils.Bluetooth;

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
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

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

    //delays
    private static final int SCAN_DELAY = 3000;
    private static final int TURN_ON_INTERVAL = 100;
    private static final int TURN_ON_DELAY = 2000;

    //states
    private boolean enabled;
    private boolean scanning;
    private boolean wasScanning;

    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;
    private final SparseArray<BluetoothDevice> devices = new SparseArray<>();
    private static final int PROGRESS_MSG = 1;
    private static final int PROGRESS_DISMISS = 2;
    private static final int TOAST = 3;
    private static final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case PROGRESS_MSG:
                    progress.setMessage((String) message.obj);
                    break;
                case PROGRESS_DISMISS:
                    progress.dismiss();
                    break;
                case TOAST:
                    Toast.makeText(BaseActivity.getActivity(), (String) message.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    };

    private static ProgressDialog progress;
    private BluetoothListener listener;
    private int turn_on_try;

    //fretx specific
    private static final String DEVICE_NAME = "FretX";
    private static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    private BluetoothGattCharacteristic rx;
    private final byte[] clear = new byte[]{0};
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
        if (!enabled)
            return;
        Log.d(TAG, "start");
        if (wasScanning && !scanning) {
            wasScanning = false;
            scan();
        }
    }

    public void stop(){
        if (!enabled)
            return;
        handler.removeCallbacksAndMessages(null);
        if (scanning) {
            adapter.getBluetoothLeScanner().stopScan(scanCallback);
            wasScanning = true;
            scanning = false;
        }
        progress.dismiss();
        Log.d(TAG, "stop");
    }

    public boolean isEnabled() {
        return enabled;
    }

    /* = = = = = = = = = = = = = = = = = = = SCANNING = = = = = = = = = = = = = = = = = = = = = */
    public void scan() {
        progress = new ProgressDialog(BaseActivity.getActivity());
        progress.setIndeterminate(true);
        progress.setCancelable(false);

        handler.obtainMessage(PROGRESS_MSG, "Enabling").sendToTarget();
        progress.show();

        Log.d(TAG, "enabling...");
        if(!adapter.isEnabled()) {
            adapter.enable();
            turn_on_try = 0;
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
                    progress.dismiss();
                    if (listener != null)
                        listener.onScanFailure();
                }
            } else {
                startScan();
            }
        }
    };

    private void startScan() {
        Log.d(TAG, "scanning...");
        handler.obtainMessage(PROGRESS_MSG, "Scanning").sendToTarget();
        devices.clear();
        ScanSettings settings = new ScanSettings.Builder().build();
        ScanFilter filter = new ScanFilter.Builder().setDeviceName(DEVICE_NAME).build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(filter);
        scanning = true;
        adapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
        handler.postDelayed(endOfScan, SCAN_DELAY);
    }

    private Runnable endOfScan = new Runnable() {
        @Override
        public void run() {
            adapter.getBluetoothLeScanner().stopScan(scanCallback);
            scanning = false;
            if (devices.size() == 1) {
                handler.removeCallbacksAndMessages(null);
                connect(devices.valueAt(0));
            } else if (devices.size() > 1) {
                Log.d(TAG, "Too many devices found");
                progress.dismiss();
                if (listener != null) {
                    listener.onScanFailure();
                }
            } else {
                Log.d(TAG, "No device found");
                progress.dismiss();
                if (listener != null) {
                    listener.onScanFailure();
                }
            }
        }
    };

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();
            devices.put(device.hashCode(), device);
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

            progress.dismiss();
            if (listener != null) {
                listener.onScanFailure();
            }
        }
    };

    /* = = = = = = = = = = = = = = = = = = = CONNECTING = = = = = = = = = = = = = = = = = = = = = */
    private void connect(BluetoothDevice device) {
        Log.d(TAG, "connecting");
        handler.obtainMessage(PROGRESS_MSG, "Connecting").sendToTarget();
        gatt = device.connectGatt(BaseActivity.getActivity(), false, gattCallback);
    }

    public void disconnect() {
        if (gatt != null) {
            Log.d(TAG, "disconnecting");
            gatt.close();
            gatt = null;
        }
    }

    public boolean isConnected() {
        return !(gatt == null);
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                handler.obtainMessage(PROGRESS_DISMISS, null).sendToTarget();
                handler.obtainMessage(TOAST, "Connected").sendToTarget();
                Log.d(TAG, "discovering...");
                if (listener != null)
                    listener.onConnect();
                handler.obtainMessage(PROGRESS_MSG, "Discovering services").sendToTarget();
                gatt.discoverServices();
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "disconnected");
                if (listener != null)
                    listener.onDisconnect();
                Bluetooth.this.gatt = null;
                handler.obtainMessage(TOAST, "Connection failed").sendToTarget();
                handler.obtainMessage(PROGRESS_DISMISS, null).sendToTarget();
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "failure, disconnecting");
                gatt.close();
                Bluetooth.this.gatt = null;
                handler.obtainMessage(PROGRESS_DISMISS, null).sendToTarget();
                handler.obtainMessage(TOAST, "Connection failed").sendToTarget();
                if (listener != null)
                    listener.onFailure();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            BluetoothGattService RxService = Bluetooth.this.gatt.getService(RX_SERVICE_UUID);
            rx = RxService.getCharacteristic(RX_CHAR_UUID);
        }
    };

    /* = = = = = = = = = = = = = = = = = = = = = MATRIX = = = = = = = = = = = = = = = = = = = = = */
    public void setMatrix(Chord chord) {
        if (gatt == null || rx == null)
            return;
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(chord.toString(), chordFingerings);
        rx.setValue(bluetoothArray);
        gatt.writeCharacteristic(rx);
    }

    public void setMatrix(byte[] fingerings) {
        if (gatt == null || rx == null)
            return;
        rx.setValue(fingerings);
        gatt.writeCharacteristic(rx);
    }

    public void setMatrix(Scale scale) {
        if (gatt == null || rx == null)
            return;
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(scale.toString(), chordFingerings);
        rx.setValue(bluetoothArray);
        gatt.writeCharacteristic(rx);
    }

    public void clearMatrix() {
        if (gatt == null || rx == null)
            return;
        rx.setValue(clear);
        gatt.writeCharacteristic(rx);
    }

    /* = = = = = = = = = = = = = = = = = = = LISTENERS = = = = = = = = = = = = = = = = = = = = = */
    public void setListener(@Nullable BluetoothListener listener) {
        this.listener = listener;
    }
}
