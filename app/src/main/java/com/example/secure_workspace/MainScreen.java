package com.example.secure_workspace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.secure_workspace.BluetoothLeService.*;
import static com.example.secure_workspace.PopUpWindow.DEVICE_NAME_AVAILABLE;

public class MainScreen extends AppCompatActivity {

    private Button btn_1;
    private Button btn_3;
    private TextView text_3;
    private Toolbar my_toolbar;
    private Button btn_2;
    private ListView list_1;
    private TextView text_5;
    private TextView text_8;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private HashMap<String, String> values;
    private BluetoothLeService bluetoothService;
    private String TAG = "Main Activity";
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean isSearching = false;

    // Total 10 devices can be able to join
    private String selectedAddress;
    private String selectedName;
    private String connectedFlag;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;

    private String LIST_NAME = "NAME";
    private String LIST_UUID = "UUID";

    private boolean connected;
    private boolean firstStart = true;

    private BluetoothLeScan bleScan;
    // Device Name and Address
    private String[] name;
    private String[] address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        btn_1 = findViewById(R.id.btn_1);
        text_3 = findViewById(R.id.text_3);
        btn_2 = findViewById(R.id.btn_2);
        list_1 = findViewById(R.id.list_1);
        btn_3 = findViewById(R.id.btn_3);
        text_5 = findViewById(R.id.text_5);
        text_8 = findViewById(R.id.text_8);

        my_toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(my_toolbar);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Dashboard");
        gdata();

        bleScan = new BluetoothLeScan(bluetoothLeScanner, MainScreen.this);

