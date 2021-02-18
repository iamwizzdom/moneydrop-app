package com.quidvis.moneydrop.model;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Notification implements Serializable {

    private int id;
    private String reference, message, image, activity, payload, dateTime;
    private final JSONObject noticeObject;

    public Notification(Context context, JSONObject notificationObject) throws JSONException {
        this.noticeObject = notificationObject;
        this.setId(notificationObject.getInt("id"));
        this.setReference(notificationObject.getString("uuid"));
        this.setMessage(notificationObject.getString("message"));
        this.setImage(notificationObject.getString("image"));
        this.setActivity(notificationObject.getString("activity"));
        this.setPayload(notificationObject.getString("payload"));
        this.setDateTime(notificationObject.getString("date_time"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public JSONObject getNoticeObject() {
        return noticeObject;
    }
}
