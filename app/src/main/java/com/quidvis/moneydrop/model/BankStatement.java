package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.constant.URLContract;

import org.json.JSONException;
import org.json.JSONObject;

public class BankStatement {

    private int id;
    private String uuid, file, fileName, expiration;
    private boolean expired;
    private final JSONObject statementObject;

    public BankStatement(JSONObject statementObject) throws JSONException {
        this.statementObject = statementObject;
        setValues(statementObject);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFile() {
        return file;
    }

    public String getFileUrl() {
        return (URLContract.BASE_URL + "/" + file);
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public void setValues(JSONObject statementObject) throws JSONException {
        this.setId(statementObject.getInt("id"));
        this.setUuid(statementObject.getString("uuid"));
        this.setFile(statementObject.getString("file"));
        this.setFileName(statementObject.getString("file_name"));
        this.setExpiration(statementObject.getString("expiration"));
        this.setExpired(statementObject.getBoolean("is_expired"));
    }

    public JSONObject getStatementObject() {
        return statementObject;
    }
}
