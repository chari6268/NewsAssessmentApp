package com.chari6268.newsapplication.siara;

import java.io.Serializable;
import java.util.Date;

public class NewsItem implements Serializable {
    private int id;
    private String title;
    private String content;
    private String imageUrl;
    private String audioUrl;
    private int categoryId;
    private Date postedDate;
    private boolean isSaved;

    // Default constructor
    public NewsItem() {
    }

    // Full constructor
    public NewsItem(int id, String title, String content, String imageUrl,
                    String audioUrl, int categoryId, Date postedDate) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
        this.categoryId = categoryId;
        this.postedDate = postedDate;
        this.isSaved = false;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public Date getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(Date postedDate) {
        this.postedDate = postedDate;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewsItem newsItem = (NewsItem) o;
        return id == newsItem.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}