package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class Loan {

    private int id;
    private double amount, interest;
    private String uuid, loanType, interestType, tenure, purpose, note, status, date, dateTime;
    private boolean isFundRaiser, isMine, isGranted, hasApplied;
    private User user;
    private final JSONObject loanObject;

    public Loan(Context context, JSONObject loanObject) throws JSONException {
        this.loanObject = loanObject;
        this.setId(loanObject.getInt("id"));
        this.setUuid(loanObject.getString("uuid"));
        this.setLoanType(loanObject.getString("loan_type_readable"));
        this.setInterestType(loanObject.getString("interest_type_readable"));
        this.setAmount(loanObject.getDouble("amount"));
        this.setTenure(loanObject.getString("tenure_readable"));
        this.setPurpose(loanObject.getString("purpose_readable"));
        this.setInterest(loanObject.getDouble("interest"));
        this.setNote(loanObject.getString("note"));
        this.setStatus(loanObject.getString("status_readable"));
        this.setDate(loanObject.getString("date"));
        this.setDateTime(loanObject.getString("date_time"));
        this.setFundRaiser(loanObject.getBoolean("is_fund_raiser"));
        this.setMine(loanObject.getBoolean("is_mine"));
        this.setGranted(loanObject.getBoolean("is_granted"));
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isLoanOffer() {
        return getLoanType().equalsIgnoreCase("offer");
    }

    public boolean isLoanRequest() {
        return getLoanType().equalsIgnoreCase("request");
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

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isFundRaiser() {
        return isFundRaiser;
    }

    public void setFundRaiser(boolean fundRaiser) {
        isFundRaiser = fundRaiser;
    }

    public boolean isPending() {
        return getStatus().equalsIgnoreCase("pending");
    }

    public boolean isAwaiting() {
        return getStatus().equalsIgnoreCase("awaiting");
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public boolean isGranted() {
        return isGranted;
    }

    public void setGranted(boolean granted) {
        isGranted = granted;
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
