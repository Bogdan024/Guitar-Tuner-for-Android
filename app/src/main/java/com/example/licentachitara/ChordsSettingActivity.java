package com.example.licentachitara;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChordsSettingActivity extends AppCompatActivity {

    private LinearLayout standardTuning;
    private LinearLayout dropDTuning;
    private LinearLayout customTuningLayout;
    private LinearLayout tuningContainer;
    private ScrollView tuningScrollView;
    private Spinner customTuning1stString, customTuning2ndString, customTuning3rdString, customTuning4thString, customTuning5thString, customTuning6thString;
    private Button addCustomTuningButton, saveCustomTuningButton, exitButton;
    private EditText tuningNameEt;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String[] guitarNotes = {"E2", "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2", "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3", "C4", "C#4", "D4", "D#4", "E4"};
    private List<TuningMode> tuningsList = new ArrayList<>();
    private List<String> tuningIdsList = new ArrayList<>();
    private String[] tuningNames = {"Standard Tuning", "Drop D Tuning"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chords_setting);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        tuningScrollView = findViewById(R.id.tuningScrollView);
        tuningContainer = findViewById(R.id.tuningContainer);
        standardTuning = findViewById(R.id.standardTuning);
        dropDTuning = findViewById(R.id.dropDTuning);
        customTuningLayout = findViewById(R.id.customTuningLayout);
        tuningNameEt = findViewById(R.id.tuningNameEt);
        customTuning1stString = findViewById(R.id.customTuning1stString);
        customTuning2ndString = findViewById(R.id.customTuning2ndString);
        customTuning3rdString = findViewById(R.id.customTuning3rdString);
        customTuning4thString = findViewById(R.id.customTuning4thString);
        customTuning5thString = findViewById(R.id.customTuning5thString);
        customTuning6thString = findViewById(R.id.customTuning6thString);
        addCustomTuningButton = findViewById(R.id.addCustomTuningButton);
        saveCustomTuningButton = findViewById(R.id.saveCustomTuningButton);
        exitButton = findViewById(R.id.exitButton);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, guitarNotes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        customTuning1stString.setAdapter(adapter);
        customTuning2ndString.setAdapter(adapter);
        customTuning3rdString.setAdapter(adapter);
        customTuning4thString.setAdapter(adapter);
        customTuning5thString.setAdapter(adapter);
        customTuning6thString.setAdapter(adapter);

        if (currentUser != null) {
            String userId = currentUser.getUid();
            loadTuningsFromFirestore(userId);
        }


        addCustomTuningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customTuningLayout.setVisibility(View.VISIBLE);
                addCustomTuningButton.setVisibility(View.INVISIBLE);
                addCustomTuningButton.setEnabled(false);
                tuningScrollView.setVisibility(View.GONE);
            }
        });

