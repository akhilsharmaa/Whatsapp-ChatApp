package com.example.classsync;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfileActivity extends AppCompatActivity {

    androidx.appcompat.widget.Toolbar toolbarOfUpdateProfile;
    EditText mNewUserName;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    androidx.appcompat.widget.Toolbar mToolbarOfViewUserProfile;
    ImageButton mBackButtonOfUpdateProfile;
    ImageView mGetNewUserImageInImage;
    Button mUpdateProfileButton;

    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;
    FirebaseStorage firebaseStorage;
    ProgressBar mProgressBarOfUpdateProfile;


    private String ImageUriToken;

    private String oldUserName;

    private Uri imagePath;
    Intent intent;

    private static int PICK_IMAGE = 123;
    String newName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        toolbarOfUpdateProfile= findViewById(R.id.toolbarOfUpdateProfile);
        mBackButtonOfUpdateProfile = findViewById(R.id.backButtonOfUpdateProfile);
        mGetNewUserImageInImage = findViewById(R.id.getNewUserImageInImageView);
        mProgressBarOfUpdateProfile = findViewById(R.id.progressBarOfUpdateProfile);

        mNewUserName = findViewById(R.id.getNewUserName);
        mUpdateProfileButton = findViewById(R.id.updateProfile_btn);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        intent = getIntent();

        setSupportActionBar(mToolbarOfViewUserProfile);

        // Back Button
        mBackButtonOfUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }});



        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());

        mUpdateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                newName = mNewUserName.getText().toString();
                if (newName.isEmpty()){
                    Toast.makeText(UpdateProfileActivity.this, "Name is Empty", Toast.LENGTH_SHORT).show();
                }else if (imagePath!=null){
                    mProgressBarOfUpdateProfile.setVisibility(View.VISIBLE);
                    UserProfile mUserProfile = new UserProfile(newName,firebaseAuth.getUid());
                    databaseReference.setValue(mUserProfile);

                    updateImageToStorage();
                    Toast.makeText(UpdateProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    mProgressBarOfUpdateProfile.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(UpdateProfileActivity.this, ChatActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    mProgressBarOfUpdateProfile.setVisibility(View.VISIBLE);
                    UserProfile mUserProfile = new UserProfile(newName,firebaseAuth.getUid());
                    databaseReference.setValue(mUserProfile);
                    updateNameOnCloudFirestore();
                    mProgressBarOfUpdateProfile.setVisibility(View.INVISIBLE);
                    Toast.makeText(UpdateProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateProfileActivity.this, ChatActivity.class);
                    startActivity(intent);
                    finish();
                }


            }});

        mGetNewUserImageInImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent,PICK_IMAGE);
            }});

        storageReference = firebaseStorage.getReference();
        storageReference.child("Images")
                .child(firebaseAuth.getUid()).child("Profile Pic")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageUriToken = uri.toString();
                Picasso.get().load(ImageUriToken).into(mGetNewUserImageInImage);
            }
        });





    }

    private void updateNameOnCloudFirestore() {


        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String , Object> userData  = new HashMap<>();
        userData.put("name", newName);
        userData.put("image",ImageUriToken);
        userData.put("uid", firebaseAuth.getUid());
        userData.put("status","Online");


        documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(UpdateProfileActivity.this, "Data on cloud firestore send success", Toast.LENGTH_SHORT).show();
            }
        });

    }




    private void updateImageToStorage() {

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
                        ImageUriToken = uri.toString();
                        Toast.makeText(UpdateProfileActivity.this, "URI get success", Toast.LENGTH_SHORT).show();


                        updateNameOnCloudFirestore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UpdateProfileActivity.this, "URI get failure", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(UpdateProfileActivity.this, "Image is Updated", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateProfileActivity.this, "Image not Updated", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            // Get image from user device
            imagePath = data.getData();
            Log.i("getImagePath Intent", imagePath.toString());
            mGetNewUserImageInImage.setImageURI(imagePath);
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