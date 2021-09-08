package com.example.secure_workspace;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    private Button button_start;
    private Button button_favourites;
    private Button button_about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_start = findViewById(R.id.btn_start);
        button_favourites = findViewById(R.id.btn_favourites);
        button_about = findViewById(R.id.btn_about);

        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_main_screen();
            }
        });

        button_favourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_favourite_screen();
            }
        });

        button_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_about_screen();
            }
        });

    }

    private void start_about_screen() {
        Intent intent = new Intent(MainActivity.this, About.class);
        startActivity(intent);
    }

    private void start_favourite_screen() {
        Intent intent = new Intent(MainActivity.this, MainFavouritesScreen.class);
        startActivity(intent);
    }

    private void start_main_screen() {
        Intent intent = new Intent(MainActivity.this, MainScreen.class);
        startActivity(intent);
    }
}

