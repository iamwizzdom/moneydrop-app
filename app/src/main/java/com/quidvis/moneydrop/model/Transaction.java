package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Transaction {

    private int id;
    private double amount, fees;
    private String reference, type, direction, currency, narration, status, date, dateTime;
    private Card card;
    private User user;
    private final JSONObject transObject;

    public Transaction(Context context, JSONObject transObject) throws JSONException {
        this.transObject = transObject;
        this.setId(transObject.getInt("id"));
        this.setReference(transObject.getString("uuid"));
        this.setType(transObject.getString("type_readable"));
        this.setDirection(transObject.getString("direction_readable"));
        this.setAmount(transObject.getDouble("amount"));
        this.setFees(transObject.getDouble("fees"));
        this.setCurrency(transObject.getString("currency"));
        this.setNarration(transObject.getString("narration"));
        this.setStatus(transObject.getString("status_readable"));
        this.setDate(transObject.getString("date"));
        this.setDateTime(transObject.getString("date_time"));
        if (transObject.has("card") && !Utility.castEmpty(transObject.getString("card")).isEmpty())
            card = new Card(context, transObject.getJSONObject("card"));
        if (transObject.has("user") && !Utility.castEmpty(transObject.getString("user")).isEmpty())
            user = new User(context, transObject.getJSONObject("user"));
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

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public JSONObject getTransObject() {
        return transObject;
    }
}
