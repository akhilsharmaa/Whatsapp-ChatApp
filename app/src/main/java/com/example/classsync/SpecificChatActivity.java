package com.example.classsync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SpecificChatActivity extends AppCompatActivity {

    private EditText mGetMessage;
    private ImageView backButton;
    private ImageView mImageOfSpecificUser;
    private TextView  mNameOfSpecificUserUser;
    private ImageView mSendButton;
    private androidx.appcompat.widget.Toolbar toolbarOfSpecificChat;
    private TextView mNameOfSpecificUser;
    private CardView cardViewOfSendMessageButton;

    private String enteredMessage;

    Intent intent;
    String mReceiverName, mSenderName, mReceiverUid, mSenderUid;

    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    String senderRoom , receiverRoom;

    RecyclerView mMessageRecyclerView ;

    String mReceiverImageUri;

    String currentTime;

    Calendar calendar;
    SimpleDateFormat simpleDateFormat;


    MessagesAdapter messagesAdapter;
    ArrayList<Messages> messagesArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_chat);

        backButton = findViewById(R.id.backButtonOfSpecificProfile);
        mGetMessage = findViewById(R.id.getMessage);
        cardViewOfSendMessageButton = findViewById(R.id.cardViewOfSendMessageButton);
        toolbarOfSpecificChat = findViewById(R.id.toolbarOfSpecificActivity);
        mNameOfSpecificUser = findViewById(R.id.nameOfSpecificUser);
        mImageOfSpecificUser = findViewById(R.id.imageViewOfSpecificUserInToolbar);

        intent = getIntent();


        // Recycler View messages
        messagesArrayList  = new ArrayList<>();
        mMessageRecyclerView = findViewById(R.id.recyclerViewOfSpecificChat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(linearLayoutManager);
        messagesAdapter = new MessagesAdapter(SpecificChatActivity.this,messagesArrayList);
        mMessageRecyclerView.setAdapter(messagesAdapter);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm a");



        mSenderUid = firebaseAuth.getUid();

        // Getting the intent data from previous activity.
        mReceiverUid = getIntent().getStringExtra("ReceiverUid");
        mReceiverName = getIntent().getStringExtra("ReceiverName");
        mReceiverImageUri = getIntent().getStringExtra("ReceiverImageUri");


        /** Room's Name for chatting */
        senderRoom = mSenderUid+mReceiverUid;
        receiverRoom = mReceiverUid+ mSenderUid;

        // Setting the 2nd person''s image and name
        mNameOfSpecificUser.setText(mReceiverName);
        if (!mReceiverImageUri.isEmpty()){
            Picasso.get().load(mReceiverImageUri).into(mImageOfSpecificUser);
        }


        /** TODO: Send And Receive Message Full Function to the user */
        cardViewOfSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enteredMessage = mGetMessage.getText().toString();

                // User Message is not empty....
                if (!enteredMessage.isEmpty()){
                    Date date = new Date();


                    currentTime = simpleDateFormat.format(calendar.getTime());
                    Log.i("Date and Time" , "date - "+ date.getTime() + " Current time - " + currentTime);

                    Messages messages = new Messages(enteredMessage,firebaseAuth.getUid(),date.getTime(),currentTime);

                    /** Pushing the data to the firebase RealTime data base*/
                    firebaseDatabase.getReference()
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .push().setValue(messages)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            firebaseDatabase.getReference()
                                    .child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .push().setValue(messages)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                   Log.i("Message", "Message Sent");
                                }
                            });
                        }});


                }else{ // If EditText is Empty
                    Toast.makeText(SpecificChatActivity.this, "Enter message first", Toast.LENGTH_SHORT).show();
                }
                mGetMessage.setText(null);
            }
        });


        Log.i("SenderRoom",senderRoom);
        DatabaseReference databaseReference = firebaseDatabase.getReference()
                .child("chats")
                .child(senderRoom)
                .child("messages");

        messagesAdapter = new MessagesAdapter(SpecificChatActivity.this, messagesArrayList);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();

                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Messages messages = snapshot1.getValue(Messages.class);
                    messagesArrayList.add(messages);
                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /* TODO: Opening the 2nd user's Detailed Profile*/
        setSupportActionBar(toolbarOfSpecificChat);
        toolbarOfSpecificChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Open Detailed Profile", Toast.LENGTH_SHORT).show();

            }});


        /** Back button to close the activity */
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }});
    }
}