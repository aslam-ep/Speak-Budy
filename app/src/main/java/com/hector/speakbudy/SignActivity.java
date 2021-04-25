package com.hector.speakbudy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

@SuppressLint("RestrictedApi")
public class SignActivity extends AppCompatActivity implements LifecycleOwner {

    ImageView backButton, flashButton, soundButton;
    PreviewView previewView;
    FloatingActionButton videoRecordButton;
    LinearLayout progressBar;
    TextView loadingText;

    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    Camera camera;
    VideoCapture videoCapture;

    File file;

    boolean flash = false;
    boolean sound = true;
    int MY_FILE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        backButton = findViewById(R.id.objectBackButton2);
        flashButton = findViewById(R.id.flashButton2);
        soundButton = findViewById(R.id.soundButton2);
        previewView = findViewById(R.id.previewView2);
        videoRecordButton = findViewById(R.id.recordButton);
        progressBar = findViewById(R.id.sign_progress);
        loadingText = findViewById(R.id.loadingText);

        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Video.mp4");

        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                startCamera(cameraProvider);
            } catch (ExecutionException | InterruptedException e){
                Log.d("CameraError", e.toString());
            }
        }, ContextCompat.getMainExecutor(this));

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
                camera.getCameraControl().enableTorch(flash);
            }
        });

        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sound){
                    soundButton.setImageResource(R.drawable.ic_baseline_volume_off_24);
                    sound = false;
                }else {
                    soundButton.setImageResource(R.drawable.ic_baseline_volume_up_24);
                    sound = true;
                }
            }
        });

        videoRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkFilePermission()) {
                    videoRecordButton.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);

                    VideoCapture.OutputFileOptions outputFileOptions = new VideoCapture.OutputFileOptions.Builder(file).build();
                    videoCapture.startRecording(outputFileOptions, ContextCompat.getMainExecutor(getApplicationContext()), new VideoCapture.OnVideoSavedCallback() {
                        @Override
                        public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                            videoRecordButton.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);

                        }

                        @Override
                        public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                            videoRecordButton.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "Can't Capture", Toast.LENGTH_SHORT).show();
                            Log.d("HEY NO", "Video File : " + cause);
                        }
                    });

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            videoCapture.stopRecording();
                            Log.d("HEY Stopping", "Video File : Stopped");
                        }
                    }, 3000);
                }
            }
        });
    }

    private void startCamera(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        videoCapture = new VideoCapture.Builder()
                .setTargetRotation(previewView.getDisplay().getRotation())
                .setVideoFrameRate(24)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
    }

    boolean checkFilePermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SignActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_FILE_PERMISSION_CODE);
        }else {
            return true;
        }
        return false;
    }
}