package com.quidvis.moneydrop.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.quidvis.moneydrop.model.User;

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
}