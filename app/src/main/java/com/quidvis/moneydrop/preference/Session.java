package com.quidvis.moneydrop.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Wisdom Emenike.
 * Date: 6/17/2017
 * Time: 12:34 AM
 */

public class Session {

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public Session(Context context){
        this.prefs = context.getSharedPreferences("moneydrop_session", Context.MODE_PRIVATE);
        this.editor = prefs.edit();
    }

    public void setLoggedIn(boolean loggedIn){
        this.editor.putBoolean("loggedIn", loggedIn);
        this.editor.commit();
    }

    public boolean isLoggedIn(){
        return prefs.getBoolean("loggedIn", false);
    }

    public void setAccountInfo(JSONObject accountInfo) {
        this.editor.putString("accountInfo", accountInfo.toString());
        this.editor.commit();
    }

    public JSONObject getAccountInfo(){
        JSONObject defaultValue = new JSONObject();
        try {
            return new JSONObject(prefs.getString("accountInfo", defaultValue.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }
}