package com.quidvis.moneydrop.interfaces;

public interface HttpRequestCallback {
    void onStarted();
    void onCompleted();
    void onCancelled();
}
