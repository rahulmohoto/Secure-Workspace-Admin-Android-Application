package com.example.secure_workspace;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PopUpWindow{

    private TextView test2;
    private Button btn_1;
    private Button btn_2;
    private Date date;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    public final static String DEVICE_NAME_AVAILABLE =
            "com.example.bluetooth.le.SetupConnection";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

    //PopupWindow display method
    public void showPopupWindow(final View view, final String address, final String name) {

        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_pop_up_window, null);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 50);
        popupWindow.setElevation(4);

        //Initialize the elements of our window, install the handler
        test2 = popupView.findViewById(R.id.titleText);
        btn_2 = popupView.findViewById(R.id.messageButton);
        btn_1 = popupView.findViewById(R.id.selectButton);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Data");
        date = new Date();

        String data = name + " " + address;
        test2.setText(data);

        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String databaseKey = sdf.format(new Timestamp(date.getTime()));
                databaseReference.child(databaseKey).setValue(new Post(name,address));
                Toast.makeText(view.getContext(), "Device added to favourites", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });

        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseDatabase = FirebaseDatabase.getInstance();
                databaseReference = firebaseDatabase.getReference("Dashboard");

                DatabaseReference ref = databaseReference.child("Device_Address");
                ref.setValue(address);
                DatabaseReference ref2 = databaseReference.child("Device_Name");
                ref2.setValue(name);

                Intent intent = new Intent(DEVICE_NAME_AVAILABLE);
                view.getContext().sendBroadcast(intent);
                popupWindow.dismiss();
            }
        });

        //Handler for clicking on the inactive zone of the window
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Close the window when clicked
                popupWindow.dismiss();
                return true;
            }
        });
    }
}
