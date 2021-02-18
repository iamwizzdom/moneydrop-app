package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class Review {

    private int id;
    private String review, date, dateFormatted;
    private User user, reviewer;
    private LoanApplication loanApplication;
    private final JSONObject reviewObject;

    public Review(Context context, JSONObject reviewObject) throws JSONException {
        this.reviewObject = reviewObject;
        this.setId(reviewObject.getInt("id"));
        this.setReview(reviewObject.getString("review"));
        this.setDate(reviewObject.getString("date"));
        this.setDateFormatted(reviewObject.getString("date_formatted"));
        if (reviewObject.has("application") && !Utility.castEmpty(reviewObject.getString("application")).isEmpty())
            loanApplication = new LoanApplication(context, reviewObject.getJSONObject("application"));
        if (reviewObject.has("user") && !Utility.castEmpty(reviewObject.getString("user")).isEmpty())
            user = new User(context, reviewObject.getJSONObject("user"));
        if (reviewObject.has("reviewer") && !Utility.castEmpty(reviewObject.getString("reviewer")).isEmpty())
            reviewer = new User(context, reviewObject.getJSONObject("reviewer"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateFormatted() {
        return dateFormatted;
    }

    public void setDateFormatted(String dateFormatted) {
        this.dateFormatted = dateFormatted;
    }

    public User getUser() {
        return user;
    }

    public User getReviewer() {
        return reviewer;
    }

    public LoanApplication getLoanApplication() {
        return loanApplication;
    }

    public JSONObject getReviewObject() {
        return reviewObject;
    }
}
