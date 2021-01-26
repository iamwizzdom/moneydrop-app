package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class Loan {

    private int id;
    private double amount, interest;
    private String reference, loanType, interestType, tenure, purpose, note, status, date;
    private boolean isFundRaiser, isMine, hasApplied;
    private User user;
    private final JSONObject loanObject;

    public Loan(Context context, JSONObject loanObject) throws JSONException {
        this.loanObject = loanObject;
        this.setId(loanObject.getInt("id"));
        this.setReference(loanObject.getString("uuid"));
        this.setLoanType(loanObject.getString("loan_type_readable"));
        this.setInterestType(loanObject.getString("interest_type_readable"));
        this.setAmount(loanObject.getDouble("amount"));
        this.setTenure(loanObject.getString("tenure_readable"));
        this.setPurpose(loanObject.getString("purpose_readable"));
        this.setInterest(loanObject.getDouble("interest"));
        this.setNote(loanObject.getString("note"));
        this.setStatus(loanObject.getString("status_readable"));
        this.setDate(loanObject.getString("date"));
        this.setFundRaiser(loanObject.getBoolean("is_fund_raiser"));
        this.setMine(loanObject.getBoolean("is_mine"));
        this.setHasApplied(loanObject.getBoolean("has_applied"));
        if (loanObject.has("user") && !Utility.castEmpty(loanObject.getString("user")).isEmpty())
            user = new User(context, loanObject.getJSONObject("user"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isLoanOffer() {
        return getLoanType().equals("offer");
    }

    public boolean isLoanRequest() {
        return getLoanType().equals("request");
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public String getInterestType() {
        return interestType;
    }

    public void setInterestType(String interestType) {
        this.interestType = interestType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTenure() {
        return tenure;
    }

    public void setTenure(String tenure) {
        this.tenure = tenure;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public boolean isFundRaiser() {
        return isFundRaiser;
    }

    public void setFundRaiser(boolean fundRaiser) {
        isFundRaiser = fundRaiser;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public boolean isHasApplied() {
        return hasApplied;
    }

    public void setHasApplied(boolean hasApplied) {
        this.hasApplied = hasApplied;
    }

    public User getUser() {
        return user;
    }

    public JSONObject getLoanObject() {
        return loanObject;
    }
}
