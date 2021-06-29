package com.example.classsync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    EditText mMobileNumber;
    Button sendOtpBtn;
    CountryCodePicker ccp;
    String countryCode;
    String phoneNumber;

    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;
    String codeSent;

    @Override
    protected void onStart() {
        Log.d("OnStartMainActivity", "Started");
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        user = FirebaseAuth.getInstance().getCurrentUser();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendOtpBtn = findViewById(R.id.sendOtpButton);
        mMobileNumber = findViewById(R.id.getPhoneNumber);
        ccp = findViewById(R.id.countryCodePicker);
        ccp.registerCarrierNumberEditText(mMobileNumber);
        progressBar = findViewById(R.id.progress_circular);

        sendOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OtpAuthenticationActivity.class);
                intent.putExtra("mobileNumber",ccp.getFullNumberWithPlus().replace(" ", ""));
                startActivity(intent);
            }
        });

    }


}