package com.example.selfevaluation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.selfevaluation.databinding.ActivityLogInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import util.JournalApi;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityLogInBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;



    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("users");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        firebaseAuth = FirebaseAuth.getInstance();



        binding.logInButton.setOnClickListener(this);
        binding.createAccountButton.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logInButton:

                    String email = binding.logInEmail.getText().toString().trim();
                    String password = binding.logInPassword.getText().toString().trim();

                    logIntoAccount(email,password);
                    break;
            case R.id.createAccountButton:
                startActivity(new Intent(LogInActivity.this,CreateAccountActivity.class));
        }
    }

    private void logIntoAccount(String email, String password) {

        if(!TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password)){

            binding.loginProgressBar.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                     FirebaseUser user = firebaseAuth.getCurrentUser();
                    assert user != null;
                    String currentUserId = user.getUid();
                    collectionReference.whereEqualTo("userId",currentUserId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value,
                                                    @Nullable FirebaseFirestoreException error) {

                                    if(error!=null){
                                        Toast.makeText(LogInActivity.this,"error"+error.getMessage(),Toast.LENGTH_SHORT)
                                                .show();

                                    }
                                    else{
//                                        Toast.makeText(LogInActivity.this,"error"+error.getMessage(),Toast.LENGTH_SHORT)
//                                                .show();
                                    }
                                    assert value != null;
                                    if(!value.isEmpty()){
                                        binding.loginProgressBar.setVisibility(View.INVISIBLE);
                                        for(QueryDocumentSnapshot snapshot : value){
                                            JournalApi journalApi = JournalApi.getInstance();
                                            journalApi.setUsername(snapshot.getString("username"));
                                            journalApi.setUserId(snapshot.getString("userId"));

                                            // Go to list activity

                                            startActivity(new Intent(LogInActivity.this,JournalListActivity.class));

                                        }

                                    }
                                    else {
                                        binding.loginProgressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(LogInActivity.this,"no user found",Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }
                            });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            binding.loginProgressBar.setVisibility(View.INVISIBLE);

                            Log.d("tag" , e.getMessage());

                            Toast.makeText(LogInActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });



        }else {
            Toast.makeText(LogInActivity.this,"please enter email and password",Toast.LENGTH_SHORT)
                    .show();
        }

    }
}