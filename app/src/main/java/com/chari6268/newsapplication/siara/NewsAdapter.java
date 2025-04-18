package com.chari6268.newsapplication.siara;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chari6268.newsapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<NewsItem> newsList;
    private Context context;
    private SimpleDateFormat dateFormat;

    public NewsAdapter(List<NewsItem> newsList, Context context) {
        this.newsList = newsList;
        this.context = context;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem newsItem = newsList.get(position);

        // Set news title
        holder.newsTitle.setText(newsItem.getTitle());

        // Set news date
        String formattedDate = dateFormat.format(newsItem.getPostedDate());
        holder.newsDate.setText(formattedDate);

        // Load image using Glide
        Glide.with(context)
                .load(newsItem.getImageUrl())
                .placeholder(R.drawable.ic_news_placeholder)
                .error(R.drawable.ic_news_error)
                .centerCrop()
                .into(holder.newsImage);

        // Setup action buttons
        setupActionButtons(holder, newsItem);
    }

    private void setupActionButtons(NewsViewHolder holder, NewsItem newsItem) {
        // Save button
        holder.saveButton.setOnClickListener(v -> {
            // Check if already saved
            if (isNewsSaved(newsItem.getId())) {
                // Remove from saved
                removeFromSaved(newsItem.getId());
                holder.saveButton.setImageResource(R.drawable.ic_bookmark_border);
                Toast.makeText(context, "Removed from saved", Toast.LENGTH_SHORT).show();
            } else {
                // Add to saved
                addToSaved(newsItem);
                holder.saveButton.setImageResource(R.drawable.ic_bookmark_filled);
                Toast.makeText(context, "Added to saved", Toast.LENGTH_SHORT).show();
            }
        });

        // Set initial save button state
        if (isNewsSaved(newsItem.getId())) {
            holder.saveButton.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            holder.saveButton.setImageResource(R.drawable.ic_bookmark_border);
        }

        // Share button
        holder.shareButton.setOnClickListener(v -> {
            shareNews(newsItem);
        });
    }

    private boolean isNewsSaved(int newsId) {
        SharedPreferences preferences = context.getSharedPreferences("NewsAppPrefs", Context.MODE_PRIVATE);
        String json = preferences.getString("savedNews", null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<NewsItem>>() {}.getType();
            List<NewsItem> savedNews = gson.fromJson(json, type);

            for (NewsItem item : savedNews) {
                if (item.getId() == newsId) {
                    return true;
                }
            }
        }

        return false;
    }

    private void addToSaved(NewsItem newsItem) {
        SharedPreferences preferences = context.getSharedPreferences("NewsAppPrefs", Context.MODE_PRIVATE);
        String json = preferences.getString("savedNews", null);
        List<NewsItem> savedNews;

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<NewsItem>>() {}.getType();
            savedNews = gson.fromJson(json, type);
        } else {
            savedNews = new ArrayList<>();
        }

        savedNews.add(newsItem);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String updatedJson = gson.toJson(savedNews);
        editor.putString("savedNews", updatedJson);
        editor.apply();
    }

    private void removeFromSaved(int newsId) {
        SharedPreferences preferences = context.getSharedPreferences("NewsAppPrefs", Context.MODE_PRIVATE);
        String json = preferences.getString("savedNews", null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<NewsItem>>() {}.getType();
            List<NewsItem> savedNews = gson.fromJson(json, type);

            List<NewsItem> updatedList = new ArrayList<>();
            for (NewsItem item : savedNews) {
                if (item.getId() != newsId) {
                    updatedList.add(item);
                }
            }

            SharedPreferences.Editor editor = preferences.edit();
            String updatedJson = gson.toJson(updatedList);
            editor.putString("savedNews", updatedJson);
            editor.apply();
        }
    }

    private void shareNews(NewsItem newsItem) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, newsItem.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                newsItem.getTitle() + "\n\nCheck out this interesting news from the News Application!");
        context.startActivity(Intent.createChooser(shareIntent, "Share News via"));
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImage;
        TextView newsTitle;
        TextView newsDate;
        ImageButton saveButton;
        ImageButton shareButton;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage = itemView.findViewById(R.id.news_image);
            newsTitle = itemView.findViewById(R.id.news_title);
            newsDate = itemView.findViewById(R.id.news_date);
            saveButton = itemView.findViewById(R.id.save_button);
            shareButton = itemView.findViewById(R.id.share_button);
        }
    }
}