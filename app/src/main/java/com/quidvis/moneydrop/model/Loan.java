package com.quidvis.moneydrop.model;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.constant.URLContract;

import static com.quidvis.moneydrop.constant.Constant.FEMALE;
import static com.quidvis.moneydrop.constant.Constant.MALE;

public class Loan {

    private int id, userGender;
    private double amount;
    private String picture, type, status, date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserGender() {
        return userGender;
    }

    public void setUserGender(int userGender) {
        this.userGender = userGender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

    public String getPicture() {
        return picture;
    }

    public String getPictureUrl() {
        return (URLContract.URL_SCHEME + URLContract.HOST_URL + "/" + picture);
    }

    public int getDefaultPicture() {
        return getUserGender() == MALE ? R.drawable.male : (getUserGender() == FEMALE ? R.drawable.female : R.drawable.unisex);
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
