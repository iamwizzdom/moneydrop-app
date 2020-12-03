package com.quidvis.moneydrop.network;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.quidvis.moneydrop.utility.HttpRequest;

public class NetService extends AsyncTaskLoader<String> {

    private final HttpRequest request;
    private final HttpRequest.RequestCallback requestCallback;

    public NetService(@NonNull Context context, HttpRequest request, HttpRequest.RequestCallback requestCallback) {
        super(context);
        this.request = request;
        this.requestCallback = requestCallback;
    }

    @Nullable
    @Override
    public String loadInBackground() {
        VolleySingleton.getInstance().addToRequestQueue(request.getStringRequest());
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

