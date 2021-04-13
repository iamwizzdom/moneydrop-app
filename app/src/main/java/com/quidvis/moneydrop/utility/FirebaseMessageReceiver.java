package com.quidvis.moneydrop.utility;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.FirebaseConfig;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.network.VolleySingleton;
import com.quidvis.moneydrop.preference.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FirebaseMessageReceiver extends FirebaseMessagingService {

//    private static final String TAG = FirebaseMessageReceiver.class.getSimpleName();
    private NotificationUtils notificationUtils;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        sendRegistrationToServer(getApplicationContext(), token);

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(FirebaseConfig.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

//        RemoteMessage.Notification notification = remoteMessage.getNotification();

        // Check if message contains a notification payload.
//        if (notification != null) handleNotification(notification.getBody());

        Map<String, String> data = remoteMessage.getData();

        // Check if message contains a data payload.
        if (data.size() > 0) {
            try {
                JSONObject json = new JSONObject(data);
                handleDataMessage(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleNotification(String message) {
        if (NotificationUtils.isAppIsInForeground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(FirebaseConfig.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        }
    }

    private void handleDataMessage(JSONObject json) {

        try {

            String title = json.getString("title");
            String message = json.getString("message");
            boolean isBackground = json.getBoolean("is_background");
            String imageUrl = getFullImageUrl(json.getString("image"));
            String timestamp = json.getString("timestamp");
            JSONObject payload = new JSONObject(json.getString("payload"));

//            if (NotificationUtils.isAppIsInForeground(getApplicationContext())) {
//                // app is in foreground, broadcast the push message
//                Intent pushNotification = new Intent(FirebaseConfig.PUSH_NOTIFICATION);
//                pushNotification.putExtra("message", message);
//                pushNotification.putExtra("payload", payload.toString());
//                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
//
//                // play notification sound
//                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
//                notificationUtils.playNotificationSound();
//
//            } else {
//
//            }

            // app is in background, show the notification in notification tray

            Intent resultIntent = NotificationIntent.getIntent(getApplicationContext(), payload.getString("activity"), payload.getString("data"));

            // check for image attachment
            if (TextUtils.isEmpty(imageUrl)) {
                showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
            } else {
                // image is present, show notification with image
                showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }

    public static void storeRegIdInPref(Session session, String token) {
        session.setFirebaseToken(token);
    }

    public static void sendRegistrationToServer(Context context, final String token) {
        // sending gcm token to server
        DbHelper dbHelper = new DbHelper(context);
        Session session = new Session(context);

        if (!session.isLoggedIn()) {
            storeRegIdInPref(session, token);
            return;
        }

        VolleySingleton.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST,
                String.format(URLContract.PROFILE_UPDATE_REQUEST_URL, "pn_token"),
                response -> {
                    try {

                        JSONObject object = new JSONObject(response);

                        if (object.getBoolean("status")) storeRegIdInPref(session, token);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("pn_token", token);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Auth-Token", dbHelper.getUser().getToken());
                params.put("Authorization", String.format("Basic %s", Base64.encodeToString(Constant.SERVER_CREDENTIAL.getBytes(), Base64.NO_WRAP)));
                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        });
    }

    private String getFullImageUrl(String imageUri) {
        if (TextUtils.isEmpty(imageUri)) return imageUri;
        return (URLContract.URL_SCHEME + URLContract.HOST_URL + "/" + imageUri);
    }
}