//        saveCustomTuningButton.setEnabled(false);
//        tuningNameEt.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                saveCustomTuningButton.setEnabled(!s.toString().trim().isEmpty());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        saveCustomTuningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tuningNameEt.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(),"You need to name your new tune",Toast.LENGTH_SHORT).show();
                }
                else {
                    String tuningName = tuningNameEt.getText().toString().trim();
                    String firstString = customTuning1stString.getSelectedItem().toString();
                    String secondString = customTuning2ndString.getSelectedItem().toString();
                    String thirdString = customTuning3rdString.getSelectedItem().toString();
                    String fourthString = customTuning4thString.getSelectedItem().toString();
                    String fifthString = customTuning5thString.getSelectedItem().toString();
                    String sixthString = customTuning6thString.getSelectedItem().toString();

                    if (currentUser != null) {
                        saveTuningToFirestore(new TuningMode(tuningName, firstString, secondString, thirdString, fourthString, fifthString, sixthString));
                    }
                    customTuningLayout.setVisibility(View.GONE);
                    addCustomTuningButton.setVisibility(View.VISIBLE);
                    addCustomTuningButton.setEnabled(true);
                    tuningScrollView.setVisibility(View.VISIBLE);
                }
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customTuningLayout.setVisibility(View.GONE);
                addCustomTuningButton.setVisibility(View.VISIBLE);
                addCustomTuningButton.setEnabled(true);
                tuningScrollView.setVisibility(View.VISIBLE);
            }
        });

    }

    private void saveTuningToFirestore(TuningMode tuningMode) {
        Map<String, String> tuningData = new HashMap<>();
        tuningData.put("userId", currentUser.getUid());
        tuningData.put("name", tuningMode.getName());
        tuningData.put("1stString", tuningMode.getFirstString());
        tuningData.put("2ndString", tuningMode.getSecondString());
        tuningData.put("3rdString", tuningMode.getThirdString());
        tuningData.put("4thString", tuningMode.getFourthString());
        tuningData.put("5thString", tuningMode.getFifthString());
        tuningData.put("6thString", tuningMode.getSixthString());

        db.collection("stringsCollection").add(tuningData)
                .addOnSuccessListener(documentReference -> Toast.makeText(this, "Tuning saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving tuning", Toast.LENGTH_SHORT).show());
    }

    private void loadTuningsFromFirestore(String userId) {

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("stringsCollection").whereEqualTo("userId", currentUser.getUid()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(ChordsSettingActivity.this, "Error getting tunings", Toast.LENGTH_SHORT).show();
                    return;
                }

                tuningsList.clear();
                tuningIdsList.clear();
                tuningContainer.removeAllViews();
                addTuningToLayout(new TuningMode("Standard Tuning", "E4", "B3", "G3", "D3", "A2", "E2"));
                addTuningToLayout(new TuningMode("Drop D Tuning", "E4", "B3", "G3", "D3", "A2", "D2"));



                for (DocumentSnapshot document : snapshots) {
                    String name = document.getString("name");
                    String firstString = document.getString("1stString");
                    String secondString = document.getString("2ndString");
                    String thirdString = document.getString("3rdString");
                    String fourthString = document.getString("4thString");
                    String fifthString = document.getString("5thString");
                    String sixthString = document.getString("6thString");
                    TuningMode customTuning = new TuningMode(name, firstString, secondString, thirdString, fourthString, fifthString, sixthString);
                    tuningsList.add(customTuning);
                    tuningIdsList.add(document.getId());
                    addTuningToLayout(customTuning);
                }
            }
        });
    }

    private void addTuningToLayout(TuningMode tuningMode) {
        LinearLayout tuningLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 16, 0, 16);
        tuningLayout.setLayoutParams(layoutParams);
        tuningLayout.setOrientation(LinearLayout.VERTICAL);
        tuningLayout.setPadding(32, 32, 32, 32);
        tuningLayout.setBackgroundResource(R.drawable.chords_scrollview_layout);
        tuningLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTuningToMainActivity(tuningMode);
            }
        });

        List<String> tuningToNotDelete = Arrays.asList(tuningNames);
        if (!tuningToNotDelete.contains(tuningMode.getName())) {
            tuningLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showDeleteConfirmationDialog(tuningIdsList.get(tuningsList.indexOf(tuningMode)));
                    return true;
                }
            });
        }

        TextView tuningTextView = new TextView(this);
        tuningTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tuningTextView.setText(tuningMode.toString());
        tuningTextView.setTextSize(18);
        tuningTextView.setPadding(8, 8, 8, 8);

        tuningLayout.addView(tuningTextView);
        tuningContainer.addView(tuningLayout);
    }

    private void showDeleteConfirmationDialog(String documentId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete tuning mode")
                .setMessage("Are you sure you want to delete this tuning?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTuningFromFirestore(documentId);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteTuningFromFirestore(String documentId) {
        db.collection("stringsCollection").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tuning deleted", Toast.LENGTH_SHORT).show();
                    loadTuningsFromFirestore(currentUser.getUid());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting tuning", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ChordsSettingActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveTuningToPreferences(TuningMode tuningMode) {
        SharedPreferences sharedPreferences = getSharedPreferences("TuningPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("1st_STRING", tuningMode.getFirstString());
        editor.putString("2nd_STRING", tuningMode.getSecondString());
        editor.putString("3rd_STRING", tuningMode.getThirdString());
        editor.putString("4th_STRING", tuningMode.getFourthString());
        editor.putString("5th_STRING", tuningMode.getFifthString());
        editor.putString("6th_STRING", tuningMode.getSixthString());
        editor.apply();
    }

    private void sendTuningToMainActivity(TuningMode tuningMode) {

        saveTuningToPreferences(tuningMode);

        Intent intent = new Intent(ChordsSettingActivity.this, MainActivity.class);
        intent.putExtra("T_NAME", tuningMode.getName());
        intent.putExtra("1st_STRING", tuningMode.getFirstString());
        intent.putExtra("2nd_STRING", tuningMode.getSecondString());
        intent.putExtra("3rd_STRING", tuningMode.getThirdString());
        intent.putExtra("4th_STRING", tuningMode.getFourthString());
        intent.putExtra("5th_STRING", tuningMode.getFifthString());
        intent.putExtra("6th_STRING", tuningMode.getSixthString());
        startActivity(intent);
        Toast.makeText(this,tuningMode.getName() + " selected as new tuning mode", Toast.LENGTH_SHORT).show();
        finish();
    }
}