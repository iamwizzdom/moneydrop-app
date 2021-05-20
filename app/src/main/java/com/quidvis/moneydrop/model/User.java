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

    private final Context context;
    private final DbHelper dbHelper;
    
    private int id, gender, status;
    private double rating;
    private String uuid, firstname, middlename, lastname, email, phone, bvn,
            dob, address, picture, token;
    private Country country;
    private State state;
    private BankStatement bankStatement;
    private JSONObject userObject;
    
    public User(Context context) {
        this.context = context;
        this.dbHelper = new DbHelper(context);
    }

    public User(Context context, JSONObject userObject) throws JSONException {
        this.context = context;
        this.dbHelper = new DbHelper(context);
        setValues(userObject);
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

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getPicture() {
        return picture;
    }

    public int getDefaultPicture() {
        return getGender() == MALE ? R.drawable.male : (getGender() == FEMALE ? R.drawable.female : R.drawable.unisex);
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public BankStatement getBankStatement() {
        return bankStatement;
    }

    public void setBankStatement(BankStatement bankStatement) {
        this.bankStatement = bankStatement;
    }

    public void setValues(JSONObject userObject) throws JSONException {
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
        this.setRating(userObject.getDouble("rating"));
        this.setGender(userObject.getInt("gender"));
        this.setAddress(userObject.getString("address"));
        if (userObject.has("country") && !Utility.castEmpty(userObject.getString("country")).isEmpty())
            setCountry(new Country(context, userObject.getJSONObject("country")));
        else setCountry(null);
        if (userObject.has("state") && !Utility.castEmpty(userObject.getString("state")).isEmpty())
            setState(new State(context, userObject.getJSONObject("state")));
        else setState(null);
        this.setStatus(userObject.getInt("status"));
        if (userObject.has("bank_statement") && !Utility.castEmpty(userObject.getString("bank_statement")).isEmpty())
            setBankStatement(new BankStatement(userObject.getJSONObject("bank_statement")));
        else setBankStatement(null);

        if (userObject.has("token")) this.setToken(userObject.getString("token"));
    }

    public JSONObject getUserObject() {
        if (userObject != null) return userObject;
        JSONObject object = new JSONObject();
        try {
            object.put("uuid", uuid);
            object.put("firstname", firstname);
            object.put("middlename", middlename);
            object.put("lastname", lastname);
            object.put("phone", phone);
            object.put("email", email);
            object.put("bvn", bvn);
            object.put("picture", picture);
            object.put("dob", dob);
            object.put("rating", rating);
            object.put("gender", gender);
            object.put("address", address);
            if (country != null) object.put("country", country.getCountryObject());
            if (state != null) object.put("state", state.getStateObject());
            object.put("status", status);
            if (bankStatement != null) object.put("bank_statement", bankStatement.getStatementObject());
            if (token != null) object.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public boolean update() {
        return dbHelper.updateUser(this);
    }
}
