package com.quidvis.moneydrop.network;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.quidvis.moneydrop.BuildConfig;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.ServiceCallback;
import com.quidvis.moneydrop.preference.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.quidvis.moneydrop.constant.Constant.HTTP_SUCCESS_CODE;
import static com.quidvis.moneydrop.constant.Constant.RETRY_IN_20_SEC;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NetService extends IntentService {

    private static String callBackFlag;
    private static ServiceCallback activityCallback;

    public final static String GET_COUNTRY_LIST = "country_list";

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public NetService getService() {
            return NetService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf(); return false;
    }

    public NetService() {
        super("NetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent == null) return;

        String action = intent.getAction();

        if (action == null) return;

        final DbHelper dbHelper;
        StringRequest stringRequest;
        RetryPolicy policy;
        final Session session;

        dbHelper = new DbHelper(getApplicationContext());


        switch (action) {
            case GET_COUNTRY_LIST:

                // Request a string response from the provided URL.
                stringRequest = new StringRequest(Request.Method.POST, URLContract.LOGIN_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String data) {
                                // Give back the response string.

                                if (activityCallback != null)
                                    activityCallback.toggleProgressBar(false);

                                if (data.isEmpty())
                                    if (activityCallback != null) {
                                        activityCallback.onErrorResponse();
                                        return;
                                    }

                                JSONObject response;

                                try {

                                    response = new JSONObject(data);

                                    String version = response.getString("version");

                                    if (activityCallback != null &&
                                            activityCallback.isOldVersion(version)) return;

                                    if (!response.getString("flag").equals(callBackFlag)) return;

                                    boolean status = response.getBoolean("status");
                                    int code = response.getInt("code");
                                    String message = response.getString("message");

                                    if (!status || code != HTTP_SUCCESS_CODE) {
                                        if (activityCallback != null) {
                                            activityCallback.toastMessage(message);
                                        }
                                        return;
                                    }

                                    if (activityCallback != null) {
                                        activityCallback.passJsonArrayResponse(
                                                response.getJSONArray("response")
                                        );
                                    }

                                } catch (JSONException e) {
                                    if (activityCallback != null) {
                                        activityCallback.onErrorResponse();
                                    }
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (activityCallback != null) {
                            activityCallback.toggleProgressBar(false);
                            activityCallback.onErrorResponse();
                        }
                        error.printStackTrace();
                    }
                }) {
                    //adding headers to the request
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        String auth = "Basic " + Base64.encodeToString(Constant.SERVER_CREDENTIAL.getBytes(), Base64.NO_WRAP);
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", auth);
                        headers.put("X-Version", BuildConfig.VERSION_NAME);
                        headers.put("X-Flag", callBackFlag);
                        return headers;
                    }
                };

                // Add the request to the RequestQueue.// Add the request to the RequestQueue.
                policy = new DefaultRetryPolicy(RETRY_IN_20_SEC, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                stringRequest.setRetryPolicy(policy);
                // Add the request to the RequestQueue.
                VolleySingleton.getInstance().addToRequestQueue(stringRequest);

                break;
        }

        dbHelper.close();

    }

    public void setActivityCallback(ServiceCallback callbacks, String flag) {
        activityCallback = callbacks;
        callBackFlag = flag;
    }

}

