package com.hector.speakbudy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechActivity extends AppCompatActivity {

    // Audio permission variable
    private static final int MY_AUDIO_PERMISSION_CODE = 100;

    // Speech recognizer
    SpeechRecognizer speechRecognizer;
    Intent speechRecognizerIntent;

    // String to store the speech
    String textSaid;

    // View elements variable
    ImageView backButton, micButton;
    TextView textToDisplay;
    LinearLayout progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        // Full screen and hiding the action bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        // Initialize speech objects
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(SpeechActivity.this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // Connection view elements to the view variables
        mappingViews();

        // backButton onClick action listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Speech recognizer listener
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {}

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matchesFound = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matchesFound != null){
                    textSaid = matchesFound.get(0);
                    textSaid = textSaid.substring(0, 1).toUpperCase() + textSaid.substring(1).toLowerCase();
                    textToDisplay.setText(textSaid);
                    progressBar.setVisibility(View.INVISIBLE);
                    textToDisplay.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        // micButton onClick listener
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAudioPermission()){
                    progressBar.setVisibility(View.VISIBLE);
                    textToDisplay.setVisibility(View.INVISIBLE);
                    textSaid = "";
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
            }
        });

    }

    // Mapping variables to the view elements
    private void mappingViews() {
        progressBar = findViewById(R.id.speech_progress);
        backButton = findViewById(R.id.speechBackButton);
        textToDisplay = findViewById(R.id.text_display);
        micButton = findViewById(R.id.mic_button);
    }

    // Checking the audio permission
    private boolean checkAudioPermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SpeechActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_AUDIO_PERMISSION_CODE);
        }else {
            return true;
        }
        return false;
    }
}