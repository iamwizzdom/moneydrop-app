package com.quidvis.moneydrop.model;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.RecyclerViewItem;

import java.io.Serializable;

//@Entity(tableName = "bank_accounts")
public class BankAccount implements RecyclerViewItem, Serializable {

    private final DbHelper dbHelper;

//    @PrimaryKey
//    @ColumnInfo(name = "id")
//    @SerializedName("id")
    private int id;

//    @ColumnInfo(name = "uuid")
//    @SerializedName("uuid")
    private String uuid;

//    @ColumnInfo(name = "bank_name", index = true)
//    @SerializedName("bank_name")
    private String bankName;

//    @ColumnInfo(name = "account_name", index = true)
//    @SerializedName("account_name")
    private String accountName;

//    @ColumnInfo(name = "account_number", index = true)
//    @SerializedName("account_number")
    private String accountNumber;

//    @ColumnInfo(name = "recipient_code", index = true)
//    @SerializedName("recipient_code")
    private String recipientCode;

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

    @Override
    public int getItemType() {
        return VIEW_TYPE_ITEM;
    }
}
