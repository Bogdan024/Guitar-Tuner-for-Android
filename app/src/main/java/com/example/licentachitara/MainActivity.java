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
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static final String RECORD_TAG = "AUDIO_RECORD";
    int channel_config = AudioFormat.CHANNEL_IN_MONO;
    int format = AudioFormat.ENCODING_PCM_16BIT;
    int samplesize = 44100;
    private int buffersize = AudioRecord.getMinBufferSize(samplesize, channel_config, format);
    private short[] audioBuffer = new short[buffersize];

    private double[] dataFromNative;

    private boolean stopListen;
    AudioRecord audioRecord;
    private Thread listener;

    private Button startRecording;
    private Button stopRecording;

    private TextView samplesTw;

    private TextView outputFrequencyTw;

    private double maxFrequency = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    static {
        System.loadLibrary("licentachitara");
    }


    public native double FFTAnalyze(double[] audioSamples, int buffersize);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startRecording = findViewById(R.id.start_recording);
        stopRecording = findViewById(R.id.stop_recording);

        samplesTw = findViewById(R.id.samplesTw);

        outputFrequencyTw = findViewById(R.id.outputFrequencyTw);

        startRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
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
                audioBuffer = null;
                dataFromNative = null;
                outputFrequencyTw.setText("");
                maxFrequency = 0;
            }
        });
    }

//    public native void calculateFrequency();

    public void setTextViewText(String text) {

    }


    private void recording() {
        Log.d(RECORD_TAG, "Recording function");

        Log.d(RECORD_TAG,"buffersize " + buffersize);

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



        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(RECORD_TAG, "error");
            return;
        }

        audioRecord.startRecording();
        Log.d(RECORD_TAG,"Started recording");

        audioBuffer = new short[buffersize];

        int audioRead = 0;

        while(stopListen == false) {
            int sample = audioRecord.read(audioBuffer, 0, buffersize);
            if (sample > 0) {
                audioRead += sample;

                Log.d(RECORD_TAG,audioRead + " AUDIOREAD LONG VAR");
                Log.d(RECORD_TAG,sample + " SAMPLE INT VAR");

                dataFromNative = new double[buffersize];



                for (int i=0;i< audioBuffer.length;i++)
                {
                    dataFromNative[i] = (double) audioBuffer[i];
                }

//                for (int j=0;j<dataFromNative.length;j++)
//                {
//                    dataFromNative[j] = (double) (0.54 - 0.46 * Math.cos(2 * Math.PI * j / (dataFromNative.length - 1)));
//                }

                double freqOutput = FFTAnalyze(dataFromNative, buffersize);

                Log.d(RECORD_TAG,freqOutput + " FFT OUTPUT");


//                for (int j=0;j<freqOutput.length;j++)
//                {
//                    if (freqOutput[j] > maxFrequency)
//                    {
//                        maxFrequency = freqOutput[j];
//                    }
//                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        outputFrequencyTw.setText(freqOutput + " Hz");
                    }
                });
            }


        }

        samplesTw.setText(audioRead + " samples");


        Log.d(RECORD_TAG, String.format("Samples read: %d", audioRead));


//        double[] fftOutput = new double[(int) audioRead * 2];
//
//        calculateFrequency(audioBuffer,buffersize,fftOutput);
//
//        double maxValue = -1;
//        int maxIndex = -1;
//        for (int i = 0; i < fftOutput.length; i++) {
//            if (fftOutput[i] > maxValue) {
//                maxValue = fftOutput[i];
//                maxIndex = i;
//            }
//        }
//        double maxFrequency = maxIndex * samplesize / audioBuffer.length;

    }


}