package com.example.secure_workspace;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FavouritesList extends ArrayAdapter<String> {

    private Activity context;
    private String[] name;
    private String[] address;

    FavouritesList(Activity context, String[] name, String[] address) {
        super(context, R.layout.favourites_list, address);

        this.context = context;
        this.name = name;
        this.address = address;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.favourites_list, null, true);

        TextView device_name = rowView.findViewById(R.id.list_view_text);
        TextView device_address = rowView.findViewById(R.id.list_view_text2);

        device_name.setText(name[position]);
        device_address.setText(address[position]);

        return rowView;
    }
}
