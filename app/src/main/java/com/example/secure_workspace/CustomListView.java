package com.example.secure_workspace;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class CustomListView extends ArrayAdapter<String> {

    private Activity context;
    private String[] key;
    private String[] value;


    CustomListView(Activity context, String[] key, String[] value) {
        super(context, R.layout.activity_list_view, key);

        this.context = context;
        this.key = key;
        this.value = value;

    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.activity_list_view, null, true);

        TextView device_name = (TextView) rowView.findViewById(R.id.list_view_text);
        TextView address = (TextView) rowView.findViewById(R.id.list_view_text2);

        device_name.setText(key[position]);
        address.setText(value[position]);
        return rowView;
    }

}
