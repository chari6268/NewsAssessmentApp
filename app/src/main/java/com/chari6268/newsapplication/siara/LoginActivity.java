package com.chari6268.newsapplication.siara;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chari6268.newsapplication.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private EditText phoneNumberET;
    private Button sendOTPBtn;
    TextView skipLoginBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.siara_activity_login);

        mAuth = FirebaseAuth.getInstance();

        phoneNumberET = findViewById(R.id.phone_number_et);
        sendOTPBtn = findViewById(R.id.send_otp_btn);
        skipLoginBtn = findViewById(R.id.skip_login_btn);

        sendOTPBtn.setOnClickListener(v -> {
            String phoneNumber = phoneNumberET.getText().toString().trim();
            if (TextUtils.isEmpty(phoneNumber)) {
                phoneNumberET.setError("Phone number required");
                return;
            }

            // Send OTP via Firebase
            startPhoneVerification(phoneNumber);
        });

        skipLoginBtn.setOnClickListener(v -> {
            // Skip to category selection
            startActivity(new Intent(LoginActivity.this, CategorySelectionActivity.class));
            finish();
        });
    }

    private void startPhoneVerification(String phoneNumber) {
        // Firebase phone authentication logic
        // Assuming phone number is in the format "+1234567890"
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+91 " + phoneNumber; // Add country code if not present
        }
        String finalPhoneNumber = phoneNumber;
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(LoginActivity.this, "Verification failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        // Save verification ID and navigate to OTP input screen
                        Intent intent = new Intent(LoginActivity.this, OTPVerificationActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("phoneNumber", finalPhoneNumber);
                        startActivity(intent);
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Navigate to category selection
                        startActivity(new Intent(LoginActivity.this, CategorySelectionActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
