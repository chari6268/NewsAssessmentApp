package com.chari6268.newsapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditActivity extends AppCompatActivity {

    private Uri imageUri,videoUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST = 2;
    private TextView filenameImage;
    private TextView filenameVideo,postTextView;
    ImageView imageView ,videoView;
    Button UpdateButton;
    LoadingDialog loadingDialog;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    final String MY_PREFS_NAME = "status";
    String uuid="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        loadingDialog = new LoadingDialog(this);
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String phone = intent.getStringExtra("phone");
        String email = intent.getStringExtra("email");
        String profileImage = intent.getStringExtra("profileImage");
        uuid = intent.getStringExtra("uuid");
        String branch = intent.getStringExtra("branch");
        String city = intent.getStringExtra("city");
        String postText = intent.getStringExtra("posttext");
        String imageUrl = intent.getStringExtra("imgurl");
        String videoUrl = intent.getStringExtra("videourl");

        // Initialize Firebase Storage and Database
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("NewsData");

        // Find the views
        TextView nameTextView = findViewById(R.id.student_name);
        TextView emailTextView = findViewById(R.id.student_mail);
        TextView branchTextView = findViewById(R.id.student_branch);
        TextView numberTextView = findViewById(R.id.student_number);
        TextView cityTextView = findViewById(R.id.student_city);
        postTextView = findViewById(R.id.student_post);
        imageView = findViewById(R.id.browse);
        videoView = findViewById(R.id.browse1);
        filenameImage = findViewById(R.id.filename);
        filenameVideo = findViewById(R.id.filename1);
        UpdateButton = findViewById(R.id.student_submit);

        nameTextView.setText(name);
        emailTextView.setText(email);
        branchTextView.setText(branch);
        numberTextView.setText(phone);
        cityTextView.setText(city);
        postTextView.setText(postText);
        Glide.with(EditActivity.this).load(imageUrl).into(imageView);
        Glide.with(EditActivity.this).load(videoUrl).into(videoView);

        imageView.setOnClickListener(v -> openImageChooser());
        videoView.setOnClickListener(v -> openVideoChooser());
        UpdateButton.setOnClickListener(v -> handleSubmit());


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
                imageView.setImageURI(imageUri);
            } else if (requestCode == PICK_VIDEO_REQUEST && data != null && data.getData() != null) {
                videoUri = data.getData();
                Glide.with(EditActivity.this).load(videoUri).into(videoView);
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
        String textInput = postTextView.getText().toString().trim();

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
                                    DatabaseReference newsRef = FirebaseDatabase.getInstance().getReference("NewsData").child("NEED_TO_BE_CREATED").child(uuid);
                                    DatabaseReference activatedRef = FirebaseDatabase.getInstance().getReference("NewsData").child("ACTIVATED").child(uuid);

                                    // Create a task to delete data from both locations
                                    Task<Void> deleteNewsTask = newsRef.removeValue();
                                    Task<Void> deleteActivatedTask = activatedRef.removeValue();

                                    // Combine the tasks
                                    Task<Void> allTasks = Tasks.whenAll(deleteNewsTask, deleteActivatedTask);

                                    allTasks.addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Data deleted successfully, proceed with uploading new files
                                                        deleteExistingFilesAndUploadNew(imageUri,videoUri,uuid,textInput);
                                                    } else {
                                                        Toast.makeText(EditActivity.this, "Failed to delete existing data.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void deleteExistingFilesAndUploadNew(final Uri imageUri, final Uri videoUri, final String uuid,String textInput) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        StorageReference imageRef = storageRef.child("images/" + uuid + ".jpg");
        StorageReference videoRef = storageRef.child("videos/" + uuid + ".mp4");
        imageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    videoRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Files deleted, now upload new files
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
                                                Toast.makeText(EditActivity.this, "Failed to upload video.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(EditActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(EditActivity.this, "Failed to delete video file.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(EditActivity.this, "Failed to delete image file.", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                                        Toast.makeText(EditActivity.this, "Data saved successfully.", Toast.LENGTH_SHORT).show();
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