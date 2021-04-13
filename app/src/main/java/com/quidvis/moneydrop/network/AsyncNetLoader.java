package com.quidvis.moneydrop.network;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.quidvis.moneydrop.interfaces.HttpRequestCallback;

public class AsyncNetLoader extends AsyncTaskLoader<String> {

    private final HttpRequest request;
    private final WebFileReader webFileReader;
    private final HttpRequestCallback requestCallback;

    public AsyncNetLoader(@NonNull Context context, HttpRequest request, HttpRequestCallback requestCallback) {
        super(context);
        this.request = request;
        this.webFileReader = null;
        this.requestCallback = requestCallback;
    }

    public AsyncNetLoader(@NonNull Context context, WebFileReader webFileReader, HttpRequestCallback requestCallback) {
        super(context);
        this.request = null;
        this.webFileReader = webFileReader;
        this.requestCallback = requestCallback;
    }

    @Nullable
    @Override
    public String loadInBackground() {
        if (request != null) VolleySingleton.getInstance().addToRequestQueue(request.getStringRequest());
        else webFileReader.getHttpConnector().connect();
        return null;
    }

    @Override
    protected void onStartLoading() {
        // Start loading
        super.onStartLoading();
        requestCallback.onStarted();
        forceLoad();
    }

    @Override
    public void deliverResult(String data) {
        if (isStarted()) {
            // Deliver result if loader is currently started
            requestCallback.onCompleted();
            super.deliverResult(data);
        }
    }

    @Override
    public void deliverCancellation() {
        if (isStarted()) {
            // Deliver cancellation if loader is currently started
            requestCallback.onCancelled();
            super.deliverCancellation();
        }
    }
}

