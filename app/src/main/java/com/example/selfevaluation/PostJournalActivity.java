package com.example.selfevaluation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.selfevaluation.databinding.ActivityPostJournalBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

import model.Journal;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE = 1;
    private ActivityPostJournalBinding binding;

    private String currentUserId;
    private String CurrentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //connection to firesStore
    private final FirebaseFirestore db =  FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private final CollectionReference collectionReference =  db.collection("Journal");
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostJournalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference =FirebaseStorage.getInstance().getReference();


        binding.postJournalSaveButton.setOnClickListener(this);
        binding.postJournalCameraButton.setOnClickListener(this);


        if (JournalApi.getInstance() != null){
            currentUserId = JournalApi.getInstance().getUserId();
            CurrentUserName = JournalApi.getInstance().getUsername();

            binding.postJournalUserName.setText(CurrentUserName);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                if (user!= null){

                }else {

                }

            }
        };





    }



    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.postJournalSaveButton:
                //save journal
                saveJournal();

                break;

            case R.id.postJournalCameraButton:
                // get image from gallery
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;
        }

    }


        private void saveJournal() {
            final String title = binding.postJournalTittle.getText().toString().trim();
            final String thoughts = binding.writeJournalEditText.getText().toString().trim();
            binding.postJournalProgressbar.setVisibility(View.VISIBLE);

            if (!TextUtils.isEmpty(title) &&
                    !TextUtils.isEmpty(thoughts)
                    && imageUri != null) {

                final StorageReference filepath = storageReference //.../journal_images/our_image.jpeg
                        .child("journal_images")
                        .child("my_image_" + Timestamp.now().getSeconds()); // my_image_887474737

                filepath.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                          binding.postJournalProgressbar.setVisibility(View.INVISIBLE);

                                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        String imageUrl = uri.toString();
                                        //Todo: create a Journal Object - model
                                        Journal journal = new Journal();
                                        journal.setTitle(title);
                                        journal.setThought(thoughts);
                                        journal.setImageUrl(imageUrl);
                                        journal.setTimeAdded(new Timestamp(new Date()));
                                        journal.setUserName(CurrentUserName);
                                        journal.setUserId(currentUserId);

                                        //Todo:invoke our collectionReference
                                        collectionReference.add(journal)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {

                                                        binding.postJournalProgressbar.setVisibility(View.INVISIBLE);
                                                        startActivity(new Intent(PostJournalActivity.this,
                                                                JournalListActivity.class));
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("TAG", "onFailure: " + e.getMessage());
                                                        Toast.makeText(PostJournalActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                        //Todo: and save a Journal instance.

                                    }
                                });


                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                binding.postJournalProgressbar.setVisibility(View.INVISIBLE);

                                Toast.makeText(PostJournalActivity.this, "something gone wrong", Toast.LENGTH_SHORT).show();

                            }
                        });


            } else {

                binding.postJournalProgressbar.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "please  fill every field", Toast.LENGTH_SHORT).show();

            }
        }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData(); // we have the actual path to the image
                binding.postJournalImageView.setImageURI(imageUri);//show image

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        user= firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

}