        // Connect Button
        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gattServiceIntent = new Intent(MainScreen.this, BluetoothLeService.class);;
                if(connected == false) {
                    final boolean value = bindService(gattServiceIntent, serviceConnection, 0);
                    if (value == true) {
                        startService(gattServiceIntent);
                        Toast.makeText(getApplicationContext(), "Bind Successful", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    stopService(gattServiceIntent);
                }
            }
        });

        // Power Bluetooth Button
        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBtPermission();
                bleScan.setBtConnection();
                updateConnectionState("Bt Powered On");
            }
        });

        // Bluetooth Search Button
        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bleScan.setBtConnection() && bleScan.getBluetoothAdapter().isEnabled()) {
                    isSearching = true;
                    updateConnectionState("Searching..");
                    bleScan.scanLeDevice();
                }
            }
        });

        // List after Scanning
        list_1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainScreen.this, "You Clicked at " +address[+ position], Toast.LENGTH_SHORT).show();
                PopUpWindow popUpClass = new PopUpWindow();
                popUpClass.showPopupWindow(view, address[+ position], name[+ position]);
            }
        });
    }

    private void populateListView() {
        isSearching = false;
        updateConnectionState("Search Finished");

        values = bleScan.getHashMap();

        address = new String[values.size()];
        name = new String[values.size()];

        int index = 0;
        for (Map.Entry<String, String> e : values.entrySet()) {
            address[index] = e.getKey();
            name[index] = e.getValue();
            index++;
        }

        Log.e(TAG, "onClick: "+values);

        CustomListView adapter = new CustomListView(MainScreen.this, address, name);
        list_1.setAdapter(adapter);
    }

    private void setBtPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainScreen.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainScreen.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        101);
            }
            Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }



    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.e(TAG, "Service Connected");
            if (bluetoothService != null) {
                if (!bluetoothService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }
                // perform device connection
                Log.e(TAG, "Able to initialize Bluetooth");
                bluetoothService.connect(selectedAddress);

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothService = null;
            Log.e(TAG, "Service Disconnected");
            resetConnectedFlag();
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "On Gatt Update Received");
            final String action = intent.getAction();
            if (ACTION_GATT_CONNECTED.equals(action)) {
                Log.e(TAG, "Gatt Server Connected");
                updateConnectionState("Connected");
                SetConnectedFlag();
            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.e(TAG, "Gatt Server Disconnected");
                updateConnectionState("DisConnected");
            } else if (ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Log.e(TAG, "Device Discovery is finished, get supported Gatt Services");
                displayGattServices(bluetoothService.getSupportedGattServices());
            } else if (ACTION_DATA_AVAILABLE.equals(action)){
                Log.e(TAG, "Action Data Available");
                displayData(BluetoothLeService.value);
            } else if (BLESearch_DATA_AVAILABLE.equals(action)){
                    Log.e(TAG, "Blutooth Search Completed");
                    populateListView();
            } else if (DEVICE_NAME_AVAILABLE.equals(action)){
                Log.e(TAG, "Selected Device");
                setup();
            }
        }
    };

    private boolean checkConnectedFlag(){
        if(connectedFlag.equals("True")){
            btn_1.setEnabled(true);
            btn_2.setEnabled(false);
            btn_3.setEnabled(false);
            btn_1.setText("DisConnect");
            text_3.setText("Connected");
            return true;
        } else{
            btn_1.setEnabled(false);
            text_3.setText("Disconnected");
            return false;
        }
    }

    private void SetConnectedFlag() {
        btn_1.setEnabled(true);
        btn_2.setEnabled(false);
        btn_3.setEnabled(false);
        btn_1.setText("DisConnect");
        text_3.setText("Connected");

        connected = true;
        DatabaseReference ref = databaseReference.child("Connected");
        ref.setValue("True");
    }

    private void resetConnectedFlag(){
        if(!isSearching) {
            connected = false;
            btn_2.setEnabled(true);
            btn_1.setText("Connect");
            text_3.setText("Disconnected");
            DatabaseReference ref = databaseReference.child("Device_Address");
            ref.setValue("NULL");
            DatabaseReference ref2 = databaseReference.child("Device_Name");
            ref2.setValue("NULL");
            DatabaseReference ref3 = databaseReference.child("Connected");
            ref3.setValue("False");
            DatabaseReference ref4 = databaseReference.child("Received_Data");
            ref4.setValue("NULL");
        }
    }

    private void displayData(final String st) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text_8.setText(st);
            }
        });
    }

    private void updateConnectionState(final String st) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text_3.setText(st);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothService != null) {
            final boolean result = bluetoothService.connect(selectedAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BLESearch_DATA_AVAILABLE);
        intentFilter.addAction(DEVICE_NAME_AVAILABLE);
        return intentFilter;
    }


    private void displayGattServices(List<BluetoothGattService> gattServices) {
        Log.e(TAG, "Display Gatt Services");
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().
                getString(R.string.unknown_service);
        String unknownCharaString = getResources().
                getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics =
                new ArrayList<>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, GattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);

                Log.d(TAG, "displayGattServices--> "+gattCharacteristic.getUuid()+" "+gattCharacteristic.getProperties());

                if(gattCharacteristic.getUuid().toString().equals("6e400003-b5a3-f393-e0a9-e50e24dcca9e")){
                    Log.e("Match Found-->", "displayGattServices: "+ GattAttributes.lookup(uuid, unknownCharaString) +" "+ uuid);
                    readCharacteristic(gattCharacteristic);
                    setCharacteristicNotification(gattCharacteristic, true);
                }

//                gattCharacteristicGroupData.add(currentCharaData);

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    public void setup(){
        Log.e(TAG, "setup: "+selectedName);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn_1.setEnabled(true);
                text_5.setText(selectedName);
            }
        });
    }

    private void gdata() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Device_Info device = snapshot.getValue(Device_Info.class);
                selectedName = device.Device_Name;
                BluetoothLeService.name = selectedName;
                selectedAddress = device.Device_Address;
                connectedFlag = device.Connected;
                text_5.setText(selectedName);
                text_8.setText(device.Received_Data);
                Log.e(TAG, "onDataChange: "+device.Device_Name+" "+device.Device_Address+" "+device.Received_Data+" "+device.Connected);

                if(firstStart){
                    connected = checkConnectedFlag();
                    firstStart = !firstStart;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainScreen.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}