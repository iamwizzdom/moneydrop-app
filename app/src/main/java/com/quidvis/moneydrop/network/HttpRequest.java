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

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.quidvis.moneydrop.activity.MainActivity;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.fragment.custom.CustomCompatFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestCallback;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.utility.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Objects;

import static com.quidvis.moneydrop.constant.Constant.RETRY_IN_30_SEC;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Wisdom Emenike.
 * Date: 10/1/2018
 * Time: 3:13 PM
 */
public abstract class HttpRequest {

    private static int centralRequestID = 0;
    private final int requestID;
    private static boolean ongoingTask = false;
    private final String url;
    private final int method;
    private HttpRequestParams httpRequestParams;
    private StringRequest stringRequest;
    private OkHttpRequest okHttpRequest;
    private Request.Builder okHttpRequestBuilder;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final Fragment fragment;
    private final AppCompatActivity activity;
    private Map<String, String> headers;
    private int statusCode;
    private Runnable runnable;
    private final Handler handler;
    private final LoaderManager loaderManager;

    public HttpRequest(AppCompatActivity activity, String url, int method) {
        requestID = ++centralRequestID;
        this.url = url;
        this.method = method;
        this.fragment = null;
        this.activity = activity;
        handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        loaderManager = LoaderManager.getInstance(this.activity);
    }

    public HttpRequest(AppCompatActivity activity, String url, int method, HttpRequestParams httpRequestParams) {
        requestID = ++centralRequestID;
        this.url = url;
        this.method = method;
        this.fragment = null;
        this.activity = activity;
        this.httpRequestParams = httpRequestParams;
        handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        loaderManager = LoaderManager.getInstance(this.activity);
    }

    public HttpRequest(AppCompatActivity activity, String url, HttpRequestParams httpRequestParams) {
        requestID = ++centralRequestID;
        this.url = url;
        this.method = Method.POST;
        this.fragment = null;
        this.activity = activity;
        this.httpRequestParams = httpRequestParams;
        handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        loaderManager = LoaderManager.getInstance(this.activity);
    }

    public HttpRequest(Fragment fragment, String url, int method) {
        requestID = ++centralRequestID;
        this.url = url;
        this.method = method;
        this.fragment = fragment;
        this.activity = (AppCompatActivity) fragment.requireActivity();
        handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        loaderManager = LoaderManager.getInstance(this.activity);
    }

    public HttpRequest(Fragment fragment, String url, int method, HttpRequestParams httpRequestParams) {
        requestID = ++centralRequestID;
        this.url = url;
        this.method = method;
        this.fragment = fragment;
        this.activity = (AppCompatActivity) fragment.requireActivity();
        this.httpRequestParams = httpRequestParams;
        handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        loaderManager = LoaderManager.getInstance(this.activity);
    }

    public HttpRequest(Fragment fragment, String url, HttpRequestParams httpRequestParams) {
        requestID = ++centralRequestID;
        this.url = url;
        this.method = Method.POST;
        this.fragment = fragment;
        this.activity = (AppCompatActivity) fragment.requireActivity();
        this.httpRequestParams = httpRequestParams;
        handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        loaderManager = LoaderManager.getInstance(this.activity);
    }

    public StringRequest getStringRequest() {
        return stringRequest;
    }

    public OkHttpRequest getOkHttpRequest() {
        return okHttpRequest;
    }

    protected abstract void onRequestStarted();
    protected abstract void onRequestCancelled();
    protected abstract void onRequestCompleted(boolean onError);
    protected abstract void onRequestSuccess(String response, int statusCode, Map<String, String> headers);
    protected abstract void onRequestError(String error, int statusCode, Map<String, String> headers);

    public final void send() {
        send(RETRY_IN_30_SEC, false);
    }

    public final void send(int timeout) {
        send(timeout, false);
    }

    public final void send(boolean runOnCompletedLast) {
        send(RETRY_IN_30_SEC, runOnCompletedLast);
    }

