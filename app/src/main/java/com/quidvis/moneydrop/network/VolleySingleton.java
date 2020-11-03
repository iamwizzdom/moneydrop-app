package com.quidvis.moneydrop.network;

import android.annotation.SuppressLint;
import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Wisdom Emenike.
 * Date: 5/9/2018
 * Time: 1:00 AM
 */

@SuppressWarnings("ALL")
public class VolleySingleton extends Application {
    @SuppressLint("StaticFieldLeak")
    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }


    public static synchronized VolleySingleton getInstance(){
        return mInstance == null ? (mInstance = new VolleySingleton()) : mInstance;
    }


    private RequestQueue getRequestQueue(){
        // If RequestQueue is null the initialize new RequestQueue
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(this);
        }

        // Return RequestQueue
        return mRequestQueue;
    }



    public<T> void addToRequestQueue(Request<T> request){
        // Add the specified request to the request queue
        getRequestQueue().add(request);
    }
}
