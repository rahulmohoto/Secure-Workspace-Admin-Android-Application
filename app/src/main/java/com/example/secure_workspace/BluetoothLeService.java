package com.example.secure_workspace;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import static com.example.secure_workspace.GattAttributes.Read;

public class BluetoothLeService extends Service {

    private Binder binder = new LocalBinder();

    private BluetoothAdapter bluetoothAdapter;
    static BluetoothGatt bluetoothGatt;
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String BLESearch_DATA_AVAILABLE =
            "com.example.bluetooth.le.BLESearch_DATA_AVAILABLE";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    public static String value;
    public static String name;

    private int connectionState;

    private String TAG = "BluetoothLEService";
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Dashboard");
        Log.e(TAG, "Binder OnBind");

        mBuilder = new NotificationCompat.Builder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        return binder;
    }

    class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            Log.e(TAG, "Got Service");
            return BluetoothLeService.this;
        }
    }

    public boolean initialize() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            // connect to the GATT server on the device
            bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
            Log.e(TAG, "Successfully connected to the device address.");
            return true;
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Device not found with provided address.  Unable to connect.");
            return false;
        }
        // connect to the GATT server on the device
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                Log.e(TAG, "BluetoothGattCallback-> STATE_CONNECTED-> onConnectionStateChange: ");
                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                // Attempts to discover services after successful connection.
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                Log.e(TAG, "BluetoothGattCallback-> STATE_DISCONNECTED-> onConnectionStateChange: ");
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Gatt Success-> onServicesDiscovered received: " + status);
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.e(TAG, "onServicesDiscovered: ");

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(
                BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                int status
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "onCharacteristicRead: Sending to Broadcast Update of Characteristics");
                broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(
                BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic
        ) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) {
            Log.e(TAG, "Bluetooth is returned null");
            return null;
        }
        Log.e(TAG, "Services Found from getSupportedGattServices");
        return bluetoothGatt.getServices();
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        Log.e(TAG, "broadcastUpdate: " + action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        Log.e("BroadCastUpdate-->", "broadcastUpdate: " + action);

        final Intent intent = new Intent(action);

        if(Read.equals(characteristic.getUuid().toString()))
        {
            value = characteristic.getStringValue(0);
            Log.w(TAG, "Value--> "+ value);

            DatabaseReference ref = databaseReference.child("Received_Data");
            ref.setValue(value);

            sendNotification();

            sendBroadcast(intent);
        }

    }

    private void sendNotification() {
        mBuilder.setSmallIcon(R.drawable.ic_launcher_background);
        mBuilder.setContentTitle(value);
        mBuilder.setContentText(name + " is sending an SOS message");
        mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        mNotificationManager.notify(0, mBuilder.build());
    }

    public static void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null) {
            Log.w("Bluetooth LE Service-->", "BluetoothGatt not initialized");
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }

    public static void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bluetoothGatt == null) {
            Log.w("CharacteristicsNotify", "BluetoothGatt not initialized");
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        close();
        return super.onUnbind(intent);
    }

    private void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    public BluetoothGatt getGatt(){
        return bluetoothGatt;
    }


}
