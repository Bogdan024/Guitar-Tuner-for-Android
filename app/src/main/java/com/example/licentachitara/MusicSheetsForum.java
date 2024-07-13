package com.example.licentachitara;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicSheetsForum extends AppCompatActivity{

    private FirebaseFirestore database;
    private FirebaseAuth auth;
    private List<MusicSheet> musicSheetsList;
    private MusicSheetAdapter musicSheetAdapter;
    private RecyclerView recyclerView;
    private Button addMusicSheetBtn;
    private LinearLayout forumLayout;
    private TextView usernameTv;
    private TextView forumTitleTv;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_sheets_forum);

        forumLayout = findViewById(R.id.forumLayout);
        usernameTv = findViewById(R.id.usernameTv);
        forumTitleTv = findViewById(R.id.forumTitleTv);
        Typeface typeface = ResourcesCompat.getFont(this,R.font.architects_daughter);
        forumTitleTv.setTypeface(typeface);

        searchBar = findViewById(R.id.searchBar);
        addMusicSheetBtn = findViewById(R.id.addPostBtn);

        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            checkEmailVerification();
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        musicSheetsList = new ArrayList<>();
        musicSheetAdapter = new MusicSheetAdapter(this, musicSheetsList);
        recyclerView.setAdapter(musicSheetAdapter);


        DividerItemDecoration verticalDecoration = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
        Drawable verticalDivider = ContextCompat.getDrawable(this, R.drawable.vertical_divider);
        verticalDecoration.setDrawable(verticalDivider);

        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(this, R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);

//        recyclerView.addItemDecoration(horizontalDecoration);
//        recyclerView.addItemDecoration(verticalDecoration);

        recyclerView.setItemAnimator(new DefaultItemAnimator() {
            {
                setAddDuration(250);
                setRemoveDuration(250);
            }
        });

        addMusicSheetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicSheetsForum.this, CreateMusicSheetActivity.class);
                intent.putExtra("username_key", usernameTv.getText().toString());
                startActivity(intent);
            }
        });

        loadMusicSheets("");

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadMusicSheets(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadMusicSheets(String searchText) {
        database.collection("postsCollection").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }

                musicSheetsList.clear();
                if (value != null) {
                    for (DocumentSnapshot document : value.getDocuments()) {
                        MusicSheet musicSheet = document.toObject(MusicSheet.class);
                        if (musicSheet != null) {
                            if (searchText.isEmpty() ||
                                    musicSheet.getTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                                    musicSheet.getSheetDescription().toLowerCase().contains(searchText.toLowerCase())) {
                                musicSheetsList.add(musicSheet);
                            }
                        }
                    }
                }
                musicSheetAdapter.notifyDataSetChanged();
            }
        });
    }

    private void checkEmailVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (user.isEmailVerified()) {
                        // Email is verified, proceed with app flow
                        checkUserInFirestore(user);
                    } else {
                        // Email not verified, show dialog and redirect
                        showEmailNotVerifiedDialog();
                    }
                }
            });
        } else {
            // User not logged in, redirect to login or main activity
            redirectToMainActivity();
        }
    }

    private void showEmailNotVerifiedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Your email is not verified. Please check your email for the verification link.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                redirectToMainActivity();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(MusicSheetsForum.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkUserInFirestore(final FirebaseUser user) {
        database.collection("usersCollection").document(user.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String username = document.getString("username");
                                if (username == null || username.isEmpty()) {
                                    promptForUsername(user);
                                } else {
                                    usernameTv.setText(username);
                                }
                            } else {
                                createUserInFirestore(user);
                            }
                        }
                    }
                });
    }

    private void promptForUsername(final FirebaseUser user) {
        // Create a dialog to input username
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Set Username")
                .setMessage("Insert your username")
                .setView(input)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String username = input.getText().toString().trim();
                        if (!username.isEmpty()) {
                            database.collection("usersCollection").document(user.getUid())
                                    .update("username", username);
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void createUserInFirestore(final FirebaseUser user) {
        String userEmail = user.getEmail();
        Map<String, String> userAndMail = new HashMap<>();
        userAndMail.put("email", user.getEmail());
        database.collection("usersCollection").document(user.getUid()).set(userAndMail, SetOptions.mergeFields("email"))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            promptForUsername(user);
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MusicSheetsForum.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}