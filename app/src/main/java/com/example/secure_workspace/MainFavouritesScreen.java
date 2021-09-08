package com.example.secure_workspace;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainFavouritesScreen extends AppCompatActivity {

    private ListView list_2;
    private Toolbar toolbar;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private String[] address;
    private String[] name;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_favourites_screen_layout);

        list_2 = findViewById(R.id.list_2);
        toolbar = findViewById(R.id.device_toolbar);
        setSupportActionBar(toolbar);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Data");
        gdata();
    }

    private void gdata() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                address = new String[(int) snapshot.getChildrenCount()];
                name = new String[(int) snapshot.getChildrenCount()];
                int i = 0;
                for(DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    name[i] = post.name;
                    address[i] = post.address;
                    Log.d("Firebase--> ", post.name + " " +post.address);
                    i++;
                }

                start_favourites_list();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainFavouritesScreen.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void start_favourites_list(){
        FavouritesList adapter = new FavouritesList(MainFavouritesScreen.this, address, name);
        list_2.setAdapter(adapter);
    }


}
