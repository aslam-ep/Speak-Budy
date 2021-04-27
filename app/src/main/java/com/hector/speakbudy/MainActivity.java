package com.hector.speakbudy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    boolean doubleBackToExitPressedOnce = false;

    // View Element Variables
    ImageView object, sign, speech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // App full screen and hiding the action bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        // Connect the view elements
        object = findViewById(R.id.objects);
        sign = findViewById(R.id.sign);
        speech = findViewById(R.id.speech);

        // Object icon onclick listener
        object.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()) {
                    startActivity(new Intent(MainActivity.this, ObjectActivity.class));
                }
            }
        });

        // Sign icon onclick listener
        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()){
                    startActivity(new Intent(MainActivity.this, SignActivity.class));
                }
            }
        });

        // Speech icon onclick listener
        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SpeechActivity.class));
            }
        });
    }

    // Camera permission check
    boolean checkCameraPermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        }else {
            return true;
        }
        return false;
    }

    // Function for double press back button to close the app
    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
        }

        this.doubleBackToExitPressedOnce = true;
        Snackbar.make(findViewById(android.R.id.content), "Press Back Again", Snackbar.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}