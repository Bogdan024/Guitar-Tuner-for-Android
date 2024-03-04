package com.example.licentachitara;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PermissionActivity extends AppCompatActivity {

    private static final String TAG = "PERM_TAG";
    private static final int RECORD_AUDIO_PERMISSION_CODE = 1024;

    private Button enableMicBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        enableMicBtn = findViewById(R.id.enableMicBtn);

        askForPermission();

        enableMicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeSettingsPermission();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"On resume");
        askForPermission();
    }

    private void changeSettingsPermission()
    {
        Log.d(TAG,"Change permission from settings");
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",getPackageName(),null);
        intent.setData(uri);
        startActivity(intent);
    }
    private void askForPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG,"Check for permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
        }
        else
        {
            startActivity(new Intent(PermissionActivity.this, LogInActivity.class ));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE)
        {
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Log.d(TAG,"Permission granted");
                startActivity(new Intent(PermissionActivity.this, LogInActivity.class ));
                finish();
            }
            else
            {
                Log.d(TAG,"Permision denied");
            }
        }
    }
}