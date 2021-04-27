package com.quidvis.moneydrop.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Created by Wisdom Emenike.
 * Date: 6/17/2017
 * Time: 12:34 AM
 */

public class Session {

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public Session(Context context) {
        this.prefs = context.getSharedPreferences("moneydrop_session", Context.MODE_PRIVATE);
        this.editor = prefs.edit();
    }

    public void setLoggedIn(boolean loggedIn) {
        this.editor.putBoolean("loggedIn", loggedIn);
        this.editor.commit();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("loggedIn", false);
    }

    public void setCompletedCountryImport(boolean status) {
        this.editor.putBoolean("completedCountryImport", status);
        this.editor.commit();
    }

    public boolean hasCompletedCountryImport() {
        return prefs.getBoolean("completedCountryImport", false);
    }

    public void setCompletedStateImport(boolean status) {
        this.editor.putBoolean("completedStateImport", status);
        this.editor.commit();
    }

    public boolean hasCompletedStateImport() {
        return prefs.getBoolean("completedStateImport", false);
    }

    public void setFirstTimeLaunch(boolean launched) {
        this.editor.putBoolean("launched", launched);
        this.editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return prefs.getBoolean("launched", true);
    }

    public void setJSONObject(String key, JSONObject data) {
        this.editor.putString(key, data.toString());
        this.editor.commit();
    }

    public JSONObject getJSONObject(String key) {
        try {
            String value = prefs.getString(key, null);
            if (value == null) return null;
            return new JSONObject(value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setJSONArray(String key, JSONArray data) {
        this.editor.putString(key, data.toString());
        this.editor.commit();
    }

    public JSONArray getJSONArray(String key) {
        try {
            String value = prefs.getString(key, null);
            if (value == null) return null;
            return new JSONArray(value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setFirebaseToken(String token) {
        this.editor.putString("firebaseToken", token);
        this.editor.commit();
    }

    public String getFirebaseToken() {
        return prefs.getString("firebaseToken", "");
    }

    public void clearAll() {
        boolean isFirstTimeLaunch = isFirstTimeLaunch();
        boolean hasCompletedCountryImport = hasCompletedCountryImport();
        boolean hasCompletedStateImport = hasCompletedStateImport();
        boolean cleared = editor.clear().commit();
        if (cleared) {
            setFirstTimeLaunch(isFirstTimeLaunch);
            setCompletedCountryImport(hasCompletedCountryImport);
            setCompletedStateImport(hasCompletedStateImport);
        }
    }
}