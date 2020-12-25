package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.database.DbHelper;

public class Bank {

    private final DbHelper dbHelper;

    private int id, uid;
    private String name, code;

    public Bank(Context context) {
        this.dbHelper = new DbHelper(context);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean update() {
//        return dbHelper.updateBankAccount(this);
        return false;
    }
}
