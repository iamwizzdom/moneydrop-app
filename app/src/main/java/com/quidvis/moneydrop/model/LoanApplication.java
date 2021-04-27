package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class LoanApplication {

    private int id, userID;
    private double amount, repaidAmount, payableAmount, unpaidAmount;
    private String reference, loanID, dueDate, dueDateShort, date, dateShort, dateGranted, status;
    private boolean repaid, reviewed, hasGranted;
    private Loan loan;
    private User applicant;
    private final JSONObject applicationObject;

    public LoanApplication(Context context, JSONObject applicationObject) throws JSONException {
        this.applicationObject = applicationObject;
        this.setId(applicationObject.getInt("id"));
        this.setReference(applicationObject.getString("uuid"));
        this.setAmount(applicationObject.getDouble("amount"));
        this.setPayableAmount(applicationObject.getDouble("amount_payable"));
        this.setRepaidAmount(applicationObject.getDouble("repaid_amount"));
        this.setUnpaidAmount(applicationObject.getDouble("unpaid_amount"));
        this.setLoanID(applicationObject.getString("loan_id"));
        this.setUserID(applicationObject.getInt("user_id"));
        this.setStatus(applicationObject.getString("status_readable"));
        this.setRepaid(applicationObject.getBoolean("is_repaid"));
        this.setReviewed(applicationObject.getBoolean("is_reviewed"));
        this.setHasGranted(applicationObject.getBoolean("has_granted"));
        this.setDueDate(applicationObject.getString("due_at"));
        this.setDueDateShort(applicationObject.getString("due_date_short"));
        this.setDate(applicationObject.getString("date"));
        this.setDateShort(applicationObject.getString("date_short"));
        this.setDateGranted(Utility.castEmpty(applicationObject.getString("granted_date_short"), "Unavailable"));
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getRepaidAmount() {
        return repaidAmount;
    }

    public void setRepaidAmount(double repaidAmount) {
        this.repaidAmount = repaidAmount;
    }

    public double getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(double payableAmount) {
        this.payableAmount = payableAmount;
    }

    public double getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(double unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
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

    public void setLoan(Loan loan) {
        try {
            applicationObject.put("loan", loan.getLoanObject());
            this.loan = loan;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public User getApplicant() {
        return applicant;
    }

    public void setApplicant(User applicant) {
        try {
            applicationObject.put("applicant", applicant.getUserObject());
            this.applicant = applicant;
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    public String getDateGranted() {
        return dateGranted;
    }

    public void setDateGranted(String dateGranted) {
        this.dateGranted = dateGranted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isGranted() {
        return getStatus().equals("granted");
    }

    public boolean isAwaiting() {
        return getStatus().equals("awaiting");
    }

    public boolean isRejected() {
        return getStatus().equals("rejected");
    }

    public boolean isRepaid() {
        return repaid;
    }

    public void setRepaid(boolean repaid) {
        this.repaid = repaid;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
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
