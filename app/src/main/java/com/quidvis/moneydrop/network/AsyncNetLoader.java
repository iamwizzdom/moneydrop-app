package com.quidvis.moneydrop.network;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;

import com.quidvis.moneydrop.interfaces.HttpRequestCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class AsyncNetLoader extends AsyncTaskLoader<String> {

    private final Context context;
    private final HttpRequest request;
    private final WebFileReader webFileReader;
    private final HttpRequestCallback requestCallback;

    public AsyncNetLoader(@NonNull Context context, HttpRequest request, HttpRequestCallback requestCallback) {
        super(context);
        this.context = context;
        this.request = request;
        this.webFileReader = null;
        this.requestCallback = requestCallback;
    }

    public AsyncNetLoader(@NonNull Context context, WebFileReader webFileReader, HttpRequestCallback requestCallback) {
        super(context);
        this.context = context;
        this.request = null;
        this.webFileReader = webFileReader;
        this.requestCallback = requestCallback;
    }

    @Nullable
    @Override
    public String loadInBackground() {
        if (request != null) {

            OkHttpClient.Builder client = new OkHttpClient.Builder();
            OkHttpRequest okHttpRequest = this.request.getOkHttpRequest();
            OkHttpRequest.Callback callback = okHttpRequest.getCallback();

            client.callTimeout(okHttpRequest.getTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(okHttpRequest.getTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(okHttpRequest.getTimeout(), TimeUnit.MILLISECONDS)
                    .addInterceptor(okHttpRequest.getInterceptor())
                    .build()
                    .newCall(okHttpRequest.getRequest().build())
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            ((Activity) context).runOnUiThread(callback::onFailure);
                            call.cancel();
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            String body = Objects.requireNonNull(response.body()).string();
                            Map<String, String> headers = new HashMap<>();
                            for (String name: response.headers().names()) {
                                headers.put(name, response.header(name));
                            }
                            ((Activity) context).runOnUiThread(() -> callback.onResponse(body, response.code(), headers));
                            response.close();
                        }
                    });
//            VolleySingleton.getInstance().addToRequestQueue(request.getStringRequest());
        } else webFileReader.getHttpConnector().connect();
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

