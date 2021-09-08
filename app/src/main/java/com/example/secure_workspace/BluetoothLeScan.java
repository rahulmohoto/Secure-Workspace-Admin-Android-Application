package com.example.secure_workspace;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

import static com.example.secure_workspace.BluetoothLeService.BLESearch_DATA_AVAILABLE;

public class BluetoothLeScan {
    private String TAG = "BluetoothBleScan";

    private boolean scanning;
    private Handler handler;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothAdapter bluetoothAdapter;

    private Context context;
    private HashMap<String, String> map;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    BluetoothLeScan(BluetoothLeScanner BluetoothLeScanner, Context context){
        handler = new Handler();
        this.bluetoothLeScanner = BluetoothLeScanner;
        this.context = context;
        map = new HashMap<>();
    }

    public BluetoothAdapter getBluetoothAdapter(){
        return bluetoothAdapter;
    }

    public HashMap<String, String> getHashMap(){
        return map;
    }

    public boolean setBtConnection(){
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context.getApplicationContext(), "Bluetooth Feature Not Enabled", Toast.LENGTH_SHORT).show();
            return false;
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBtIntent);
        }
        return true;
    }

    void scanLeDevice() {
        if (!this.scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BluetoothLeScan.this.scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    Log.e(TAG, "run: "+"finished");
                    broadcastUpdate(BLESearch_DATA_AVAILABLE);
                }
            }, SCAN_PERIOD);

            this.scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            this.scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private void broadcastUpdate(String action) {
        final Intent intent = new Intent(action);
        Log.e(TAG, "broadcastUpdate: " + action);
        context.sendBroadcast(intent);
    }

    // BLE SCAN CALLBACK
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            map.put(result.getDevice().getAddress(), result.getDevice().getName());
            Log.d(TAG, "ScanCallback: " + result.getDevice().getAddress() + " Device Name: " + result.getDevice().getName());
        }
    };

}
