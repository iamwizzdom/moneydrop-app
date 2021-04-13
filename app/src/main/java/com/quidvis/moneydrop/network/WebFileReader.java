package com.quidvis.moneydrop.network;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.fragment.custom.CustomCompatFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestCallback;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.Validator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Wisdom Emenike.
 * Date: 10/1/2018
 * Time: 3:13 PM
 */

public abstract class WebFileReader {

    private static int centralRequestID = 0;
    private final int requestID;
    private final AppCompatActivity activity;
    private final Fragment fragment;
    private final String url;
    private final int method;
    private HttpURLConnection connection;
    private Map<String, Object> headers;
    private Runnable runnable;
    private final Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    private final LoaderManager loaderManager;
    private HttpConnector httpConnector;

    private static boolean ongoingTask = false;

    protected WebFileReader(AppCompatActivity activity, String url, int method) {
        requestID = ++centralRequestID;
        this.activity = activity;
        this.fragment = null;
        this.url = url;
        this.method = method;
        loaderManager = LoaderManager.getInstance(this.activity);
    }

    protected WebFileReader(AppCompatActivity activity, String url, int method, Map<String, Object> headers) {
        requestID = ++centralRequestID;
        this.activity = activity;
        this.fragment = null;
        this.url = url;
        this.headers = headers;
        this.method = method;
        loaderManager = LoaderManager.getInstance(this.activity);
    }

    protected WebFileReader(Fragment fragment, String url, int method) {
        requestID = ++centralRequestID;
        this.fragment = fragment;
        this.activity = (AppCompatActivity) fragment.requireActivity();
        this.url = url;
        this.method = method;
        loaderManager = LoaderManager.getInstance(this.activity);
    }

    protected WebFileReader(Fragment fragment, String url, Map<String, Object> headers) {
        requestID = ++centralRequestID;
        this.fragment = fragment;
        this.activity = (AppCompatActivity) fragment.requireActivity();
        this.url = url;
        this.headers = headers;
        this.method = Method.GET;
        loaderManager = LoaderManager.getInstance(this.activity);
    }

    protected abstract void onReadStarted();
    protected abstract void onReadCancelled();
    protected abstract void onReadCompleted(boolean onError);
    protected abstract void onReadSuccess(String fileName, byte[] fileByte, int statusCode, Map<String, List<String>> headers);
    protected abstract void onReadError(String error, int statusCode, Map<String, List<String>> headers);

    public HttpConnector getHttpConnector() {
        return httpConnector;
    }

    public final void send() {

        if (!Validator.isNetworkConnected(activity)) {
            onReadCompleted(true);
            onReadError("No network connection to read file.", 503, null);
            return;
        }

        if (isOngoingTask()) {
            if (runnable != null) handler.removeCallbacks(runnable);
            handler.postDelayed(runnable = WebFileReader.this::send, 500);
            return;
        }

        httpConnector = new HttpConnector() {
            @Override
            public void connect() {

                try {

                    connection = (HttpURLConnection) (new URL(url)).openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod(getRequestMethod(method));

                    if (headers != null && headers.size() > 0) {
                        for (Map.Entry<String, Object> header : headers.entrySet()) {
                            connection.setRequestProperty(header.getKey(), header.getValue().toString());
                        }
                    }

                    connection.connect();

                    if (WebFileReader.this.fragment instanceof CustomCompatFragment) {
                        ((CustomCompatFragment) WebFileReader.this.fragment).removeOnStopFragmentListener(requestID);
                    } else if (WebFileReader.this.activity instanceof CustomCompatActivity) {
                        ((CustomCompatActivity) WebFileReader.this.activity).removeOnStopActivityListener(requestID);
                    }

                    int responseCode = connection.getResponseCode();

                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        setOngoingTask(false);
                        activity.runOnUiThread(() -> {
                            onReadCompleted(true);
                            onReadError("An unexpected error occurred while reading file.", responseCode, connection.getHeaderFields());
                        });
                        return;
                    }

                    final String disposition = connection.getHeaderField("Content-Disposition"), fileName;
                    if (disposition != null) {
                        int startIndex = disposition.indexOf("filename=") + 10, endIndex = disposition.length() - 1;
                        fileName = disposition.substring(startIndex, endIndex);
                    } else fileName = UUID.randomUUID().toString();

                    setOngoingTask(false);
                    final byte[] fileByte = Utility.toByteArray(connection.getInputStream());

                    activity.runOnUiThread(() -> {
                        onReadCompleted(false);
                        onReadSuccess(fileName, fileByte, responseCode, connection.getHeaderFields());
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    setOngoingTask(false);
                    activity.runOnUiThread(() -> {
                        onReadCompleted(true);
                        onReadError("An unexpected error occurred while reading file.", 417, connection != null ? connection.getHeaderFields() : null);
                    });
                }
            }
        };

        loaderManager.initLoader(requestID, null, new WebFileReader.LoaderCallback(activity, this, new WebFileReader.RequestCallback() {

            @Override
            public void onStarted() {
                setOngoingTask(true);
                onReadStarted();
            }

            @Override
            public void onCompleted() {
                loaderManager.destroyLoader(requestID);
            }

            @Override
            public void onCancelled() {
                cancel();
            }
        }));

        if (this.fragment instanceof CustomCompatFragment) {

            ((CustomCompatFragment) this.fragment).setOnStopFragmentListener(requestID, () -> {
                onReadCompleted(false);
                cancel();
                ((CustomCompatFragment) this.fragment).removeOnStopFragmentListener(requestID);
            });

        } else if (this.activity instanceof CustomCompatActivity) {

            ((CustomCompatActivity) this.activity).setOnStopActivityListener(requestID, () -> {
                onReadCompleted(false);
                cancel();
                ((CustomCompatActivity) this.activity).removeOnStopActivityListener(requestID);
            });
        }
    }

    protected void cancel() {
        setOngoingTask(false);
        loaderManager.destroyLoader(requestID);
        if (connection != null) connection.disconnect();
        onReadCancelled();
    }

    private static boolean isOngoingTask() {
        return ongoingTask;
    }

    private static void setOngoingTask(boolean ongoingTask) {
        WebFileReader.ongoingTask = ongoingTask;
    }

    private String getRequestMethod(int method) {
        switch (method) {
            case Method.POST:
                return "POST";
            case Method.PUT:
                return "PUT";
            case Method.DELETE:
                return "DELETE";
            case Method.HEAD:
                return "HEAD";
            case Method.OPTIONS:
                return "OPTIONS";
            case Method.TRACE:
                return "TRACE";
            case Method.PATCH:
                return "PATCH";
            default:
                return "GET";
        }
    }

    /**
     * Supported request methods.
     */
    public interface Method {
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }

    public static abstract class HttpConnector {
        public abstract void connect();
    }

    public abstract static class RequestCallback implements HttpRequestCallback {
    }

    private static class LoaderCallback implements LoaderManager.LoaderCallbacks<String> {

        private final AppCompatActivity activity;
        private final WebFileReader webFileReader;
        private final WebFileReader.RequestCallback callback;

        LoaderCallback(AppCompatActivity activity, WebFileReader webFileReader, WebFileReader.RequestCallback callback) {
            this.activity = activity;
            this.webFileReader = webFileReader;
            this.callback = callback;
        }

        @NonNull
        @Override
        public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
            return new AsyncNetLoader(activity, webFileReader, callback);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        }

        @Override
        public void onLoaderReset(@NonNull Loader<String> loader) {

        }
    }
}
