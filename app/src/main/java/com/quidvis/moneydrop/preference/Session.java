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

    public void setJSONObject(String key, JSONObject data) {
        this.editor.putString(key, data.toString());
        this.editor.commit();
    }

    public void setJSONArray(String key, JSONArray data) {
        this.editor.putString(key, data.toString());
        this.editor.commit();
    }

    public JSONObject getJSONObject(String key) {
        try {
            return new JSONObject(Objects.requireNonNull(prefs.getString(key, "")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONArray getJSONArray(String key) {
        try {
            return new JSONArray(Objects.requireNonNull(prefs.getString(key, "")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean clearAll() {
        return editor.clear().commit();
    }
}