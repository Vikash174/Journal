package com.example.selfevaluation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.selfevaluation.databinding.ActivityCreateAccountBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {
    private ActivityCreateAccountBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;


    //FireStore connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final CollectionReference collectionReference = db.collection("users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        firebaseAuth = FirebaseAuth.getInstance();

        binding.createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(binding.createAccountUserName.getText().toString())
                        && !TextUtils.isEmpty(binding.createAccountEmail.getText().toString())
                        && !TextUtils.isEmpty(binding.createAccountPassword.getText().toString())) {
                    String email = binding.createAccountEmail.getText().toString().trim();
                    String password = binding.createAccountPassword.getText().toString().trim();
                    String username = binding.createAccountUserName.getText().toString().trim();

                    createUserEmailAccount(email, password, username);

                } else {
                    Toast.makeText(CreateAccountActivity.this,
                            "Empty Fields Not Allowed",
                            Toast.LENGTH_LONG)
                            .show();
                }

            }
        });
    }

    private void createUserEmailAccount(String email, String password, String username) {
        if (!TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password)
                && !TextUtils.isEmpty(username)) {

            binding.createAccountProgressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //we take user to AddJournalActivity
                                currentUser = firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                final String currentUserId = currentUser.getUid();

                                //Create a user Map so we can create a user in the User collection
                                Map<String, String> userObj = new HashMap<>();
                                userObj.put("userId", currentUserId);
                                userObj.put("username", username);
                                userObj.put("userPassword",password);
                                userObj.put("userEmail", email);

                                //save to our fireStore database
                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (Objects.requireNonNull(task.getResult()).exists()) {
                                                                    binding.createAccountProgressBar.setVisibility(View.INVISIBLE);
                                                                    String name = task.getResult()
                                                                            .getString("username");

                                                                    JournalApi journalApi = JournalApi.getInstance(); //Global API
                                                                    journalApi.setUserId(currentUserId);
                                                                    journalApi.setUsername(name);

                                                                    Intent intent = new Intent(CreateAccountActivity.this,
                                                                            PostJournalActivity.class);
                                                                    intent.putExtra("username", name);
                                                                    intent.putExtra("userId", currentUserId);
                                                                    startActivity(intent);


                                                                } else {

                                                                    binding.createAccountProgressBar.setVisibility(View.INVISIBLE);
                                                                }

                                                            }
                                                        });

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(CreateAccountActivity.this, "collection failed" + e.getMessage(), Toast.LENGTH_SHORT).show();


                                            }
                                        });


                            } else {
                                binding.createAccountProgressBar.setVisibility(View.INVISIBLE);
                            }


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(CreateAccountActivity.this, "failed " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } else {
            Toast.makeText(CreateAccountActivity.this,
                    "Empty Fields Not Allowed",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }
}