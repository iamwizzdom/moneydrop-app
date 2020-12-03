package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;

/**
 * Created by Wisdom Emenike.
 * Date: 5/24/2020
 * Time: 11:38 PM
 */
public class User {

    private final DbHelper dbHelper;

    public final static int GENDER_MALE = 1, GENDER_FEMALE = 2;
    
    private int id, gender, status;
    private boolean verifiedEmail, verifiedPhone;
    private String firstname, middlename, lastname, email, phone, bvn,
            dob, address, country, state, picture, token;
    
    public User(Context context) {
        this.dbHelper = new DbHelper(context);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBvn() {
        return bvn;
    }

    public void setBvn(String bvn) {
        this.bvn = bvn;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPicture() {
        return picture;
    }

    public String getPictureUrl() {
        return (URLContract.URL_SCHEME + URLContract.HOST_URL + "/" + picture);
    }

    public int getDefaultPicture() {
        return getGender() == User.GENDER_MALE ? R.drawable.male : R.drawable.female;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isVerifiedEmail() {
        return verifiedEmail;
    }

    public void setVerifiedEmail(boolean verifiedEmail) {
        this.verifiedEmail = verifiedEmail;
    }

    public boolean isVerifiedPhone() {
        return verifiedPhone;
    }

    public void setVerifiedPhone(boolean verifiedPhone) {
        this.verifiedPhone = verifiedPhone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    public boolean update() {
        return dbHelper.updateUser(this);
    }
}
