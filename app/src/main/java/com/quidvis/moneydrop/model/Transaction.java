package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Transaction implements Serializable {

    private int id;
    private double amount, fees;
    private String reference, type, direction, currency, comment, status, date, dateTime;
    private Card card;
    private final JSONObject transObject;

    public Transaction(Context context, JSONObject transObject) throws JSONException {
        this.transObject = transObject;
        this.setId(transObject.getInt("id"));
        this.setReference(transObject.getString("uuid"));
        this.setType(transObject.getString("type"));
        this.setDirection(transObject.getString("direction"));
        this.setAmount(transObject.getDouble("amount"));
        this.setFees(transObject.getDouble("fees"));
        this.setCurrency(transObject.getString("currency"));
        this.setComment(transObject.getString("comment"));
        this.setStatus(transObject.getString("status"));
        this.setDate(transObject.getString("date"));
        this.setDateTime(transObject.getString("date_time"));
        if (transObject.has("card") && !Utility.isEmpty(transObject.getString("card")).isEmpty())
            card = new Card(context, transObject.getJSONObject("card"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getFees() {
        return fees;
    }

    public void setFees(double fees) {
        this.fees = fees;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public JSONObject getTransObject() {
        return transObject;
    }
}
