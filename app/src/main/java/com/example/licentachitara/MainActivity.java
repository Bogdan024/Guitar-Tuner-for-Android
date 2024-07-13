package com.example.licentachitara;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.NoiseSuppressor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private Button btnSheetsComponent;
    private Button btnTunerComponent;
    private Button btnChordSetComponent;

    private static DecimalFormat NUMBER_WITH_DECIMALS = new DecimalFormat("0.##");
    private static final String RECORD_TAG = "AUDIO_RECORD";
    int channel_config = AudioFormat.CHANNEL_IN_MONO;
    int format = AudioFormat.ENCODING_PCM_16BIT;
    int samplesize = 44100;
    private int buffersize = AudioRecord.getMinBufferSize(samplesize, channel_config, format);
    private short[] audioBuffer = new short[buffersize];
    private double[] dataFromNative;
    private double freqOutput;
    private int negative = 0;
    private boolean stopListen;
    private AudioRecord audioRecord = null;
    private NoiseSuppressor noiseSuppressor = null;
    private Thread listener;
    private Button startRecording;
    private Button stopRecording;
    private TextView samplesTw;
    private TextView outputFrequencyTw;
    private TextView noteTw;
    private TextView inTuneTw;

    private static final HashMap<String, Double> notesAllFreqMap= new HashMap<>();
    static {
        notesAllFreqMap.put("C", 32.70);
        notesAllFreqMap.put("C#", 34.65);
        notesAllFreqMap.put("D", 36.71);
        notesAllFreqMap.put("D#", 38.89);
        notesAllFreqMap.put("E", 41.20);
        notesAllFreqMap.put("F", 43.65);
        notesAllFreqMap.put("F#", 46.25);
        notesAllFreqMap.put("G", 49.00);
        notesAllFreqMap.put("G#", 51.91);
        notesAllFreqMap.put("A", 55.00);
        notesAllFreqMap.put("A#", 58.27);
        notesAllFreqMap.put("B", 61.74);
    }
    private static final HashMap<Integer, String> indexAllNotesMap = new HashMap<>();
    static {
        indexAllNotesMap.put(0, "C");
        indexAllNotesMap.put(1, "C#");
        indexAllNotesMap.put(2, "D");
        indexAllNotesMap.put(3, "D#");
        indexAllNotesMap.put(4, "E");
        indexAllNotesMap.put(5, "F");
        indexAllNotesMap.put(6, "F#");
        indexAllNotesMap.put(7, "G");
        indexAllNotesMap.put(8, "G#");
        indexAllNotesMap.put(9, "A");
        indexAllNotesMap.put(10, "A#");
        indexAllNotesMap.put(11, "B");
    }
    private HashMap<String, Double> stringsToTuneMap;
    private int differenceInCents = 0;
    private String noteToOutput;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Handler checkForTuneHandler = new Handler(Looper.getMainLooper());
    private boolean stringInTune = false;
    private TextView firstStringTv, secondStringTv, thirdStringTv, fourthStringTv, fifthStringTv, sixthStringTv;
    private List<TextView> notesTvList;
    private static final double noteA4 = 440.0;

    static {
        System.loadLibrary("licentachitara");
    }


    private native double FFTAnalyze(double[] audioSamples, int buffersize);
    private native void destroyPlanFFT();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSheetsComponent = findViewById(R.id.btn_sheets_component);
        btnTunerComponent = findViewById(R.id.btn_tuner_component);
        btnChordSetComponent = findViewById(R.id.btn_chordsSet_component);

        startRecording = findViewById(R.id.start_recording);
        stopRecording = findViewById(R.id.stop_recording);

        samplesTw = findViewById(R.id.samplesTw);
        inTuneTw = findViewById(R.id.inTuneTv);

        outputFrequencyTw = findViewById(R.id.outputFrequencyTw);
        noteTw = findViewById(R.id.noteTw);

        firstStringTv = findViewById(R.id.firstStringTv);
        secondStringTv = findViewById(R.id.secondStringTv);
        thirdStringTv = findViewById(R.id.thirdStringTv);
        fourthStringTv = findViewById(R.id.fourthStringTv);
        fifthStringTv = findViewById(R.id.fifthStringTv);
        sixthStringTv = findViewById(R.id.sixthStringTv);

        notesTvList = new ArrayList<>();
        notesTvList.add(firstStringTv);
        notesTvList.add(secondStringTv);
        notesTvList.add(thirdStringTv);
        notesTvList.add(fourthStringTv);
        notesTvList.add(fifthStringTv);
        notesTvList.add(sixthStringTv);

        loadTuningFromPreferences();


        stringsToTuneMap = new HashMap<>();
        for (TextView textView : notesTvList) {
            if (textView.getText().toString().length() > 2) {
                String tvToString = textView.getText().toString().trim();
                String note = tvToString.substring(0,tvToString.length() - 1);
                int octave = Character.getNumericValue(tvToString.charAt(2));
                double stringFrequency = notesAllFreqMap.get(note) * Math.pow(2,octave - 1);
                stringsToTuneMap.put(tvToString, stringFrequency);
            } else {
                String tvToString = textView.getText().toString().trim();
                String note = Character.toString(tvToString.charAt(0));
                int octave = Character.getNumericValue(tvToString.charAt(1));
                double stringFrequency = notesAllFreqMap.get(note) * Math.pow(2,octave - 1);
                stringsToTuneMap.put(tvToString, stringFrequency);
            }
        }

        for (Map.Entry<String,Double> map : stringsToTuneMap.entrySet()) {
            Log.d("MAP VALUES", map.getKey() + " " + map.getValue());
        }


        btnSheetsComponent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MusicSheetsForum.class));
                finish();
            }
        });

        btnChordSetComponent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ChordsSettingActivity.class));
                finish();
            }
        });

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
                startRecording.setEnabled(false);
            }
        });

        stopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (audioRecord != null) {
                        stopListen = true;
                        audioRecord.stop();
                        audioRecord.release();
                        audioBuffer = null;
                        dataFromNative = null;
                        audioRecord = null;
                    }
                    outputFrequencyTw.setText("Play a string on your guitar");

                    if (noiseSuppressor != null) {
                        noiseSuppressor.release();
                        noiseSuppressor = null;
                    }
                    destroyPlanFFT();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Recording already stopped",Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    Toast.makeText(getApplicationContext(), "Recording already stopped",Toast.LENGTH_SHORT).show();
                }
                startRecording.setEnabled(true);
            }
        });


    }

