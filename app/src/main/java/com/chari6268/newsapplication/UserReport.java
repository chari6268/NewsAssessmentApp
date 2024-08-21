package com.chari6268.newsapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserReport extends AppCompatActivity {

    private Spinner citySpinner,branchSpinner;
    private Button submitButton;
    private RecyclerView recyclerView;
    private LinearLayout checkLayout,recyclerViewLayout;
    TextView workLoadInToolBar,tittle;
    final String MY_PREFS_NAME = "status";
    private ImageView menu;
    private DatabaseReference databaseReference;
    LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_report);
        loadingDialog = new LoadingDialog(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        workLoadInToolBar = findViewById(R.id.textViewinToolbar);
        workLoadInToolBar.setText("");
        tittle = findViewById(R.id.titleViewinToolbar);
        tittle.setText("User Reports");
        menu = findViewById(R.id.IconinToolbar);
        menu.setOnClickListener(v ->{
            PopupMenu popupMenu = new PopupMenu(this, v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.drawer_view, popupMenu.getMenu());

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.nav_post:
                        AnimationUtils.loadAnimation(this,R.anim.fade_in);
                        startActivity(new Intent(UserReport.this,postNews.class));
                        return true;
                    case R.id.nav_report:
                        return true;
                    case R.id.nav_logout:
                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.remove("uuid");
                        editor.putBoolean("isLoggedIn", false);
                        editor.apply();

                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(UserReport.this, Login.class);
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

        citySpinner = findViewById(R.id.select_city);
        branchSpinner = findViewById(R.id.select_dep);
        submitButton = findViewById(R.id.phone_submit_button);
        recyclerView = findViewById(R.id.report_recycler_view);
        checkLayout = findViewById(R.id.check_layout);
        recyclerViewLayout = findViewById(R.id.recycler_view_layout);

        submitButton.setOnClickListener(v ->{
            loadingDialog.load();
            String city = citySpinner.getSelectedItem().toString();
            String dep = branchSpinner.getSelectedItem().toString();

            if((city.isEmpty() || city.equals("")) && (dep.isEmpty() || dep.equals(""))){
                Toast.makeText(this, "Please Select all fields", Toast.LENGTH_SHORT).show();
            }else {
                AnimationUtils.loadAnimation(UserReport.this, R.anim.fade_in);
                checkLayout.setVisibility(View.GONE);
                recyclerViewLayout.setVisibility(View.VISIBLE);

                databaseReference = FirebaseDatabase.getInstance().getReference("NewsData").child("ACTIVATED");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<NewsData> userList = new ArrayList<>();
                        List<userData> userDataList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            NewsData user = snapshot.getValue(NewsData.class);
                            userList.add(user);
                            FirebaseDatabase.getInstance().getReference("UserData")
                                    .child("ACTIVATED")
                                    .child(user.getUserId()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                userData testUser = snapshot.getValue(userData.class);
                                                userDataList.add(testUser);
                                                System.out.println(userDataList);
                                                System.out.println(userList);
                                                loadingDialog.dismisss();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle possible errors
                        Toast.makeText(UserReport.this, ""+databaseError.toString(), Toast.LENGTH_SHORT).show();
                    }
                });



            }



        });

    }
}