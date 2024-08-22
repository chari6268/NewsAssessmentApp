package com.chari6268.newsapplication;

public class NewsData {
    public String userId;
    public String textInput;
    public String imageUrl;
    public String videoUrl;
    public String status;

    public NewsData() {
    }

    public NewsData(String userId, String textInput, String imageUrl, String videoUrl,String status) {
        this.userId = userId;
        this.textInput = textInput;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTextInput() {
        return textInput;
    }

    public void setTextInput(String textInput) {
        this.textInput = textInput;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "NewsData{" +
                "userId='" + userId + '\'' +
                ", textInput='" + textInput + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
