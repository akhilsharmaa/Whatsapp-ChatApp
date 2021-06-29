package com.example.classsync;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SetProfileActivity extends AppCompatActivity {

    private CardView mGetUserImage_cardView;
    private ImageView mGetUserImageInImageView;
    private static int PICK_IMAGE = 123;
    private Uri imagePath;

    private EditText mGetUserName;
    private Button mSaveProfile;

    private FirebaseAuth firebaseAuth;
    private String name;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private DatabaseReference mDatabase;

    private String ImageUriAccessToken;
    private FirebaseFirestore firebaseFirestore;

    private ProgressBar mProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mGetUserName = findViewById(R.id.getUserName);
        mGetUserImageInImageView = findViewById(R.id.getUserImage);
        mProgressbar = findViewById(R.id.progressbar_circular);
        mSaveProfile = findViewById(R.id.save_profileBtn);
        mGetUserImage_cardView = findViewById(R.id.userImage_card_view);

        Log.i("UID", "User UID: " +firebaseAuth.getUid().toString());  // getting Firebase user UID

        mGetUserImage_cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
               startActivityForResult(intent,PICK_IMAGE);
            }
        });

        mSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = mGetUserName.getText().toString();
                if (name.isEmpty()) {
                    Toast.makeText(SetProfileActivity.this, "Enter your name", Toast.LENGTH_SHORT).show();
                }else if (imagePath== null){
                    Toast.makeText(SetProfileActivity.this, "Please set a IMAGE", Toast.LENGTH_SHORT).show();
                }else {
                    mProgressbar.setVisibility(View.VISIBLE);
                    sendDataForNewUser();
                    mProgressbar.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(SetProfileActivity.this,ChatActivity.class);
                    intent.putExtra("nameOfUser",mGetUserName.toString());
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void sendDataForNewUser() {
        sendDataToRealTimeDatabase();
    }

    private void sendDataToRealTimeDatabase() {

        UserProfile mUserProfile = new UserProfile(name, firebaseAuth.getUid());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(firebaseAuth.getUid()).setValue(mUserProfile);


        sendImageToStorage();
    }

    // Send image to Firebase Cloud Store
    private void sendImageToStorage() {
        StorageReference imageRef = storageReference.child("Images")
                .child(firebaseAuth.getUid()).child("Profile Pic");


        /** Image Compression
            this will compress the image size but not the quality*/
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
        }catch (IOException e){
            // If error occurs
            Log.i("Image Compression",  "" + e.getMessage());
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte [] data = byteArrayOutputStream.toByteArray();


        /** Putting image to the storage */
        UploadTask uploadTask = imageRef.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageUriAccessToken = uri.toString();
                        Toast.makeText(SetProfileActivity.this, "URI get success", Toast.LENGTH_SHORT).show();
                        sendDataForCloudFireStore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetProfileActivity.this, "URI get failure", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(SetProfileActivity.this, "Image is UPLOADED", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SetProfileActivity.this, "Image not uploaded", Toast.LENGTH_SHORT).show();
            }
        });;


    }

    // called in sendImageToStorage
    private void sendDataForCloudFireStore() {

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String , Object> userData  = new HashMap<>();
        userData.put("name", name);
        userData.put("image",ImageUriAccessToken);
        userData.put("uid", firebaseAuth.getUid());
        userData.put("status","Online");

        documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(SetProfileActivity.this, "Data on cloud firestore send success", Toast.LENGTH_SHORT).show();
            }
        });

        Log.i("USER DATA",userData.toString());


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            // Get image from user device
            imagePath = data.getData();
            Log.i("getImagePath Intent", imagePath.toString());

            mGetUserImageInImageView.setImageURI(imagePath);
        }



        super.onActivityResult(requestCode, resultCode, data);
    }

    /** To Show whether the user is online or offLine*/

    @Override
    protected void onStart() {
        super.onStart();

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());

        documentReference.update("status","Online").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "You are Online now", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());

        documentReference.update("status","Offline").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "You are Offline now", Toast.LENGTH_SHORT).show();
            }
        });

    }
}