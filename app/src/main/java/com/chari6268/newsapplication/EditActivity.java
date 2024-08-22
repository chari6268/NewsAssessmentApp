package com.chari6268.newsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

public class EditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String phone = intent.getStringExtra("phone");
        String email = intent.getStringExtra("email");
        String profileImage = intent.getStringExtra("profileImage");
        String uuid = intent.getStringExtra("uuid");
        String branch = intent.getStringExtra("branch");
        String city = intent.getStringExtra("city");
        String postText = intent.getStringExtra("posttext");
        String imageUrl = intent.getStringExtra("imgurl");
        String videoUrl = intent.getStringExtra("videourl");

        // Find the views
        TextView nameTextView = findViewById(R.id.student_name);
        TextView emailTextView = findViewById(R.id.student_mail);
        TextView branchTextView = findViewById(R.id.student_branch);
        TextView numberTextView = findViewById(R.id.student_number);
        TextView cityTextView = findViewById(R.id.student_city);
        TextView postTextView = findViewById(R.id.student_post);
        ImageView imageView = findViewById(R.id.browse);

        nameTextView.setText(name);
        emailTextView.setText(email);
        branchTextView.setText(branch);
        numberTextView.setText(phone);
        cityTextView.setText(city);
        postTextView.setText(postText);
        Glide.with(EditActivity.this).load(imageUrl).into(imageView);
    }
}