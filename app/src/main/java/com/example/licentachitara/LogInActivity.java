package com.example.licentachitara;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogInActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText loginEmail, loginPassword;
    private TextView signupRedirectText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        auth = FirebaseAuth.getInstance();
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signUpRedirectText);

        if (isSessionValid()) {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                startActivity(new Intent(LogInActivity.this, MainActivity.class));
                finish();
            }
        }
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmail.getText().toString();
                String pass = loginPassword.getText().toString();

                if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (!pass.isEmpty()) {
                        auth.signInWithEmailAndPassword(email, pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        FirebaseUser user = auth.getCurrentUser();
                                        if (user != null) {
                                            saveLoginTimestamp();
                                            Toast.makeText(LogInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(LogInActivity.this, MainActivity.class));
                                            finish();
                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LogInActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        loginPassword.setError("Password cannot be empty");
                    }

                } else if (email.isEmpty()) {
                    loginEmail.setError("Email cannot be empty");
                } else {
                    loginEmail.setError("Please enter a valid email");
                }
            }
        });


        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LogInActivity.this, SignUpActivity.class));
                finish();
            }
        });

    }

    private void saveLoginTimestamp() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginTime", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("login_time", System.currentTimeMillis());
        editor.apply();
    }

    private boolean isSessionValid() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginTime", MODE_PRIVATE);
        long loginTimestamp = sharedPreferences.getLong("login_time", 0);
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - loginTimestamp;
        return elapsedTime < (10 * 60 * 1000); // 10 minutes in milliseconds
    }
}