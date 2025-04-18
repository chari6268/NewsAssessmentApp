package com.chari6268.newsapplication.vignan;

public interface UploadCallback {
    void onSuccess(String fileUrl);
    void onFailure(Exception e);
}
