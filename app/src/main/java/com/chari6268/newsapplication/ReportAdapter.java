package com.chari6268.newsapplication;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.CardViewHolder> {

    private List<NewsData> newsDataList;
    private List<userData> userDataList;
    private Context context;

    public ReportAdapter(Context context, List<NewsData> newsDataList,List<userData> userDataList) {
        this.context = context;
        this.newsDataList = newsDataList;
        this.userDataList = userDataList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        NewsData newsDataTest = newsDataList.get(position);
        userData userDataTest = userDataList.get(position);

        Glide.with(holder.itemView.getContext()).load(userDataTest.getProfilePic()).into(holder.profileImageView);
        holder.nameTextView.setText(userDataTest.getName());
        holder.phoneNumberTextView.setText(userDataTest.getPhone());
        holder.emailTextView.setText(userDataTest.getEmail());
        holder.postTestTextView.setText(newsDataTest.getTextInput());

        Glide.with(holder.itemView.getContext()).load(userDataTest.getProfilePic()).into(holder.postImageView);


//        Glide.with(holder.itemView.getContext()).load(newsDataTest.getVideoUrl()).into(holder.postVideoView);
        if (newsDataTest.getVideoUrl() != null) {
            holder.postVideoView.setVisibility(View.VISIBLE);
            Uri videoUri = Uri.parse(newsDataTest.getVideoUrl());
            holder.postVideoView.setVideoURI(videoUri);
            holder.postVideoView.start();
        } else {
            holder.postVideoView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return newsDataList.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView,postImageView;
        TextView nameTextView,phoneNumberTextView,emailTextView,postTestTextView;
        VideoView postVideoView;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.imageViewProfile);
            nameTextView = itemView.findViewById(R.id.textViewname);
            phoneNumberTextView = itemView.findViewById(R.id.textViewNumber);
            emailTextView = itemView.findViewById(R.id.textViewEmail);
            postTestTextView = itemView.findViewById(R.id.postTest);
            postImageView = itemView.findViewById(R.id.postImage); // Ensure this ID exists in your layout
            postVideoView = itemView.findViewById(R.id.postVideo); // Ensure this ID exists in your layout
        }
    }
}
