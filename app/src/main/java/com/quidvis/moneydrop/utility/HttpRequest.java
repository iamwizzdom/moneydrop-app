package com.quidvis.moneydrop.utility;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.quidvis.moneydrop.activity.MainActivity;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.network.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Objects;

import static com.quidvis.moneydrop.constant.Constant.RETRY_IN_30_SEC;

/**
 * Created by Wisdom Emenike.
 * Date: 10/1/2018
 * Time: 3:13 PM
 */
public abstract class HttpRequest {

    private final String url;
    private final int method;
    private HttpRequestParams httpRequestParams;
    private StringRequest stringRequest;
    private Map<String, String> headers;
    private int statusCode;
    private Runnable runnable;
    private final Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    private static boolean ongoingTask = false;

    protected HttpRequest(String url, int method) {
        this.url = url;
        this.method = method;
    }

    protected HttpRequest(String url, int method, HttpRequestParams httpRequestParams) {
        this.url = url;
        this.method = method;
        this.httpRequestParams = httpRequestParams;
    }

    protected HttpRequest(String url, HttpRequestParams httpRequestParams) {
        this.url = url;
        this.method = Method.POST;
        this.httpRequestParams = httpRequestParams;
    }

    protected abstract void onRequestStarted();

    protected abstract void onRequestCompleted(String response, int statusCode, Map<String, String> headers);

    protected abstract void onRequestError(String error, int statusCode, Map<String, String> headers);

    protected abstract void onRequestCancelled();

    public final void send(Activity activity) {

        if (!Validator.isNetworkConnected(activity)) {
            onRequestError("No network connection, check your connection and try again.", 503, null);
            return;
        }

        if (isOngoingTask()) {
            if (runnable == null) {
                Utility.toastMessage(activity, "There's an ongoing network operation, please wait.");
            } else handler.removeCallbacks(runnable);
            handler.postDelayed(runnable = (Runnable) () -> HttpRequest.this.send(activity), 500);
            return;
        }

        setOngoingTask(true);
        onRequestStarted();

        Response.Listener<String> listener = response -> {
            // Give back the response string.
            setOngoingTask(false);
            response = response.trim();
            if (statusCode == 419) {
                String title = "Auth Error", message = "Seems your authentication token has expired, please login again to continue.";
                try {
                    JSONObject object = new JSONObject(response);
                    title = object.getString("title");
                    message = object.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MainActivity.logout(activity, title, message);
                return;
            }
            onRequestCompleted(response, statusCode, headers);
        };

        Response.ErrorListener errorListener = error -> {
            NetworkResponse response = error.networkResponse;
            setOngoingTask(false);
            int statusCode = response != null ? response.statusCode : HttpURLConnection.HTTP_NO_CONTENT;
            String responseData = response != null ? new String(response.data) : "";
            responseData = responseData.trim();
            if (statusCode == 419) {
                String title = "Auth Error", message = "Seems your authentication token has expired, please login again to continue.";
                try {
                    JSONObject object = new JSONObject(responseData);
                    title = object.getString("title");
                    message = object.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MainActivity.logout(activity, title, message);
                return;
            }
            onRequestError(responseData, statusCode, headers);
            error.printStackTrace();
        };

        // Request a string response from the provided URL.
        if (httpRequestParams != null) {

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
            } else if (httpRequestParams.getParams() == null &&
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
            } else if (httpRequestParams.getParams() == null &&
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
            } else if (httpRequestParams.getParams() != null &&
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
            } else {

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

        } else stringRequest = new StringRequest(method, url, listener, errorListener) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                statusCode = response.statusCode;
                headers = response.headers;
                return super.parseNetworkResponse(response);
            }
        };


        // Add the request to the RequestQueue.// Add the request to the RequestQueue.
        RetryPolicy policy = new DefaultRetryPolicy(RETRY_IN_30_SEC, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        // Add the request to the RequestQueue.
        VolleySingleton.getInstance().addToRequestQueue(stringRequest);
    }

    public final void cancel() {
        setOngoingTask(false);
        if (stringRequest != null) stringRequest.cancel();
        onRequestCancelled();
    }

    private static boolean isOngoingTask() {
        return ongoingTask;
    }

    private static void setOngoingTask(boolean ongoingTask) {
        HttpRequest.ongoingTask = ongoingTask;
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
}
