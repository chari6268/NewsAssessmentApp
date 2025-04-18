package com.chari6268.newsapplication.siara;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.chari6268.newsapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewsFeedActivity extends AppCompatActivity {
    private ViewPager2 newsViewPager;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsList;
    private BottomNavigationView bottomNavigationView;
    MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.siara_activity_news_feed);

        newsViewPager = findViewById(R.id.news_view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Get selected categories
        List<Category> selectedCategories = getSelectedCategories();

        // Load news feed
        loadNewsFeed(selectedCategories);

        // Set up bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.feed) {
                // Already on feed
                return true;
            } else if (itemId == R.id.saved) {
//                startActivity(new Intent(NewsFeedActivity.this, SavedNewsActivity.class));
                return true;
            } else if (itemId == R.id.search) {
//                startActivity(new Intent(NewsFeedActivity.this, SearchActivity.class));
                return true;
            } else if (itemId == R.id.profile) {
//                startActivity(new Intent(NewsFeedActivity.this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    private List<Category> getSelectedCategories() {
        SharedPreferences preferences = getSharedPreferences("NewsAppPrefs", MODE_PRIVATE);
        String json = preferences.getString("selectedCategories", null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Category>>() {}.getType();
            return gson.fromJson(json, type);
        }

        return new ArrayList<>();
    }

    private void loadNewsFeed(List<Category> selectedCategories) {
        // This would typically be from API, but for demonstration:
        newsList = new ArrayList<>();

        // First add news from selected categories
        for (Category category : selectedCategories) {
            // Fetch news for each category (would be API call)
            fetchNewsForCategory(category.getId(), true);
        }

        // Then add news from other categories
        fetchNewsForCategory(0, false); // 0 = all categories that were not selected

        // Set up adapter
        newsAdapter = new NewsAdapter(newsList, this);
        newsViewPager.setAdapter(newsAdapter);

        // Auto play next news after audio completed
        newsViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Play audio for the current news item
                playNewsAudio(position);
            }
        });
    }

    private void fetchNewsForCategory(int categoryId, boolean isSelected) {
        // This would be an API call in a real app
        // For demonstration, adding dummy data
        if (isSelected) {
            for (int i = 0; i < 5; i++) {
                NewsItem news = new NewsItem();
                news.setId(newsList.size() + 1);
                news.setTitle("Breaking: Major Event Unfolds in City");
                news.setImageUrl("https://images.unsplash.com/photo-1593642634315-48f5414c3ad9");
                String s = (i + 1) + ".mp3";
                news.setAudioUrl("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-"+ s);
                news.setCategoryId(categoryId);
                news.setPostedDate(new Date());
                newsList.add(news);
            }
        } else {
            // Add news from non-selected categories
            for (int i = 0; i < 10; i++) {
                NewsItem news = getNewsItem(i);
                newsList.add(news);
            }
        }
    }

    @NonNull
    private NewsItem getNewsItem(int i) {
        NewsItem news = new NewsItem();
        news.setId(newsList.size() + 1);
        news.setTitle("Breaking: Major Event Unfolds in City");
        news.setImageUrl("https://images.unsplash.com/photo-1593642634315-48f5414c3ad9");
        String s = (i + 1) + ".mp3";
        news.setAudioUrl("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-"+ s);
        news.setCategoryId((i % 5) + 6); // Random category outside selected ones
        news.setPostedDate(new Date());
        return news;
    }

    private void playNewsAudio(int position) {
        // Release previous media player if exists
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        // Create new media player for current news
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(newsList.get(position).getAudioUrl());
            mediaPlayer.prepare();
            mediaPlayer.start();

            // Move to next news item when audio finishes
            mediaPlayer.setOnCompletionListener(mp -> {
                if (position < newsList.size() - 1) {
                    newsViewPager.setCurrentItem(position + 1, true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
