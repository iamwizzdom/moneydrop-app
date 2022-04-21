package com.quidvis.moneydrop.network;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;

public class RetryInterceptor implements Interceptor {

    public int maxRetry;
    private int retryNum;

    public RetryInterceptor(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    @NonNull
    @Override
    public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        okhttp3.Response response = chain.proceed(request);
        while (!response.isSuccessful() && retryNum < maxRetry) {
            retryNum++;
            response.close();
            response = chain.proceed(request);
        }
        return response;
    }
}
