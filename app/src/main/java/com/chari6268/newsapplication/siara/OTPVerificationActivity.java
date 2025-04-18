package com.chari6268.newsapplication.siara;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chari6268.newsapplication.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OTPVerificationActivity extends AppCompatActivity {

    private EditText otpET1, otpET2, otpET3, otpET4, otpET5, otpET6;
    private TextView resendOtpTV, phoneNumberTV, countdownTV;
    private Button verifyBtn;
    private String phoneNumber;
    private String verificationId;
    private FirebaseAuth firebaseAuth;
    private CountDownTimer countDownTimer;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private final long TIMEOUT_SECONDS = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.siara_activity_otp_verification);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Get phone number from intent
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        verificationId = getIntent().getStringExtra("verificationId");

        if (phoneNumber == null || verificationId == null) {
            Toast.makeText(this, "Phone verification error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Setup OTP input fields
        setupOTPInputs();

        // Set phone number
        phoneNumberTV.setText(phoneNumber);

        // Start countdown timer
        startCountdownTimer();

        // Set click listeners
        verifyBtn.setOnClickListener(v -> {
            verifyOTP();
        });

        resendOtpTV.setOnClickListener(v -> {
            if (resendOtpTV.isEnabled()) {
                resendOTP();
            }
        });
    }

    private void initViews() {
        otpET1 = findViewById(R.id.otp_et_1);
        otpET2 = findViewById(R.id.otp_et_2);
        otpET3 = findViewById(R.id.otp_et_3);
        otpET4 = findViewById(R.id.otp_et_4);
        otpET5 = findViewById(R.id.otp_et_5);
        otpET6 = findViewById(R.id.otp_et_6);
        resendOtpTV = findViewById(R.id.resend_otp_tv);
        phoneNumberTV = findViewById(R.id.phone_number_tv);
        countdownTV = findViewById(R.id.countdown_tv);
        verifyBtn = findViewById(R.id.verify_btn);
    }

    private void setupOTPInputs() {
        otpET1.addTextChangedListener(new OTPTextWatcher(otpET1, otpET2));
        otpET2.addTextChangedListener(new OTPTextWatcher(otpET2, otpET3));
        otpET3.addTextChangedListener(new OTPTextWatcher(otpET3, otpET4));
        otpET4.addTextChangedListener(new OTPTextWatcher(otpET4, otpET5));
        otpET5.addTextChangedListener(new OTPTextWatcher(otpET5, otpET6));
        otpET6.addTextChangedListener(new OTPTextWatcher(otpET6, null));

        // Request focus on first edit text
        otpET1.requestFocus();
    }

    private void startCountdownTimer() {
        resendOtpTV.setEnabled(false);

        countDownTimer = new CountDownTimer(TIMEOUT_SECONDS * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdownTV.setText("Resend OTP in " + (millisUntilFinished / 1000) + " seconds");
            }

            @Override
            public void onFinish() {
                resendOtpTV.setEnabled(true);
                countdownTV.setVisibility(View.GONE);
            }
        }.start();
    }

    private void verifyOTP() {
        String otp = otpET1.getText().toString() +
                otpET2.getText().toString() +
                otpET3.getText().toString() +
                otpET4.getText().toString() +
                otpET5.getText().toString() +
                otpET6.getText().toString();

        if (otp.length() != 6) {
            Toast.makeText(this, "Please enter the complete OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        // Show progress dialog or loading animation here

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    // Hide progress dialog or loading animation here

                    if (task.isSuccessful()) {
                        // Verification successful, navigate to next screen
                        Toast.makeText(OTPVerificationActivity.this, "Verification successful", Toast.LENGTH_SHORT).show();
                        navigateToNextScreen();
                    } else {
                        // Verification failed
                        Toast.makeText(OTPVerificationActivity.this, "Verification failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resendOTP() {
        // Disable resend button
        resendOtpTV.setEnabled(false);

        // Show progress indicator
        Toast.makeText(this, "Sending OTP...", Toast.LENGTH_SHORT).show();

        // Configure Firebase phone auth
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Auto verification completed
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        // Verification failed
                        Toast.makeText(OTPVerificationActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        resendOtpTV.setEnabled(true);
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        // Code sent successfully
                        verificationId = newVerificationId;
                        resendingToken = token;
                        Toast.makeText(OTPVerificationActivity.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();

                        // Restart timer
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        startCountdownTimer();
                        countdownTV.setVisibility(View.VISIBLE);
                    }
                })
                .setForceResendingToken(resendingToken)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void navigateToNextScreen() {
        // Check if user is new or existing
        if (firebaseAuth.getCurrentUser() != null) {
            boolean isNewUser = getIntent().getBooleanExtra("isNewUser", false);

            if (isNewUser) {
                // New user, go to category selection
                startActivity(new Intent(OTPVerificationActivity.this, CategorySelectionActivity.class));
            } else {
                // Existing user, go to news feed
                startActivity(new Intent(OTPVerificationActivity.this, NewsFeedActivity.class));
            }

            // Clear back stack
            finishAffinity();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    // Text watcher class for OTP input
    class OTPTextWatcher implements TextWatcher {
        private EditText currentEditText;
        private EditText nextEditText;

        OTPTextWatcher(EditText currentEditText, EditText nextEditText) {
            this.currentEditText = currentEditText;
            this.nextEditText = nextEditText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextEditText != null) {
                nextEditText.requestFocus();
            } else if (s.length() == 0) {
                // Focus to previous edit text when backspace is pressed
                View previousEditText = currentEditText.focusSearch(View.FOCUS_LEFT);
                if (previousEditText instanceof EditText) {
                    previousEditText.requestFocus();
                }
            }

            // Enable verify button if all fields are filled
            checkInputFields();
        }
    }

    private void checkInputFields() {
        String otp = otpET1.getText().toString() +
                otpET2.getText().toString() +
                otpET3.getText().toString() +
                otpET4.getText().toString() +
                otpET5.getText().toString() +
                otpET6.getText().toString();

        verifyBtn.setEnabled(otp.length() == 6);
    }
}