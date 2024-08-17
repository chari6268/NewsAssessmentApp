package com.chari6268.newsapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Registration extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;

    private ImageView imageView;
    private TextView fileNameTextView;
    private Uri imageUri;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private EditText nameEditText, emailEditText, passwordEditText, phoneEditText;
    private Spinner citySpinner, depSpinner;
    private ImageView profileImageView;
    private Button submitButton;
    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        loadingDialog = new LoadingDialog(this);

        storage = FirebaseStorage.getInstance();

        nameEditText = findViewById(R.id.signup_name);
//        collegeIdEditText = findViewById(R.id.signup_college_id);
        emailEditText = findViewById(R.id.signup_email);
        passwordEditText = findViewById(R.id.signup_password);
        phoneEditText = findViewById(R.id.signup_phone);
        citySpinner = findViewById(R.id.city);
        depSpinner = findViewById(R.id.dep);
        profileImageView = findViewById(R.id.browse);
        submitButton = findViewById(R.id.signup_submit);
        fileNameTextView = findViewById(R.id.filename);

        profileImageView.setOnClickListener(v -> openFileChooser());
        submitButton.setOnClickListener(v -> validateAndSubmit());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void validateAndSubmit() {
        loadingDialog.load();
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String city = citySpinner.getSelectedItem().toString();
        String dep = depSpinner.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Please fill all fields and upload an image", Toast.LENGTH_SHORT).show();
            loadingDialog.dismisss();
            return;
        }

        if (!email.contains("@gmail.com")) {
            emailEditText.setError("Email must be a Gmail address");
            loadingDialog.dismisss();
            return;
        }

        if (phone.length() != 10) {
            phoneEditText.setError("Phone number must be 10 digits long");
            loadingDialog.dismisss();
            return;
        }else {


            // Upload image to Firebase Storage
            final StorageReference fileReference = storage.getReference("profile_pics").child(System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();

                                    UUID uuid = UUID.randomUUID();

                                    // Create user data map
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("name", name);
                                    user.put("email", email);
                                    user.put("password", password);
                                    user.put("phone", phone);
                                    user.put("city", city);
                                    user.put("department", dep);
                                    user.put("profilePic", imageUrl);
                                    user.put("uniqueId", uuid);
                                    userData details = new userData(name, email, password, phone, city, dep, imageUrl, String.valueOf(uuid));

                                    FirebaseDatabase.getInstance().getReference().child("UserData").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.child("NEED_TO_BE_ACTIVATED").hasChild(phone)) {
                                                loadingDialog.dismisss();
                                                Toast.makeText(getApplication(), "Oops!! User already Registered..!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                saveData(details);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            loadingDialog.dismisss();
                                        }
                                    });
                                    System.out.println(user);

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingDialog.dismisss();
                            Toast.makeText(getApplication(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

//        uploadImage(name, collegeId, email, password, phone, city, dep);
    }


    public void saveData(userData details){
        FirebaseDatabase.getInstance().getReference().child("UserData").child("NEED_TO_BE_ACTIVATED")
                .child(details.getPhone()).setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseDatabase.getInstance().getReference().child("UserData").child("NEED_TO_BE_CREATED")
                                .child(details.getPhone()).setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        loadingDialog.dismisss();
                                        Toast.makeText(getApplication(), "Success!! You Can Login Now", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplication(),Login.class));
                                        finish();
                                    }
                                });
                    }
                });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            String fileName = new File(imageUri.getPath()).getName();
            fileNameTextView.setText(fileName);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChooser();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}