package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class LoanApplication {

    private int id, userID;
    private String reference, loanID, dueDate, dueDateShort, date, dateShort;
    private boolean granted, repaid, hasGranted;
    private Loan loan;
    private User applicant;
    private final JSONObject applicationObject;

    public LoanApplication(Context context, JSONObject applicationObject) throws JSONException {
        this.applicationObject = applicationObject;
        this.setId(applicationObject.getInt("id"));
        this.setReference(applicationObject.getString("uuid"));
        this.setLoanID(applicationObject.getString("loan_id"));
        this.setUserID(applicationObject.getInt("user_id"));
        this.setGranted(applicationObject.getBoolean("is_granted"));
        this.setRepaid(applicationObject.getBoolean("is_repaid"));
        this.setHasGranted(applicationObject.getBoolean("has_granted"));
        this.setDueDate(applicationObject.getString("due_date"));
        this.setDueDateShort(applicationObject.getString("due_date_short"));
        this.setDate(applicationObject.getString("date"));
        this.setDateShort(applicationObject.getString("date_short"));
        if (applicationObject.has("loan") && !Utility.castEmpty(applicationObject.getString("loan")).isEmpty())
            loan = new Loan(context, applicationObject.getJSONObject("loan"));
        if (applicationObject.has("applicant") && !Utility.castEmpty(applicationObject.getString("applicant")).isEmpty())
            applicant = new User(context, applicationObject.getJSONObject("applicant"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getLoanID() {
        return loanID;
    }

    public void setLoanID(String loanID) {
        this.loanID = loanID;
    }

    public Loan getLoan() {
        return loan;
    }

    public User getApplicant() {
        return applicant;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDueDateShort() {
        return dueDateShort;
    }

    public void setDueDateShort(String dueDateShort) {
        this.dueDateShort = dueDateShort;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateShort() {
        return dateShort;
    }

    public void setDateShort(String dateShort) {
        this.dateShort = dateShort;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    public boolean isRepaid() {
        return repaid;
    }

    public void setRepaid(boolean repaid) {
        this.repaid = repaid;
    }

    public boolean isHasGranted() {
        return hasGranted;
    }

    public void setHasGranted(boolean hasGranted) {
        this.hasGranted = hasGranted;
    }

    public JSONObject getApplicationObject() {
        return applicationObject;
    }
}
