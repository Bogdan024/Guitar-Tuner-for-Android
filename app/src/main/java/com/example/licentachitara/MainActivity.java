package com.example.licentachitara;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static final String RECORD_TAG = "AUDIO_RECORD";
    int channel_config = AudioFormat.CHANNEL_IN_MONO;
    int format = AudioFormat.ENCODING_PCM_16BIT;
    int samplesize = 44100;
    private int buffersize = AudioRecord.getMinBufferSize(samplesize, channel_config, format);
    private short[] audioBuffer = new short[buffersize];

    private boolean stopListen;
    AudioRecord audioRecord;
    private Thread listener;

    private Button startRecording;
    private Button stopRecording;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startRecording = findViewById(R.id.start_recording);
        stopRecording = findViewById(R.id.stop_recording);

        startRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        stopListen = false;
                        recording();
                    }
                });
                listener.start();
            }
        });

        stopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopListen = true;
                audioRecord.stop();
                audioRecord.release();
            }
        });
    }

    private void recording() {
        Log.d(RECORD_TAG, "Recording function");

        int buffersize = AudioRecord.getMinBufferSize(samplesize, channel_config, format);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, samplesize, channel_config, format, buffersize);

        short[] audioBuffer = new short[buffersize];


        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(RECORD_TAG, "error");
            return;
        }

        audioRecord.startRecording();
        Log.d(RECORD_TAG,"Started recording");

        long audioRead = 0;

        while(stopListen == false) {
            int sample = audioRecord.read(audioBuffer, 0, audioBuffer.length);
            audioRead += sample;
        }


        Log.d(RECORD_TAG, String.format("Samples read: %d", audioRead));
    }

}