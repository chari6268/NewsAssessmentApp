package com.chari6268.newsapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<userData> userList;

    private Context context;

    public UserAdapter(List<userData> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        userData user = userList.get(position);
        holder.textViewName.setText(user.getName());
        holder.textViewNumber.setText(user.getPhone());
        holder.textViewEmail.setText(user.getEmail());

        // Handle click events
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("phone", user.getPhone());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("profileImage", user.getProfilePic());
            intent.putExtra("uuid",user.getUuid());
            intent.putExtra("branch",user.getDepartment());
            intent.putExtra("city",user.getCity());
            context.startActivity(intent);
        });

        // Load profile image (optional, you can use a library like Glide or Picasso)
         Glide.with(holder.itemView.getContext()).load(user.getProfilePic()).into(holder.imageViewProfile);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName,textViewNumber,textViewEmail;
        ImageView imageViewProfile;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewname);
            textViewNumber = itemView.findViewById(R.id.textViewNumber);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
        }
    }
}
