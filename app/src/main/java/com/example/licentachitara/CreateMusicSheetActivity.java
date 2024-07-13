package com.example.licentachitara;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CreateMusicSheetActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 1025;

    private EditText titleEt, authorEt, descriptionEt;
    private ImageView sheetMusicIv, songPictureIv;
    private Button chooseImageBtn, submitBtn, chooseSongPictureBtn;
    private Uri imageUri;
    private Uri songPictureUri;
    private int songPictureOk = 0;

    private FirebaseFirestore database;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_music_sheet);

        titleEt = findViewById(R.id.titleEt);
        authorEt = findViewById(R.id.authorEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        sheetMusicIv = findViewById(R.id.sheetMusicIv);
        chooseImageBtn = findViewById(R.id.chooseImageBtn);
        submitBtn = findViewById(R.id.submitBtn);
        songPictureIv = findViewById(R.id.songPictureIv);
        chooseSongPictureBtn = findViewById(R.id.chooseSongPictureBtn);

        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        username = intent.getStringExtra("username_key");

        chooseImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(CreateMusicSheetActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateMusicSheetActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
                } else {
                    openFileChooser();
                }
            }
        });

        chooseSongPictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(CreateMusicSheetActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateMusicSheetActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
                } else {
                    songPictureOk = 1;
                    openFileChooser();
                }
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitMusicSheet();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

//    private void requestStoragePermission() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
//            Toast.makeText(this, "Permission needed to access gallery", Toast.LENGTH_SHORT).show();
//        }
//        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChooser();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (songPictureOk == 0) {
                imageUri = data.getData();
                sheetMusicIv.setImageURI(imageUri);
            } else if (songPictureOk == 1) {
                songPictureUri = data.getData();
                songPictureIv.setImageURI(songPictureUri);
                songPictureOk = 0;
            }
        }
    }

    private void submitMusicSheet() {
        final String title = titleEt.getText().toString().trim();
        final String author = authorEt.getText().toString().trim();
        final String sheetDescription = descriptionEt.getText().toString().trim();
        final FirebaseUser user = auth.getCurrentUser();

        if (title.isEmpty() || author.isEmpty() || sheetDescription.isEmpty() || imageUri == null || songPictureUri == null) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        final String imageName = UUID.randomUUID().toString();
        final StorageReference fileReference = storageReference.child("sheetImages/" + imageName);

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            final String songPictureImageName = UUID.randomUUID().toString();
                            final StorageReference songPictureReference = storageReference.child("sheetImages/" + songPictureImageName);

                            songPictureReference.putFile(songPictureUri)
                                    .addOnSuccessListener(taskSnapshot1 -> songPictureReference.getDownloadUrl()
                                            .addOnSuccessListener(uri1 -> {
                                                String sheetMusicUrl = uri.toString();
                                                String songPictureUrl = uri1.toString();
                                                String userId = user.getUid();
                                                Date currentTime = Calendar.getInstance().getTime(); // Current date and time

                                                MusicSheet musicSheet = new MusicSheet(title, author, sheetDescription, sheetMusicUrl, songPictureUrl, currentTime, username, userId);
                                                database.collection("postsCollection").add(musicSheet)
                                                        .addOnSuccessListener(documentReference -> {
                                                            Toast.makeText(CreateMusicSheetActivity.this,
                                                                    "Music sheet created", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        })
                                                        .addOnFailureListener(e -> Toast.makeText(CreateMusicSheetActivity.this, "Error creating post", Toast.LENGTH_SHORT).show());
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(CreateMusicSheetActivity.this, "Error getting second image download URL", Toast.LENGTH_SHORT).show()))
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(CreateMusicSheetActivity.this, "Error uploading second image", Toast.LENGTH_SHORT).show();
                                        if (e instanceof StorageException) {
                                            StorageException se = (StorageException) e;
                                            if (se.getHttpResultCode() == 404) {
                                                // Handle the case where the object does not exist
                                                Toast.makeText(CreateMusicSheetActivity.this, "The specified file path for the second image does not exist", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        })
                        .addOnFailureListener(e -> Toast.makeText(CreateMusicSheetActivity.this, "Error getting first image download URL", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateMusicSheetActivity.this, "Error uploading first image", Toast.LENGTH_SHORT).show();
                    if (e instanceof StorageException) {
                        StorageException se = (StorageException) e;
                        if (se.getHttpResultCode() == 404) {
                            // Handle the case where the object does not exist
                            Toast.makeText(CreateMusicSheetActivity.this, "The specified file path for the first image does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
