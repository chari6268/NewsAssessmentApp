package com.chari6268.newsapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    LoadingDialog loadingDialog;
    String key="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        loadingDialog = new LoadingDialog(this);
        loadingDialog.load();
        // Retrieve the intent and its extras
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String phone = intent.getStringExtra("phone");
        String email = intent.getStringExtra("email");
        String profileImage = intent.getStringExtra("profileImage");
        String uuid = intent.getStringExtra("uuid");
        String branch = intent.getStringExtra("branch");
        String city = intent.getStringExtra("city");

        // Find the views
        TextView nameTextView = findViewById(R.id.student_name);
        TextView emailTextView = findViewById(R.id.student_mail);
        TextView branchTextView = findViewById(R.id.student_branch);
        TextView numberTextView = findViewById(R.id.student_number);
        TextView cityTextView = findViewById(R.id.student_city);
        TextView postTextView = findViewById(R.id.student_post);
        Spinner statusSpinner = findViewById(R.id.post_status);
        Button submitButton = findViewById(R.id.student_submit);

        databaseReference = FirebaseDatabase.getInstance().getReference("NewsData")
                .child("NEED_TO_BE_ACTIVATED")
                .child(uuid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    NewsData user = dataSnapshot.getValue(NewsData.class);
                if (user != null) {
                    key = user.getTextInput();

                    nameTextView.setText(name);
                    emailTextView.setText(email);
                    branchTextView.setText(branch);
                    numberTextView.setText(phone);
                    cityTextView.setText(city);
                    postTextView.setText(user.getTextInput());
                    loadingDialog.dismisss();
                }else{
                    loadingDialog.dismisss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DetailActivity.this, "No data found..!", Toast.LENGTH_SHORT).show();
            }
        });

        submitButton.setOnClickListener(v ->{
            loadingDialog.load();
            String status = statusSpinner.getSelectedItem().toString();
            if(status.isEmpty()){
                Toast.makeText(this, "Please select the Status Spinner", Toast.LENGTH_SHORT).show();
                loadingDialog.dismisss();
            }if(key.isEmpty()){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Oops! This users Data not Found ?");
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
            }else{
                FirebaseDatabase.getInstance().getReference().child("NewsData").child("NEED_TO_BE_ACTIVATED")
                        .child(uuid)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        NewsData user = snapshot.getValue(NewsData.class);
                                        if (user != null) {
                                            FirebaseDatabase.getInstance().getReference().child("NewsData").child("NEED_TO_BE_ACTIVATED").child(user.getUserId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    user.setStatus(status);
                                                    FirebaseDatabase.getInstance().getReference().child("NewsData").child("ACTIVATED").child(user.getUserId())
                                                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        loadingDialog.dismisss();
                                                                        startActivity(new Intent(DetailActivity.this, MainActivity.class));
                                                                        finish();
                                                                    }
                                                                }
                                                            });
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
        });

    }
}