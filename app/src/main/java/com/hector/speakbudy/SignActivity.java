package com.hector.speakbudy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class SignActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;

    ImageView backButton, flashButton, soundButton;

    boolean flash = false;
    boolean sound = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        backButton = findViewById(R.id.objectBackButton2);
        flashButton = findViewById(R.id.flashButton2);
        soundButton = findViewById(R.id.soundButton2);

        if(checkCameraHardware(SignActivity.this)){
            // Create an instance of Camera
            mCamera = getCameraInstance();

            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview2);
            preview.addView(mPreview);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!flash) {
                    flashButton.setImageResource(R.drawable.ic_baseline_flash_on_24);
                    flash = true;
                }
                else {
                    flashButton.setImageResource(R.drawable.ic_baseline_flash_off_24);
                    flash = false;
                }
            }
        });

        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sound){
                    soundButton.setImageResource(R.drawable.ic_baseline_volume_off_24);
                    sound = false;
                }else{
                    soundButton.setImageResource(R.drawable.ic_baseline_volume_up_24);
                    sound = true;
                }
            }
        });
    }
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            // set Camera parameters
            Camera.Parameters params = c.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            c.setParameters(params);
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d("Camera", e.toString());
        }
        return c; // returns null if camera is unavailable
    }
}