package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.database.DbHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class State {

    private final DbHelper dbHelper;

    private int id, uid, countryId;
    private String name, iso3166_2;
    private JSONObject stateObject;

    public State(Context context) {
        this.dbHelper = new DbHelper(context);
    }

    public State(Context context, JSONObject stateObject) throws JSONException {
        this.dbHelper = new DbHelper(context);
        setValues(stateObject);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIso3166_2() {
        return iso3166_2;
    }

    public void setIso3166_2(String iso3166_2) {
        this.iso3166_2 = iso3166_2;
    }

    public void setValues(JSONObject stateObject) throws JSONException {
        this.stateObject = stateObject;
        this.setUid(stateObject.getInt("id"));
        this.setCountryId(stateObject.getInt("country_id"));
        this.setName(stateObject.getString("name"));
        this.setIso3166_2(stateObject.getString("iso3166_2"));
    }

    public boolean update() {
        return dbHelper.updateState(this);
    }

    public JSONObject getStateObject() {
        return stateObject;
    }
}
