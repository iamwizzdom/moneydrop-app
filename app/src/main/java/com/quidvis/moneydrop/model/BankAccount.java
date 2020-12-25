package com.quidvis.moneydrop.model;

import android.content.Context;

import com.quidvis.moneydrop.database.DbHelper;

public class BankAccount {

    private final DbHelper dbHelper;

    private int id;
    private String uuid, bankName, accountName, accountNumber, recipientCode;

    public BankAccount(Context context) {
        this.dbHelper = new DbHelper(context);
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

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getRecipientCode() {
        return recipientCode;
    }

    public void setRecipientCode(String recipientCode) {
        this.recipientCode = recipientCode;
    }

    public boolean update() {
        return dbHelper.updateBankAccount(this);
    }
}
