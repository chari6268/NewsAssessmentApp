package com.chari6268.newsapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private DatabaseReference databaseReference;
    LoadingDialog loadingDialog;
    TextView workLoadInToolBar,tittle;
    ImageView menu;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadingDialog = new LoadingDialog(this);
        loadingDialog.load();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        workLoadInToolBar = findViewById(R.id.textViewinToolbar);
        workLoadInToolBar.setText("");
        tittle = findViewById(R.id.titleViewinToolbar);
        menu = findViewById(R.id.IconinToolbar);
        menu.setVisibility(View.GONE);
        tittle.setText("News List to Check");


        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseReference = FirebaseDatabase.getInstance().getReference("NewsData").child("NEED_TO_BE_ACTIVATED");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<NewsData> userList = new ArrayList<>();
                List<userData> filteredUserDataList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    NewsData user = snapshot.getValue(NewsData.class);
                    userList.add(user);
                }
                // Fetch user data and filter based on city and department
                DatabaseReference userDataRef = FirebaseDatabase.getInstance().getReference("UserData").child("ACTIVATED");
                userDataRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                        filteredUserDataList.clear(); // Clear previous data


                        for (DataSnapshot snapshot : userSnapshot.getChildren()) {
                            userData user = snapshot.getValue(userData.class);
                                filteredUserDataList.add(user);
                        }

                        // Extract user IDs from the filtered user data
                        List<String> validUserIds = new ArrayList<>();
                        for (NewsData user : userList) {
                            validUserIds.add(user.getUserId()); // Assuming `uuid` is used to match with `userId` in NewsData
                        }

                        // Filter NewsData based on valid user IDs
                        List<userData> filteredNewUserDataList = new ArrayList<>();
                        for (userData newsData : filteredUserDataList) {
                            if (validUserIds.contains(newsData.getUuid())) {
                                filteredNewUserDataList.add(newsData);
                            }
                        }


                        System.out.println(userList);
                        userAdapter = new UserAdapter(filteredNewUserDataList,MainActivity.this);
                        recyclerView.setAdapter(userAdapter);
                        loadingDialog.dismisss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle possible errors
                        Toast.makeText(MainActivity.this, "Failed to load user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
                Toast.makeText(MainActivity.this, ""+databaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}