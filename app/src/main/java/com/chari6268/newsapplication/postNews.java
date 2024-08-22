package com.chari6268.newsapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class postNews extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST = 2;

    private com.google.android.material.textfield.TextInputEditText inputMail;
    private ImageView browseImage,browseVideo,menu;
    private TextView filenameImage;
    private TextView filenameVideo;
    private Button submitButton;

    private Uri imageUri;
    private Uri videoUri;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    final String MY_PREFS_NAME = "status";
    LoadingDialog loadingDialog;
    String uuid;
    TextView workLoadInToolBar,tittle;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_news);
        loadingDialog = new LoadingDialog(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        workLoadInToolBar = findViewById(R.id.textViewinToolbar);
        workLoadInToolBar.setText("");
        tittle = findViewById(R.id.titleViewinToolbar);
        menu = findViewById(R.id.IconinToolbar);
        menu.setOnClickListener(v ->{
            PopupMenu popupMenu = new PopupMenu(this, v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.drawer_view, popupMenu.getMenu());

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.nav_post:
                        return true;
                    case R.id.nav_report:
                        AnimationUtils.loadAnimation(this,R.anim.fade_in);
                        startActivity(new Intent(postNews.this, UserReport.class));
                        return true;
                    case R.id.nav_logout:
                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.remove("uuid");
                        editor.putBoolean("isLoggedIn", false);
                        editor.apply();

                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(postNews.this, Login.class);
                        startActivity(intent);
                        finish();
                        return true;

                    default:
                        return false;
                }
            });

            // Show the PopupMenu
            popupMenu.show();
        });


        setSupportActionBar(toolbar);


        sharedPreferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        uuid = sharedPreferences.getString("uuid", "");

        // Initialize Firebase Storage and Database
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("NewsData");


        // Initialize views
        inputMail = findViewById(R.id.input_mail);
        browseImage = findViewById(R.id.browse);
        browseVideo = findViewById(R.id.browse1);
        filenameImage = findViewById(R.id.filename);
        filenameVideo = findViewById(R.id.filename1);
        submitButton = findViewById(R.id.phone_submit_button);


        FirebaseDatabase.getInstance().getReference("NewsData").child("NEED_TO_BE_CREATED")
                .child(uuid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            NewsData user = snapshot.getValue(NewsData.class);
                            if (user.getUserId() != null) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(postNews.this);
                                builder.setMessage("Oops! User already post the News ?");
                                builder.setTitle("Alert!!");
                                builder.setCancelable(true);
                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Set click listeners
        browseImage.setOnClickListener(v -> openImageChooser());
        browseVideo.setOnClickListener(v -> openVideoChooser());
        submitButton.setOnClickListener(v -> handleSubmit());
    }

    // Open image chooser
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    // Open video chooser
    private void openVideoChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                imageUri = data.getData();
                filenameImage.setText(getFileName(imageUri));
                browseImage.setImageURI(imageUri);
            } else if (requestCode == PICK_VIDEO_REQUEST && data != null && data.getData() != null) {
                videoUri = data.getData();
                filenameVideo.setText(getFileName(videoUri));
            }
        }
    }

    // Get file name from Uri
    private String getFileName(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        }
        return "Unknown File";
    }

    private void handleSubmit() {
        loadingDialog.load();
        String textInput = inputMail.getText().toString().trim();

        if (textInput.isEmpty()) {
            loadingDialog.dismisss();
            alert();
            Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            alert();
            loadingDialog.dismisss();
            Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (videoUri == null) {
            loadingDialog.dismisss();
            alert();
            Toast.makeText(this, "Please upload a video", Toast.LENGTH_SHORT).show();
            return;
        }

        else{

            FirebaseDatabase.getInstance().getReference("NewsData").child("NEED_TO_BE_CREATED")
                    .child(uuid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                NewsData user = snapshot.getValue(NewsData.class);
                                if(user.getUserId() != null){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(postNews.this);
                                    builder.setMessage("Oops! User already post the News ?");
                                    builder.setTitle("Alert!!");
                                    builder.setCancelable(true);
                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            loadingDialog.dismisss();
                                            dialog.cancel();
                                        }
                                    });
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                }

                            }else{
                                    uploadToFirebaseStorage(imageUri, "images/" + uuid + ".jpg", new UploadCallback() {
                                        @Override
                                        public void onSuccess(String fileUrl1) {
                                            uploadToFirebaseStorage(videoUri, "videos/" + uuid + ".mp4", new UploadCallback() {
                                                @Override
                                                public void onSuccess(String fileUrl) {
                                                    saveMetadataToDatabase(uuid, textInput, fileUrl1, fileUrl);
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                    Toast.makeText(postNews.this, "Failed to upload video.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(postNews.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }


    private void uploadToFirebaseStorage(Uri fileUri, String path, UploadCallback callback) {
        StorageReference storageReference = firebaseStorage.getReference(path);
        storageReference.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(callback::onFailure))
                .addOnFailureListener(callback::onFailure);
    }

    private void saveMetadataToDatabase(String userId, String textInput, String imageUrl, String videoUrl) {
        NewsData newsData = new NewsData(userId, textInput, imageUrl, videoUrl,"");
        databaseReference.child("NEED_TO_BE_ACTIVATED").child(userId).setValue(newsData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseDatabase.getInstance().getReference().child("NewsData").child("NEED_TO_BE_CREATED")
                                .child(uuid).setValue(newsData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        loadingDialog.dismisss();
                                        Toast.makeText(postNews.this, "Data saved successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Failed to save data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void alert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Oops! Please fill all fields and upload an image and video ?");
        builder.setTitle("Alert!!");
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}