package com.quidvis.moneydrop.constant;

public class FirebaseConfig {
    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 8310;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 8309;

    public static final String NOTIFICATION_CHANNEL_ID = "moneydrop_notification";

    public static final String SHARED_PREF = "moneydrop_firebase_pref";
}
