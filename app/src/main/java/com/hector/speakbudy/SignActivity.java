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
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.hector.speakbudy.API.RetrofitAPI;
import com.hector.speakbudy.API.RetrofitClient;
import com.hector.speakbudy.DataModels.ResponseDataModel;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("RestrictedApi")
public class SignActivity extends AppCompatActivity implements LifecycleOwner {

    // Variables for view elements
    ImageView backButton, flashButton, soundButton, videoRecordButton;
    PreviewView previewView;
    LinearLayout progressBar;
    TextView loadingText, signResult;

    // Camerax Variables
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    Camera camera;
    VideoCapture videoCapture;

    // File variable for storing the captured video
    File file;

    // Variables that needed
    boolean flash = false;
    boolean sound = true;
    int MY_FILE_PERMISSION_CODE = 101;
    int WORD_COUNT = 0;

    // Text to speech convert - variable
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        // Setting the screen as full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        // Connecting the variable and view elements using the id
        backButton = findViewById(R.id.objectBackButton2);
        flashButton = findViewById(R.id.flashButton2);
        soundButton = findViewById(R.id.soundButton2);
        previewView = findViewById(R.id.previewView2);
        videoRecordButton = findViewById(R.id.recordButton);
        progressBar = findViewById(R.id.sign_progress);
        loadingText = findViewById(R.id.loadingText);
        signResult = findViewById(R.id.signResult);


        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        // file variable initialize
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Video.mp4");

        // CameraProvider continues listen variable initialize
        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);

        // Listener add
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
                    flashButton.setImageResource(R.drawable.flash_on);
                    flash = true;
                }
                else {
                    flashButton.setImageResource(R.drawable.flash_off);
                    flash = false;
                }
                camera.getCameraControl().enableTorch(flash);
            }
        });

        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sound){
                    soundButton.setImageResource(R.drawable.sound_off);
                    sound = false;
                }else {
                    soundButton.setImageResource(R.drawable.sound_on);
                    sound = true;
                }
            }
        });

        // Capturing a 3 second video
        videoRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkFilePermission()) {
                    loadingText.setText("Capturing...");
                    videoRecordButton.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);

                    VideoCapture.OutputFileOptions outputFileOptions = new VideoCapture.OutputFileOptions.Builder(file).build();

                    videoCapture.startRecording(outputFileOptions, ContextCompat.getMainExecutor(getApplicationContext()), new VideoCapture.OnVideoSavedCallback() {
                        @Override
                        public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                            loadingText.setText("Processing...");
                            runPredictionOnVideo(file);
                        }

                        @Override
                        public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                            videoRecordButton.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                            Snackbar.make(findViewById(android.R.id.content), "Can't Capture", Snackbar.LENGTH_SHORT).show();
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

    void runPredictionOnVideo(File file){

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("video/*"),
                file
        );

        MultipartBody.Part part = MultipartBody.Part.createFormData("video", file.getName(), requestBody);

        RequestBody someData = RequestBody.create(MediaType.parse("text/plain"), "some");

        RetrofitAPI retrofitAPI = RetrofitClient.getInstance().create(RetrofitAPI.class);

        Call<ResponseDataModel> call = retrofitAPI.uploadVideo(part, someData);

        call.enqueue(new Callback<ResponseDataModel>() {
            @Override
            public void onResponse(Call<ResponseDataModel> call, Response<ResponseDataModel> response) {
                if (WORD_COUNT == 0){
                    signResult.setText(response.body().result);
                    WORD_COUNT++;
                }else {
                    signResult.setText(signResult.getText() + " " + response.body().result);
                    WORD_COUNT = (WORD_COUNT + 1) % 20;
                }

                if (sound) {
                    textToSpeech.speak(response.body().result, TextToSpeech.QUEUE_ADD, null);
                }
                
                videoRecordButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<ResponseDataModel> call, Throwable t) {
                Log.d("TAGGER", t.toString());
                Snackbar.make(findViewById(android.R.id.content), "Check Your Connection!", Snackbar.LENGTH_SHORT).show();
                videoRecordButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    // binding the view to the camera
    private void startCamera(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        videoCapture = new VideoCapture.Builder()
                .setTargetRotation(previewView.getDisplay().getRotation())
                .setVideoFrameRate(60)
                .build();

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