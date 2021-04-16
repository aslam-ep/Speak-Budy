package com.hector.speakbudy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class ObjectActivity extends AppCompatActivity {

    private ObjectDetector objectDetector;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;

    private Camera camera;

    PreviewView previewView;
    ImageView backButton, flashButton, soundButton;
    TextView objectIdentified;
    RelativeLayout parentLayout;

    TextToSpeech textToSpeech;

    boolean flash = false;
    boolean sound = true;
    HashMap<String, Rect> objects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e){
                Log.d("CameraError", e.toString());
            }
        }, ContextCompat.getMainExecutor(this));

        LocalModel localModel = new LocalModel.Builder()
                .setAssetFilePath("object_detection.tflite")
                .build();

        CustomObjectDetectorOptions customObjectDetectorOptions = new CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .setClassificationConfidenceThreshold(0.6f)
                .build();

        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);

        previewView = findViewById(R.id.previewView);
        backButton = findViewById(R.id.objectBackButton);
        flashButton = findViewById(R.id.flashButton);
        objectIdentified = findViewById(R.id.objectsResult);
        soundButton = findViewById(R.id.soundButton);
        parentLayout = findViewById(R.id.parentLayout);

        objects = new HashMap<>();

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

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
                }else{
                    soundButton.setImageResource(R.drawable.ic_baseline_volume_up_24);
                    sound = true;
                }
            }
        });
    }

    private void bindPreview(ProcessCameraProvider cameraProvider){

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1200, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {


            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();

                Image image1 = image.getImage();

                if (image1 != null){
                    InputImage processImage = InputImage.fromMediaImage(image1, rotationDegrees);

                    objectDetector.process(processImage)
                            .addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
                                @Override
                                public void onSuccess(List<DetectedObject> detectedObjects) {

                                    while (parentLayout.getChildCount() > 6){
                                        objectIdentified.setText("");
                                        parentLayout.removeViewAt(6);
                                    }

                                    if(detectedObjects.size() == 0)
                                        objects.clear();

                                    for (DetectedObject detectedObject: detectedObjects){
                                        try{
                                            String label = detectedObject.getLabels().get(0).getText();
                                            Rect rect = scaleBoundingBox(detectedObject.getBoundingBox(), image1);
                                            objectIdentified.setText(objectIdentified.getText() + label + ", ");

                                            if (sound && !objects.containsKey(label)){
                                                objects.put(label, rect);
                                                textToSpeech.speak(label, TextToSpeech.QUEUE_ADD, null);
                                            }

                                            DrawRectangle element = new DrawRectangle(getApplicationContext(), rect, label);
                                            parentLayout.addView(element);
                                        }catch (Exception e){

                                        }
                                    }

                                    image.close();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("ObjectDetectionError", e.toString());
                                    image.close();
                                }
                            });
                }
            }
        });
        camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
    }

    private Rect scaleBoundingBox(Rect rect, Image image){
        float scaleY = previewView.getHeight() / image.getHeight();
        float scaleX = previewView.getWidth() / image.getWidth();
        float scale = Math.max(scaleY, scaleX);
        Size scaledSize = new Size((int) Math.ceil(image.getWidth() * scale), (int) Math.ceil(image.getHeight() * scale));

        float offsetX = previewView.getWidth() - scaledSize.getWidth() / 2;
        float offsetY = previewView.getHeight() - scaledSize.getHeight() / 2;

        rect.left = (int) (rect.left * scale + offsetX);
        rect.top = (int) (rect.top * scale + offsetY);
        rect.right = (int) (rect.right * scale + offsetX);
        rect.bottom = (int) (rect.bottom * scale + offsetY);

        return rect;
    }
}