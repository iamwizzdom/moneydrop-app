package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import static com.quidvis.moneydrop.constant.Constant.FEMALE;
import static com.quidvis.moneydrop.constant.Constant.MALE;

/**
 * Created by Wisdom Emenike.
 * Date: 5/24/2020
 * Time: 11:38 PM
 */
public class User {

    private final DbHelper dbHelper;
    
    private int id, gender, status;
    private boolean verifiedEmail, verifiedPhone;
    private String uuid, firstname, middlename, lastname, email, phone, bvn,
            dob, address, country, state, picture, token;
    private JSONObject userObject;
    
    public User(Context context) {
        this.dbHelper = new DbHelper(context);
    }

    public User(Context context, JSONObject userObject) throws JSONException {
        this.dbHelper = new DbHelper(context);
        this.userObject = userObject;
        this.setUuid(userObject.getString("uuid"));
        this.setFirstname(userObject.getString("firstname"));
        this.setMiddlename(userObject.getString("middlename"));
        this.setLastname(userObject.getString("lastname"));
        this.setPhone(userObject.getString("phone"));
        this.setEmail(userObject.getString("email"));
        this.setBvn(userObject.getString("bvn"));
        this.setPicture(userObject.getString("picture"));
        this.setDob(userObject.getString("dob"));
        this.setGender(userObject.getInt("gender"));
        this.setAddress(userObject.getString("address"));
        this.setCountry(userObject.getString("country"));
        this.setState(userObject.getString("state"));
        this.setStatus(userObject.getInt("status"));
        JSONObject verified = userObject.getJSONObject("verified");
        this.setVerifiedEmail(verified.getBoolean("email"));
        this.setVerifiedPhone(verified.getBoolean("phone"));
        if (userObject.has("token")) this.setToken(userObject.getString("token"));
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
        return getGender() == MALE ? R.drawable.male : (getGender() == FEMALE ? R.drawable.female : R.drawable.unisex);
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

    public boolean isMe() {
        return getUuid().equals(dbHelper.getUser().getUuid());
    }

    public JSONObject getUserObject() {
        return userObject;
    }

    public boolean update() {
        return dbHelper.updateUser(this);
    }
}
