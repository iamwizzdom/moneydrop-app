package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.database.DbHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class Country {

    private final DbHelper dbHelper;

    private int id, uid;
    private String name, region, dialCode, iso, iso3, currencyName, currencyCode;
    private JSONObject countryObject;

    public Country(Context context) {
        this.dbHelper = new DbHelper(context);
    }

    public Country(Context context, JSONObject countryObject) throws JSONException {
        this.dbHelper = new DbHelper(context);
        setValues(countryObject);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDialCode() {
        return dialCode;
    }

    public void setDialCode(String dialCode) {
        this.dialCode = dialCode;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getIso3() {
        return iso3;
    }

    public void setIso3(String iso3) {
        this.iso3 = iso3;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public void setValues(JSONObject countryObject) throws JSONException {
        this.countryObject = countryObject;
        this.setUid(countryObject.getInt("id"));
        this.setName(countryObject.getString("name"));
        this.setRegion(countryObject.getString("region"));
        this.setDialCode(countryObject.getString("dial_code"));
        this.setIso(countryObject.getString("iso"));
        this.setIso3(countryObject.getString("iso3"));
        this.setCurrencyName(countryObject.getString("currency_name"));
        this.setCurrencyCode(countryObject.getString("currency_code"));
    }

    public boolean update() {
        return dbHelper.updateCountry(this);
    }

    public JSONObject getCountryObject() {
        return countryObject;
    }
}
