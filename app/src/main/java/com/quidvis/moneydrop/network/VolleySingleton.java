package com.quidvis.moneydrop.network;

import android.annotation.SuppressLint;
import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Wisdom Emenike.
 * Date: 5/9/2018
 * Time: 1:00 AM
 */

@SuppressWarnings("ALL")
public class VolleySingleton extends Application {

    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }


    public static synchronized VolleySingleton getInstance() {
        return mInstance == null ? (mInstance = new VolleySingleton()) : mInstance;
    }


    private RequestQueue getRequestQueue() {
        // If RequestQueue is null the initialize new RequestQueue
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
//            mRequestQueue = Volley.newRequestQueue(this, new HurlStack(null, sslSocketFactory()));
        }

        // Return RequestQueue
        return mRequestQueue;
    }


    public <T> void addToRequestQueue(Request<T> request) {
        // Add the specified request to the request queue
        getRequestQueue().add(request);
    }

    public SSLSocketFactory sslSocketFactory() {

        //Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        //
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        //
                    }
                }
        };

        //Install the all-trusting trust manager
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException|NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return sc.getSocketFactory();
    }

}
