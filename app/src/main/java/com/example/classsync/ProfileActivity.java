package com.example.classsync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private EditText mViewUserName;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private TextView mMoveToUpdateProfile;
    private androidx.appcompat.widget.Toolbar mToolbarOfViewUserProfile;
    private ImageButton mBackButton;
    private ImageView mViewUserImageInImage;

    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;

    private String ImageUriToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mViewUserImageInImage = findViewById(R.id.viewUserImage);
        mViewUserName = findViewById(R.id.viewUserName);
        mBackButton  = findViewById(R.id.backButtonOfViewProfile);
        mToolbarOfViewUserProfile  = findViewById(R.id.toolbarOfViewProfile);
        mMoveToUpdateProfile  = findViewById(R.id.mMoveToUpdateProfile);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        setSupportActionBar(mToolbarOfViewUserProfile);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        storageReference= firebaseStorage.getReference();

        // Fetching from fireStore and and Setting the profile image
        storageReference.child("Images")
                .child(firebaseAuth.getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageUriToken = uri.toString();
                Picasso.get().load(uri).into(mViewUserImageInImage);
            }
        });

        //
        DatabaseReference databaseReference = firebaseDatabase.getReference("Users/"+ firebaseAuth.getUid());
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserProfile userProfile = snapshot.getValue(UserProfile.class);
                    Log.i("Database base Reference", userProfile.getUsername());

                    if (userProfile!= null){
                        mViewUserName.setText(userProfile.getUsername()); }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to Load", Toast.LENGTH_SHORT).show();
                }
            });

        mMoveToUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SetProfileActivity.class);
                intent.putExtra("nameOfUser",mViewUserName.getText().toString());
                startActivity(intent);

            }
        });
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