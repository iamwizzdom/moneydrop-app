package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Notification implements Serializable {

    private int id;
    private String reference, notice, dateTime;
    private final JSONObject noticeObject;

    public Notification(Context context, JSONObject transObject) throws JSONException {
        this.noticeObject = transObject;
        this.setId(transObject.getInt("id"));
        this.setReference(transObject.getString("uuid"));
        this.setNotice(transObject.getString("notice"));
        this.setDateTime(transObject.getString("date_time"));
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

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
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