    public final void send(int timeout, boolean runOnCompletedLast) {

        if (!Validator.isNetworkConnected(activity)) {
            onRequestCompleted(true);
            onRequestError("No network connection, check your connection and try again.", 503, null);
            return;
        }

        if (isOngoingTask()) {
            if (runnable != null) handler.removeCallbacks(runnable);
            handler.postDelayed(runnable = HttpRequest.this::send, 500);
            return;
        }

//        Response.Listener<String> listener = response -> {
//            // Give back the response string.
//
//            if (this.fragment instanceof CustomCompatFragment) {
//                ((CustomCompatFragment) this.fragment).removeOnStopFragmentListener(requestID);
//            } else if (this.activity instanceof CustomCompatActivity) {
//                ((CustomCompatActivity) this.activity).removeOnStopActivityListener(requestID);
//            }
//
//            setOngoingTask(false);
//
//            response = response.trim();
//
//            if (statusCode == 419) {
//                String title = "Auth Error", message = "Seems your current session has expired, please login again to continue.";
//                try {
//                    JSONObject object = new JSONObject(response);
//                    title = object.getString("title");
//                    message = object.getString("message");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                MainActivity.logout(activity, title, message, statusCode);
//                return;
//            }
//            if (!runOnCompletedLast) onRequestCompleted(false);
//            onRequestSuccess(response, statusCode, headers);
//            if (runOnCompletedLast) onRequestCompleted(false);
//            stringRequest.cancel();
//        };

//        Response.ErrorListener errorListener = error -> {
//
//            if (this.fragment instanceof CustomCompatFragment) {
//                ((CustomCompatFragment) this.fragment).removeOnStopFragmentListener(requestID);
//            } else if (this.activity instanceof CustomCompatActivity) {
//                ((CustomCompatActivity) this.activity).removeOnStopActivityListener(requestID);
//            }
//
//            setOngoingTask(false);
//
//            NetworkResponse response = error.networkResponse;
//            int statusCode = response != null ? response.statusCode : HttpURLConnection.HTTP_NO_CONTENT;
//            String responseData = response != null ? new String(response.data) : "";
//            responseData = responseData.trim();
//
//            if (statusCode == 419) {
//                String title = "Auth Error", message = "Seems your current session has expired, please login again to continue.";
//                try {
//                    JSONObject object = new JSONObject(responseData);
//                    title = object.getString("title");
//                    message = object.getString("message");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                MainActivity.logout(activity, title, message, statusCode);
//                return;
//            }
//            if (!runOnCompletedLast) onRequestCompleted(true);
//            onRequestError(responseData, statusCode, headers);
//            if (runOnCompletedLast) onRequestCompleted(true);
//            stringRequest.cancel();
//            error.printStackTrace();
//        };

        // Request a string response from the provided URL.
//        setStringRequest(method, url, listener, errorListener);
        setOkHttpRequestBuilder(method, url);

        okHttpRequest = new OkHttpRequest(okHttpRequestBuilder, new OkHttpRequest.Callback() {
            @Override
            public void onFailure() {

                if (HttpRequest.this.fragment instanceof CustomCompatFragment) {
                    ((CustomCompatFragment) HttpRequest.this.fragment).removeOnStopFragmentListener(requestID);
                } else if (HttpRequest.this.activity instanceof CustomCompatActivity) {
                    ((CustomCompatActivity) HttpRequest.this.activity).removeOnStopActivityListener(requestID);
                }

                setOngoingTask(false);
                if (!runOnCompletedLast) onRequestCompleted(true);
                onRequestError("Request failed", 503, null);
                if (runOnCompletedLast) onRequestCompleted(true);
            }

            @Override
            public void onResponse(String response, int statusCode, Map<String, String> headers) {

                if (HttpRequest.this.fragment instanceof CustomCompatFragment) {
                    ((CustomCompatFragment) HttpRequest.this.fragment).removeOnStopFragmentListener(requestID);
                } else if (HttpRequest.this.activity instanceof CustomCompatActivity) {
                    ((CustomCompatActivity) HttpRequest.this.activity).removeOnStopActivityListener(requestID);
                }

                setOngoingTask(false);

                if (statusCode == 419) {
                    String title = "Auth Error", message = "Seems your current session has expired, please login again to continue.";
                    try {
                        JSONObject object = new JSONObject(response);
                        title = object.getString("title");
                        message = object.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    MainActivity.logout(activity, title, message, statusCode);
                    return;
                }

                boolean success = (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_CREATED);

                if (!runOnCompletedLast) onRequestCompleted(false);
                if (success) onRequestSuccess(response, statusCode, headers);
                else onRequestError(response, statusCode, headers);
                if (runOnCompletedLast) onRequestCompleted(false);

            }
        });

        okHttpRequest.setTimeout(timeout);
        okHttpRequest.setInterceptor(new RetryInterceptor(2));


        // Add the request to the RequestQueue.// Add the request to the RequestQueue.
//        RetryPolicy policy = new DefaultRetryPolicy(timeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//        stringRequest.setRetryPolicy(policy);

        // Add the request to the RequestQueue.
        loaderManager.initLoader(requestID, null, new LoaderCallback(activity, this, new RequestCallback() {

            @Override
            public void onStarted() {
                setOngoingTask(true);
                onRequestStarted();
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

            ((CustomCompatFragment) this.fragment).setOnStopFragmentListener(requestID, (key) -> {
                onRequestCompleted(false);
                cancel();
                ((CustomCompatFragment) this.fragment).removeOnStopFragmentListener(key);
            });

        } else if (this.activity instanceof CustomCompatActivity) {

            ((CustomCompatActivity) this.activity).setOnStopActivityListener(requestID, (key) -> {
                onRequestCompleted(false);
                cancel();
                ((CustomCompatActivity) this.activity).removeOnStopActivityListener(key);
            });
        }
    }

    public final void cancel() {
        setOngoingTask(false);
        loaderManager.destroyLoader(requestID);
        if (stringRequest != null) stringRequest.cancel();
        onRequestCancelled();
    }

    public final int getRequestID() {
        return requestID;
    }

    private static boolean isOngoingTask() {
        return ongoingTask;
    }

    private static void setOngoingTask(boolean ongoingTask) {
        HttpRequest.ongoingTask = ongoingTask;
    }

    private void setStringRequest(int method, String url, Response.Listener<String> listener,
                                  Response.ErrorListener errorListener) {

        if (httpRequestParams == null) {

            stringRequest = new StringRequest(method, url, listener, errorListener) {

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    statusCode = response.statusCode;
                    headers = response.headers;
                    return super.parseNetworkResponse(response);
                }
            };

            return;
        }

        if (httpRequestParams.getParams() != null &&
                httpRequestParams.getHeaders() == null &&
                httpRequestParams.getBody() == null) {

            stringRequest = new StringRequest(method, url, listener, errorListener) {

                @Override
                protected Map<String, String> getParams() {
                    return httpRequestParams != null ? httpRequestParams.getParams() : null;
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    statusCode = response.statusCode;
                    headers = response.headers;
                    return super.parseNetworkResponse(response);
                }
            };

            return;
        }

        if (httpRequestParams.getParams() == null &&
                httpRequestParams.getHeaders() != null &&
                httpRequestParams.getBody() == null) {

            stringRequest = new StringRequest(method, url, listener, errorListener) {

                @Override
                public Map<String, String> getHeaders() {
                    return httpRequestParams != null ? httpRequestParams.getHeaders() : null;
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    statusCode = response.statusCode;
                    headers = response.headers;
                    return super.parseNetworkResponse(response);
                }

            };

            return;
        }

        if (httpRequestParams.getParams() == null &&
                httpRequestParams.getHeaders() == null &&
                httpRequestParams.getBody() != null) {

            stringRequest = new StringRequest(method, url, listener, errorListener) {

                @Override
                public byte[] getBody() {
                    return httpRequestParams != null ? httpRequestParams.getBody() : null;
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    statusCode = response.statusCode;
                    headers = response.headers;
                    return super.parseNetworkResponse(response);
                }

            };

            return;
        }

        if (httpRequestParams.getParams() != null &&
                httpRequestParams.getHeaders() != null &&
                httpRequestParams.getBody() == null) {

            stringRequest = new StringRequest(method, url, listener, errorListener) {

                @Override
                protected Map<String, String> getParams() {
                    return httpRequestParams != null ? httpRequestParams.getParams() : null;
                }

                @Override
                public Map<String, String> getHeaders() {
                    return httpRequestParams != null ? httpRequestParams.getHeaders() : null;
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    statusCode = response.statusCode;
                    headers = response.headers;
                    return super.parseNetworkResponse(response);
                }

            };

            return;
        }

        stringRequest = new StringRequest(method, url, listener, errorListener) {

            @Override
            protected Map<String, String> getParams() {
                return httpRequestParams != null ? httpRequestParams.getParams() : null;
            }

            @Override
            public Map<String, String> getHeaders() {
                return httpRequestParams != null ? httpRequestParams.getHeaders() : null;
            }

            @Override
            public byte[] getBody() {
                return httpRequestParams != null ? httpRequestParams.getBody() : null;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                statusCode = response.statusCode;
                headers = response.headers;
                return super.parseNetworkResponse(response);
            }

        };
    }

    private void setOkHttpRequestBuilder(int method, String url) {

        okHttpRequestBuilder = new Request.Builder();
        okHttpRequestBuilder.url(url);

        if (httpRequestParams != null && httpRequestParams.getHeaders() != null) {
            for (Map.Entry<String, String> data : httpRequestParams.getHeaders().entrySet()) {
                okHttpRequestBuilder.addHeader(data.getKey(), data.getValue());
            }
        }

        RequestBody requestBody = null;

        if (httpRequestParams != null && httpRequestParams.getParams() != null) {
            FormBody.Builder formBody = new FormBody.Builder();
            for (Map.Entry<String, String> data : httpRequestParams.getParams().entrySet()) {
                formBody.add(data.getKey(), data.getValue());
            }
            requestBody = formBody.build();
        } else if (httpRequestParams != null && httpRequestParams.getBody() != null) {
            requestBody = RequestBody.create(httpRequestParams.getBody());
        } else if (method != Method.GET && method != Method.HEAD) {
            requestBody = RequestBody.create(new byte[0]);
        }

        okHttpRequestBuilder.method(getRequestMethod(method), requestBody);
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

    public abstract static class RequestCallback implements HttpRequestCallback {
    }

    private static class LoaderCallback implements LoaderManager.LoaderCallbacks<String> {

        private final AppCompatActivity activity;
        private final HttpRequest request;
        private final RequestCallback callback;

        LoaderCallback(AppCompatActivity activity, HttpRequest request, RequestCallback callback) {
            this.activity = activity;
            this.request = request;
            this.callback = callback;
        }

        @NonNull
        @Override
        public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
            return new AsyncNetLoader(activity, request, callback);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        }

        @Override
        public void onLoaderReset(@NonNull Loader<String> loader) {

        }
    }
}
