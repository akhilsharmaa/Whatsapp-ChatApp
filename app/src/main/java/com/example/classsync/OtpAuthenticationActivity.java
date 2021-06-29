package com.example.classsync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpAuthenticationActivity extends AppCompatActivity {

    TextView changeNumber;
    EditText mGetOtp;
    Button verify_btn;

    String phoneNumber;
    String enteredOtp;
    String otpId;

    FirebaseAuth firebaseAuth;
    ProgressBar mProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_authentication);

        changeNumber = findViewById(R.id.changeNumber);
        verify_btn = findViewById(R.id.verify_btn);
        mGetOtp = findViewById(R.id.getOtp);

        firebaseAuth = FirebaseAuth.getInstance();
        phoneNumber = getIntent().getStringExtra("mobileNumber").toString();

        changeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OtpAuthenticationActivity.this, MainActivity.class));
                finish();
            }
        });
        
        
        initiateOtp();
//        Credential

        verify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String otp = mGetOtp.getText().toString() ;

                if (otp.isEmpty()){
                    Toast.makeText(OtpAuthenticationActivity.this, "Please enter the Otp", Toast.LENGTH_SHORT).show();
                }else if (otp.length()!=6){
                    Toast.makeText(OtpAuthenticationActivity.this, "INVALID OTP", Toast.LENGTH_SHORT).show();
                }else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpId,otp);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

    }

    private void initiateOtp() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signInWithPhoneAuthCredential(phoneAuthCredential);

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(OtpAuthenticationActivity.this, "Oho, "+ e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                Log.i("onVerificationFailed",e.getLocalizedMessage());
                            }

                            @Override
                            public void onCodeSent(@NonNull  String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                otpId = s;
                            }
                        })          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task){
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            Toast.makeText(OtpAuthenticationActivity.this, "user = "+ user, Toast.LENGTH_LONG).show();

                            // Update UI
                            Intent intent = new Intent(OtpAuthenticationActivity.this, SetProfileActivity.class);
                            startActivity(intent);

                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                            Toast.makeText(OtpAuthenticationActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}