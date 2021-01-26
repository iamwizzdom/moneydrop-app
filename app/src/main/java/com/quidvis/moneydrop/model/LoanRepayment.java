package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class LoanRepayment {

    private int id;
    private double amount;
    private String reference, applicationReference, date;
    private Transaction transaction;
    private User payer;
    private final JSONObject repaymentObject;

    public LoanRepayment(Context context, JSONObject repaymentObject) throws JSONException {
        this.repaymentObject = repaymentObject;
        this.setId(repaymentObject.getInt("id"));
        this.setReference(repaymentObject.getString("uuid"));
        this.setApplicationReference(repaymentObject.getString("application_id"));
        this.setAmount(repaymentObject.getDouble("amount"));
        this.setDate(repaymentObject.getString("date"));
        if (repaymentObject.has("transaction") && !Utility.castEmpty(repaymentObject.getString("transaction")).isEmpty())
            transaction = new Transaction(context, repaymentObject.getJSONObject("transaction"));
        if (repaymentObject.has("payer") && !Utility.castEmpty(repaymentObject.getString("payer")).isEmpty())
            payer = new User(context, repaymentObject.getJSONObject("payer"));
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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getApplicationReference() {
        return applicationReference;
    }

    public void setApplicationReference(String applicationReference) {
        this.applicationReference = applicationReference;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public User getPayer() {
        return payer;
    }

    public JSONObject getRepaymentObject() {
        return repaymentObject;
    }
}