//    public native void calculateFrequency();

    public void setTextViewText(String text) {

    }


    private void recording() {
        Log.d(RECORD_TAG, "Recording function");

        Log.d(RECORD_TAG, "buffersize " + buffersize);

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

        if (NoiseSuppressor.isAvailable()) {
            noiseSuppressor = NoiseSuppressor.create(audioRecord.getAudioSessionId());
            if (noiseSuppressor != null) {
                noiseSuppressor.setEnabled(true);
            }


            audioRecord.startRecording();
            Log.d(RECORD_TAG, "Started recording");

            audioBuffer = new short[16384];


            double[] noiseProfile = new double[16384];
            boolean noiseProfileInitialized = false;

            int audioRead = 0;


            while (stopListen == false) {
                int sample = audioRecord.read(audioBuffer, 0, 16384);
                if (sample > 0) {
                    audioRead += sample;

                    Log.d(RECORD_TAG, audioRead + " AUDIOREAD LONG VAR");
                    Log.d(RECORD_TAG, sample + " SAMPLE INT VAR");

                    dataFromNative = new double[16384];


                    for (int i = 0; i < audioBuffer.length; i++) {
                        dataFromNative[i] = (double) (audioBuffer[i] / 32768.0f);
                    }


                    for (int j=0;j<dataFromNative.length;j++)
                    {
                        dataFromNative[j] *= (0.54 - 0.46 * Math.cos(2 * Math.PI * j / (dataFromNative.length - 1)));
                    }



                    if (audioRead >= 16384) {
                        Log.d(RECORD_TAG, "enough samples " + dataFromNative[16383]);
                        freqOutput = FFTAnalyze(dataFromNative, 16384);
                    }


                    Log.d(RECORD_TAG, freqOutput + " FFT OUTPUT");

                    noteToOutput = "";
                    differenceInCents = 0;

                    if (freqOutput > 0) {
                        negative = 0;
                        double referenceFrequency;
                        double noteMIDInumber = 69 + 12 * Math.log(freqOutput / noteA4) / Math.log(2);
                        int roundedMIDInr = (int) Math.round(noteMIDInumber);
                        int octave = (roundedMIDInr / 12) - 1;
                        int noteIndex = roundedMIDInr % 12;
                        String forOutput = indexAllNotesMap.get(noteIndex);
                        String noteAndOctave = forOutput + octave;
                        Log.d("NOTE AND OCTAVE", "NOTE AND OCTAVE " + noteAndOctave);
                        if (stringsToTuneMap.containsKey(noteAndOctave)) {
                            referenceFrequency = notesAllFreqMap.get(forOutput) * Math.pow(2, octave - 1);
                            Log.d("REFERENCE FREQUENCY", "REF FREQ " + referenceFrequency);
                            noteToOutput = noteAndOctave;
                            differenceInCents = Math.abs((int) Math.round(1200 * Math.log(freqOutput / referenceFrequency) / Math.log(2)));
                        } else {
                            double maxDifference = Double.MAX_VALUE;
                            String closestString = "";
                            negative = 0;
                            for (Map.Entry<String,Double> entry : stringsToTuneMap.entrySet()) {
                                double differenceFromEntry = Math.abs(1200 * Math.log(freqOutput / entry.getValue()) / Math.log(2));
                                Log.d("diference from entry", "DIFF from entry " + differenceFromEntry+ " " + "ENTRY VALUE " + entry.getValue());
                                if (maxDifference > differenceFromEntry && differenceFromEntry < 210) {
                                    if (entry.getValue() > freqOutput) {
                                        negative = 1;
                                        maxDifference = differenceFromEntry;
                                    } else {
                                        negative = 0;
                                        maxDifference = differenceFromEntry;
                                    }
                                    Log.d("MAX DFF", "DIFF IN CENTS " + maxDifference);
                                    closestString = entry.getKey();
                                }
                            }
                            if (maxDifference != Double.MAX_VALUE) {
                                differenceInCents = Math.abs((int) maxDifference);
                                Log.d("MAX DFF", "DIFF IN CENTS " + differenceInCents);
                            }
                            Log.d("DIFERENCE IN CENTS", "DIFF IN CENTS " + differenceInCents);
                            noteToOutput = closestString;
                            Log.d("NOTE TO OUTPUT", "NOTE TO OUTPUT " + noteToOutput);
                        }

                        Log.d("DIFERENCE IN CENTS AFTER IF/ELSE", "DIFF IN CENTS AFTE IF/ELSE " + differenceInCents);
                    }

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (freqOutput > 0) {
                                outputFrequencyTw.setText("Frequency of the sound " + NUMBER_WITH_DECIMALS.format(freqOutput) + " Hz");
                                noteTw.setText("The note you played: " + noteToOutput);
                                if (differenceInCents == 0) {
                                    samplesTw.setText(String.valueOf(differenceInCents));
                                } else if (negative == 1){
                                    samplesTw.setText("-" + differenceInCents);
                                } else if (negative == 0) {
                                    samplesTw.setText("+" + differenceInCents);
                                }

                                updateTuningState(-differenceInCents);
                            } else {
                                outputFrequencyTw.setText("Play a string on your guitar");
                                samplesTw.setText("");

                            }
//                            noteTw.setText("The note you played: " + noteToOutput);
//                            if (differenceInCents == 0) {
//                                samplesTw.setText(String.valueOf(differenceInCents));
//                            } else if (negative == 1){
//                                samplesTw.setText("-" + differenceInCents);
//                            } else if (negative == 0) {
//                                samplesTw.setText("+" + differenceInCents);
//                            }
//
//                            updateTextView(-differenceInCents);
                            updateTextViewState();

                        }
                    },250);
                }


            }

            Log.d(RECORD_TAG, String.format("Samples read: %d", audioRead));

            audioRead = 0;
        }
    }

    private void updateTextViewState() {
        for(TextView textView : notesTvList) {
            if (textView.getText().toString().trim().equals(noteToOutput)) {
                textView.setActivated(true);
            } else {
                textView.setActivated(false);
            }
        }
    }

    private void updateTuningState(int value) {

        if (value < -10) {
            inTuneTw.setText("Too Low");
        } else if (value > 10) {
            inTuneTw.setText("Too High");
        }

        // Update background color and position based on value
        if (value >= -10 && value <= 10) {
            samplesTw.setText("");
            samplesTw.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_check_mark));

            if (!stringInTune) {
                stringInTune = true;
                checkForTuneHandler.postDelayed(checkRunnable, 2000);
            }

        } else {
            samplesTw.setBackground(ContextCompat.getDrawable(this, R.drawable.cents_indicator));
            stringInTune = false;
        }
    }

    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            if (stringInTune) {
                inTuneTw.setText("In Tune");
            }
        }
    };

    private void loadTuningFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("TuningPreferences", Context.MODE_PRIVATE);
        String first = sharedPreferences.getString("1st_STRING", "E4");
        String second = sharedPreferences.getString("2nd_STRING", "B3");
        String third = sharedPreferences.getString("3rd_STRING", "G3");
        String fourth = sharedPreferences.getString("4th_STRING", "D3");
        String fifth = sharedPreferences.getString("5th_STRING", "A2");
        String sixth = sharedPreferences.getString("6th_STRING", "E2");

        firstStringTv.setText(first);
        secondStringTv.setText(second);
        thirdStringTv.setText(third);
        fourthStringTv.setText(fourth);
        fifthStringTv.setText(fifth);
        sixthStringTv.setText(sixth);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
            stopListen = true;
            destroyPlanFFT();
        }
    }
}