package com.quidvis.moneydrop.network;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.constant.URLContract;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
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
//            mRequestQueue = Volley.newRequestQueue(this);
            mRequestQueue = Volley.newRequestQueue(this, new HurlStack(null, getSocketFactory()));
        }

        // Return RequestQueue
        return mRequestQueue;
    }


    public <T> void addToRequestQueue(Request<T> request) {
        // Add the specified request to the request queue
        getRequestQueue().add(request);
    }

    private SSLSocketFactory getSocketFactory() {

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            InputStream cert = getApplicationContext().getResources().openRawResource(R.raw.cert);
            Certificate ca = cf.generateCertificate(cert);
            cert.close();

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory ssf = sslContext.getSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory(ssf);

            return ssf;
        } catch (Exception e) {
            return null;
        }
    }

}
