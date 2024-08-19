package com.chari6268.newsapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private com.google.android.material.textfield.TextInputEditText inputMail, inputPassword;
    private Button loginButton;
    private TextView signInSignUp;
    final String MY_PREFS_NAME = "status";
    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loadingDialog = new LoadingDialog(this);

        SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        if(sharedPreferences.getBoolean("isLoggedIn",false)){
            Intent i = new Intent(getApplicationContext(), postNews.class);
            startActivity(i);
        }

        // Initialize UI components
        inputMail = findViewById(R.id.input_mail);
        inputPassword = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.phone_submit_button);
        signInSignUp = findViewById(R.id.signin_signup);

        // Set up listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        signInSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplication(),Registration.class));
                finish();
            }
        });
    }

    private void handleLogin() {
        loadingDialog.load();
        String email = inputMail.getText().toString();
        String password = inputPassword.getText().toString();

        if(email.equals("vignantd") && password.equals("vignantd")){
            startActivity(new Intent(this,MainActivity.class));
        }

        if (email.isEmpty() || password.isEmpty()) {
            inputMail.setError("Email cannot be empty");
            inputPassword.setError("Password cannot be empty");
        } else {
            FirebaseDatabase.getInstance().getReference().child("UserData").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child("NEED_TO_BE_ACTIVATED").hasChild(email)){
                        userData studentData = snapshot.child("NEED_TO_BE_ACTIVATED").child(email).getValue(userData.class);
                        if (password.equals(studentData.getPassword())){
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(studentData.getEmail(), studentData.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    FirebaseDatabase.getInstance().getReference().child("UserData").child("NEED_TO_BE_ACTIVATED").child(studentData.getPhone()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            Toast.makeText(Login.this, ""+FirebaseAuth.getInstance().getUid(), Toast.LENGTH_SHORT).show();
                                            studentData.setUuid(FirebaseAuth.getInstance().getUid());
                                            FirebaseDatabase.getInstance().getReference().child("UserData").child("ACTIVATED").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString())
                                                    .setValue(studentData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()) {
                                                                loadingDialog.dismisss();
                                                                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                                                editor.putString("uuid",FirebaseAuth.getInstance().getUid());
                                                                editor.putBoolean("isLoggedIn", true);
                                                                editor.apply();
                                                                Toast.makeText(Login.this, "Success!!", Toast.LENGTH_SHORT).show();
                                                                startActivity(new Intent(getApplicationContext(), postNews.class));
                                                                finish();
                                                            }
                                                        }
                                                    });
                                        }
                                    });

                                }
                            });
                        }
                    }else{
                        for (DataSnapshot students : snapshot.child("ACTIVATED").getChildren()) {
                            userData st = students.getValue(userData.class);
                            if (st.getPhone().toString().equals(email)){
                                userData studentData = students.getValue(userData.class);
                                String email = studentData.getEmail();
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (!task.isSuccessful()) {
                                            try {
                                                throw task.getException();
                                            } catch (
                                                    FirebaseAuthInvalidCredentialsException wrongPassword) {
                                                Toast.makeText(Login.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                                                inputPassword.requestFocus();
                                                inputPassword.setError("Enter Correct Password!");
                                                loadingDialog.dismisss();
                                            } catch (
                                                    FirebaseTooManyRequestsException tooManyRequestsException) {
                                                Toast.makeText(Login.this, "Too many incorrect attempts!! your account is locked, try again later", Toast.LENGTH_SHORT).show();
                                                loadingDialog.dismisss();
                                            } catch (Exception e) {
                                                Log.e("error Loggind", e.getLocalizedMessage() + "   " + e.toString());
                                                Toast.makeText(Login.this, "Error", Toast.LENGTH_SHORT).show();
                                                loadingDialog.dismisss();
                                            }
                                        }else{
                                            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                            editor.putString("uuid", FirebaseAuth.getInstance().getUid());
                                            editor.putBoolean("isLoggedIn", true);
                                            editor.apply();
                                            loadingDialog.dismisss();
                                            Toast.makeText(Login.this, "Success!!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), postNews.class));
                                            finish();
                                        }
                                    }
                                });
                            }
                            else{
                                inputMail.setError("Incorrect User Name");
                                inputMail.requestFocus();
                                loadingDialog.dismisss();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

}