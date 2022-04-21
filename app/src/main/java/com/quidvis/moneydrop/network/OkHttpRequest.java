package com.quidvis.moneydrop.network;

import java.util.Map;

import okhttp3.Request;

public class OkHttpRequest {
    private final Request.Builder request;
    private final Callback callback;
    private RetryInterceptor interceptor;
    private int timeout;

    public OkHttpRequest(Request.Builder request, Callback callback) {
        this.request = request;
        this.callback = callback;
    }

    public Request.Builder getRequest() {
        return request;
    }

    public Callback getCallback() {
        return callback;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public RetryInterceptor getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(RetryInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public static abstract class Callback {
        public abstract void onFailure();
        public abstract void onResponse(String response, int statusCode, Map<String, String> headers);
    }
}
