package com.chari6268.newsapplication;

public interface UploadCallback {
    void onSuccess(String fileUrl);
    void onFailure(Exception e);
}
