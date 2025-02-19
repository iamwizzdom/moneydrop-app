package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.database.DbHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class Card {

    private final DbHelper dbHelper;

    private int id;
    private String uuid, name, brand, lastFourDigits, expMonth, expYear;

    public Card(Context context) {
        this.dbHelper = new DbHelper(context);
    }

    public Card(Context context, JSONObject cardObject) throws JSONException {
        this.dbHelper = new DbHelper(context);
        this.setUuid(cardObject.getString("uuid"));
        this.setName(cardObject.getString("name"));
        this.setLastFourDigits(cardObject.getString("last4digits"));
        this.setBrand(cardObject.getString("brand"));
        this.setExpMonth(cardObject.getString("exp_month"));
        this.setExpYear(cardObject.getString("exp_year"));
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public String getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(String expMonth) {
        this.expMonth = expMonth;
    }

    public String getExpYear() {
        return expYear;
    }

    public void setExpYear(String expYear) {
        this.expYear = expYear;
    }

    public boolean update() {
        return dbHelper.updateCard(this);
    }
}
