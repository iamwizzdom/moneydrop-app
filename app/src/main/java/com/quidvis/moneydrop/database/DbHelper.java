package com.quidvis.moneydrop.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.quidvis.moneydrop.BuildConfig;
import com.quidvis.moneydrop.constant.DbContract;
import com.quidvis.moneydrop.model.Bank;
import com.quidvis.moneydrop.model.BankAccount;
import com.quidvis.moneydrop.model.Card;
import com.quidvis.moneydrop.model.User;

import java.util.ArrayList;

/**
 * Created by Wisdom Emenike.
 * Date: 6/17/2017
 * Time: 12:34 AM
 */

@SuppressWarnings("ALL")
public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = BuildConfig.VERSION_CODE;
    private Context context = null;

    private static final String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS "
            + DbContract.USERS_TABLE_NAME + "("
            + DbContract.USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DbContract.USER_UUID + " CHAR(36) NOT NULL,"
            + DbContract.USER_FIRSTNAME + " CHAR(100) NOT NULL,"
            + DbContract.USER_MIDDLENAME + " CHAR(100) NOT NULL,"
            + DbContract.USER_LASTNAME + " CHAR(100) NOT NULL,"
            + DbContract.USER_PHONE + " CHAR(30) NOT NULL,"
            + DbContract.USER_EMAIL + " CHAR(100) NOT NULL,"
            + DbContract.USER_PICTURE + " CHAR(500) NOT NULL,"
            + DbContract.USER_DOB + " DATE NOT NULL,"
            + DbContract.USER_GENDER + " INT NOT NULL,"
            + DbContract.USER_ADDRESS + " CHAR(500) NOT NULL,"
            + DbContract.USER_COUNTY + " CHAR(500) NOT NULL,"
            + DbContract.USER_STATE + " CHAR(500) NOT NULL,"
            + DbContract.USER_STATUS + " INT NOT NULL,"
            + DbContract.USER_BVN + " CHAR(20) NOT NULL,"
            + DbContract.USER_TOKEN + " CHAR(500) NOT NULL,"
            + DbContract.USER_VERIFIED_EMAIL + " INT NOT NULL,"
            + DbContract.USER_VERIFIED_PHONE + " INT NOT NULL"
            +");";

    private static final String CREATE_CARDS_TABLE = "CREATE TABLE IF NOT EXISTS "
            + DbContract.CARDS_TABLE_NAME + "("
            + DbContract.CARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DbContract.CARD_UUID + " CHAR(36) NOT NULL,"
            + DbContract.CARD_NAME + " CHAR(200),"
            + DbContract.CARD_TYPE + " CHAR(100) NOT NULL,"
            + DbContract.CARD_BRAND + " CHAR(100) NOT NULL,"
            + DbContract.CARD_LAST_FOUR_DIGITS + " CHAR(10) NOT NULL,"
            + DbContract.CARD_EXP_MONTH + " CHAR(10) NOT NULL,"
            + DbContract.CARD_EXP_YEAR + " CHAR(10) NOT NULL"
            +");";

    private static final String CREATE_BANKS_TABLE = "CREATE TABLE IF NOT EXISTS "
            + DbContract.BANKS_TABLE_NAME + "("
            + DbContract.BANK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DbContract.BANK_UID + " INTEGER NOT NULL,"
            + DbContract.BANK_NAME + " CHAR(200),"
            + DbContract.BANK_CODE + " CHAR(20)"
            +");";

    private static final String CREATE_BANK_ACCOUNTS_TABLE = "CREATE TABLE IF NOT EXISTS "
            + DbContract.BANK_ACCOUNTS_TABLE_NAME + "("
            + DbContract.BANK_ACCOUNT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DbContract.BANK_ACCOUNT_UUID + " CHAR(36) NOT NULL,"
            + DbContract.BANK_ACCOUNT_NAME + " CHAR(200),"
            + DbContract.BANK_ACCOUNT_BANK_NAME + " CHAR(200),"
            + DbContract.BANK_ACCOUNT_NUMBER + " CHAR(100) NOT NULL,"
            + DbContract.BANK_ACCOUNT_RECIPIENT + " CHAR(200) NOT NULL"
            +");";

    private static final String DROP_USERS_TABLE = "DROP TABLE IF EXISTS " + DbContract.USERS_TABLE_NAME;
    private static final String DROP_CARDS_TABLE = "DROP TABLE IF EXISTS " + DbContract.CARDS_TABLE_NAME;
    private static final String DROP_BANKS_TABLE = "DROP TABLE IF EXISTS " + DbContract.BANKS_TABLE_NAME;
    private static final String DROP_BANK_ACCOUNTS_TABLE = "DROP TABLE IF EXISTS " + DbContract.BANK_ACCOUNTS_TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DbContract.DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db);
        onCreate(db);
    }

    public void dropTables(SQLiteDatabase db) {
        db.execSQL(DROP_USERS_TABLE);
        db.execSQL(DROP_CARDS_TABLE);
        db.execSQL(DROP_BANKS_TABLE);
        db.execSQL(DROP_BANK_ACCOUNTS_TABLE);
    }

    public void createTables(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_CARDS_TABLE);
        db.execSQL(CREATE_BANKS_TABLE);
        db.execSQL(CREATE_BANK_ACCOUNTS_TABLE);
    }

    /**
     * Store user details in SQLite Database
     * @param user
     * @return
     */
    public boolean saveUser(User user) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.USER_UUID, user.getUuid());
        contentValues.put(DbContract.USER_FIRSTNAME, user.getFirstname());
        contentValues.put(DbContract.USER_MIDDLENAME, user.getMiddlename());
        contentValues.put(DbContract.USER_LASTNAME, user.getLastname());
        contentValues.put(DbContract.USER_EMAIL, user.getEmail());
        contentValues.put(DbContract.USER_PHONE, user.getPhone());
        contentValues.put(DbContract.USER_PICTURE, user.getPicture());
        contentValues.put(DbContract.USER_GENDER, user.getGender());
        contentValues.put(DbContract.USER_DOB, user.getDob());
        contentValues.put(DbContract.USER_ADDRESS, user.getAddress());
        contentValues.put(DbContract.USER_COUNTY, user.getCountry());
        contentValues.put(DbContract.USER_STATE, user.getState());
        contentValues.put(DbContract.USER_BVN, user.getBvn());
        contentValues.put(DbContract.USER_TOKEN, user.getToken());
        contentValues.put(DbContract.USER_STATUS, user.getStatus());
        contentValues.put(DbContract.USER_VERIFIED_EMAIL, user.isVerifiedEmail());
        contentValues.put(DbContract.USER_VERIFIED_PHONE, user.isVerifiedPhone());

        SQLiteDatabase database = this.getReadableDatabase();
        boolean result = database.insert(DbContract.USERS_TABLE_NAME, null, contentValues) > 0;
        database.close();
        return result;
    }

    /**
     * Store card details in SQLite Database
     * @param card
     * @return
     */
    public boolean saveCard(Card card) {

        if (cardExist(card.getUuid())) return updateCard(card);

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.CARD_UUID, card.getUuid());
        contentValues.put(DbContract.CARD_NAME, card.getName());
        contentValues.put(DbContract.CARD_BRAND, card.getBrand());
        contentValues.put(DbContract.CARD_TYPE, card.getCardType());
        contentValues.put(DbContract.CARD_EXP_MONTH, card.getExpMonth());
        contentValues.put(DbContract.CARD_EXP_YEAR, card.getExpYear());
        contentValues.put(DbContract.CARD_LAST_FOUR_DIGITS, card.getLastFourDigits());

        SQLiteDatabase database = this.getReadableDatabase();
        boolean result = database.insert(DbContract.CARDS_TABLE_NAME, null, contentValues) > 0;
        database.close();
        return result;
    }

    /**
     * Store bank details in SQLite Database
     * @param bank
     * @return
     */
    public boolean saveBank(Bank bank) {

        if (bankExist(bank.getUid())) return updateBank(bank);

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.BANK_UID, bank.getUid());
        contentValues.put(DbContract.BANK_NAME, bank.getName());
        contentValues.put(DbContract.BANK_CODE, bank.getCode());

        SQLiteDatabase database = this.getReadableDatabase();
        boolean result = database.insert(DbContract.BANKS_TABLE_NAME, null, contentValues) > 0;
        database.close();
        return result;
    }

    /**
     * Store bank account details in SQLite Database
     * @param account
     * @return
     */
    public boolean saveBankAccount(BankAccount account) {

        if (bankAccountExist(account.getUuid())) return updateBankAccount(account);

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.BANK_ACCOUNT_UUID, account.getUuid());
        contentValues.put(DbContract.BANK_ACCOUNT_NAME, account.getAccountName());
        contentValues.put(DbContract.BANK_ACCOUNT_BANK_NAME, account.getBankName());
        contentValues.put(DbContract.BANK_ACCOUNT_NUMBER, account.getAccountNumber());
        contentValues.put(DbContract.BANK_ACCOUNT_RECIPIENT, account.getRecipientCode());

        SQLiteDatabase database = this.getReadableDatabase();
        boolean result = database.insert(DbContract.BANK_ACCOUNTS_TABLE_NAME, null, contentValues) > 0;
        database.close();
        return result;
    }

    /**
     * Update user info in SQLite Database
     * @param user
     * @return
     */
    public boolean updateUser(User user) {

        if (user.getId() <= 0) return false;

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.USER_UUID, user.getUuid());
        contentValues.put(DbContract.USER_FIRSTNAME, user.getFirstname());
        contentValues.put(DbContract.USER_MIDDLENAME, user.getMiddlename());
        contentValues.put(DbContract.USER_LASTNAME, user.getLastname());
        contentValues.put(DbContract.USER_EMAIL, user.getEmail());
        contentValues.put(DbContract.USER_PHONE, user.getPhone());
        contentValues.put(DbContract.USER_PICTURE, user.getPicture());
        contentValues.put(DbContract.USER_GENDER, user.getGender());
        contentValues.put(DbContract.USER_DOB, user.getDob());
        contentValues.put(DbContract.USER_ADDRESS, user.getAddress());
        contentValues.put(DbContract.USER_COUNTY, user.getCountry());
        contentValues.put(DbContract.USER_STATE, user.getState());
        contentValues.put(DbContract.USER_BVN, user.getBvn());
        contentValues.put(DbContract.USER_TOKEN, user.getToken());
        contentValues.put(DbContract.USER_STATUS, user.getStatus());
        contentValues.put(DbContract.USER_VERIFIED_EMAIL, user.isVerifiedEmail());
        contentValues.put(DbContract.USER_VERIFIED_PHONE, user.isVerifiedPhone());

        if (contentValues.size() <= 0) return false;

        String selection = DbContract.USER_ID + " = ? ";
        String[] args = {String.valueOf(user.getId())};
        int affectedRows = database.update(DbContract.USERS_TABLE_NAME, contentValues, selection, args);
        database.close();
        return affectedRows > 0;
    }

    /**
     * Update card info in SQLite Database
     * @param card
     * @return
     */
    public boolean updateCard(Card card) {

        if (card.getId() <= 0) return false;

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.CARD_UUID, card.getUuid());
        contentValues.put(DbContract.CARD_NAME, card.getName());
        contentValues.put(DbContract.CARD_BRAND, card.getBrand());
        contentValues.put(DbContract.CARD_TYPE, card.getCardType());
        contentValues.put(DbContract.CARD_EXP_MONTH, card.getExpMonth());
        contentValues.put(DbContract.CARD_EXP_YEAR, card.getExpYear());
        contentValues.put(DbContract.CARD_LAST_FOUR_DIGITS, card.getLastFourDigits());

        if (contentValues.size() <= 0) return false;

        String selection = DbContract.CARD_ID + " = ? ";
        String[] args = {String.valueOf(card.getId())};
        int affectedRows = database.update(DbContract.CARDS_TABLE_NAME, contentValues, selection, args);
        database.close();
        return affectedRows > 0;
    }

    /**
     * Update bank info in SQLite Database
     * @param bank
     * @return
     */
    public boolean updateBank(Bank bank) {

        if (bank.getId() <= 0) return false;

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.BANK_UID, bank.getUid());
        contentValues.put(DbContract.BANK_NAME, bank.getName());
        contentValues.put(DbContract.BANK_CODE, bank.getCode());

        if (contentValues.size() <= 0) return false;

        String selection = DbContract.BANK_ID + " = ? ";
        String[] args = {String.valueOf(bank.getId())};
        int affectedRows = database.update(DbContract.BANKS_TABLE_NAME, contentValues, selection, args);
        database.close();
        return affectedRows > 0;
    }

    /**
     * Update bank account info in SQLite Database
     * @param account
     * @return
     */
    public boolean updateBankAccount(BankAccount account) {

        if (account.getId() <= 0) return false;

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.BANK_ACCOUNT_UUID, account.getUuid());
        contentValues.put(DbContract.BANK_ACCOUNT_NAME, account.getAccountName());
        contentValues.put(DbContract.BANK_ACCOUNT_BANK_NAME, account.getBankName());
        contentValues.put(DbContract.BANK_ACCOUNT_NUMBER, account.getAccountNumber());
        contentValues.put(DbContract.BANK_ACCOUNT_RECIPIENT, account.getRecipientCode());

        if (contentValues.size() <= 0) return false;

        String selection = DbContract.BANK_ACCOUNT_ID + " = ? ";
        String[] args = {String.valueOf(account.getId())};
        int affectedRows = database.update(DbContract.BANK_ACCOUNTS_TABLE_NAME, contentValues, selection, args);
        database.close();
        return affectedRows > 0;
    }

    /**
     * Get user info from SQLite Database
     * @return
     */
    public User getUser() {

        String[] projection = {
                DbContract.USER_ID,
                DbContract.USER_UUID,
                DbContract.USER_FIRSTNAME,
                DbContract.USER_MIDDLENAME,
                DbContract.USER_LASTNAME,
                DbContract.USER_EMAIL,
                DbContract.USER_PHONE,
                DbContract.USER_DOB,
                DbContract.USER_GENDER,
                DbContract.USER_PICTURE,
                DbContract.USER_ADDRESS,
                DbContract.USER_COUNTY,
                DbContract.USER_STATE,
                DbContract.USER_BVN,
                DbContract.USER_TOKEN,
                DbContract.USER_VERIFIED_EMAIL,
                DbContract.USER_VERIFIED_PHONE,
                DbContract.USER_STATUS
        };

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = (database.query(DbContract.USERS_TABLE_NAME, projection,
                null, null, null, null, null));

        User user = new User(context);

        if (cursor != null && cursor.moveToFirst()) {
            user.setId(cursor.getInt(cursor.getColumnIndex(DbContract.USER_ID)));
            user.setUuid(cursor.getString(cursor.getColumnIndex(DbContract.USER_UUID)));
            user.setFirstname(cursor.getString(cursor.getColumnIndex(DbContract.USER_FIRSTNAME)));
            user.setMiddlename(cursor.getString(cursor.getColumnIndex(DbContract.USER_MIDDLENAME)));
            user.setLastname(cursor.getString(cursor.getColumnIndex(DbContract.USER_LASTNAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndex(DbContract.USER_EMAIL)));
            user.setPhone(cursor.getString(cursor.getColumnIndex(DbContract.USER_PHONE)));
            user.setDob(cursor.getString(cursor.getColumnIndex(DbContract.USER_DOB)));
            user.setGender(cursor.getInt(cursor.getColumnIndex(DbContract.USER_GENDER)));
            user.setPicture(cursor.getString(cursor.getColumnIndex(DbContract.USER_PICTURE)));
            user.setAddress(cursor.getString(cursor.getColumnIndex(DbContract.USER_ADDRESS)));
            user.setCountry(cursor.getString(cursor.getColumnIndex(DbContract.USER_COUNTY)));
            user.setState(cursor.getString(cursor.getColumnIndex(DbContract.USER_STATE)));
            user.setBvn(cursor.getString(cursor.getColumnIndex(DbContract.USER_BVN)));
            user.setToken(cursor.getString(cursor.getColumnIndex(DbContract.USER_TOKEN)));
            user.setVerifiedEmail(cursor.getInt(cursor.getColumnIndex(DbContract.USER_VERIFIED_EMAIL)) == 1);
            user.setVerifiedPhone(cursor.getInt(cursor.getColumnIndex(DbContract.USER_VERIFIED_PHONE)) == 1);
            user.setStatus(cursor.getInt(cursor.getColumnIndex(DbContract.USER_STATUS)));
            cursor.close();
        }

        database.close();
        return user;
    }


    /**
     * Get cards info from SQLite Database
     * @return
     */
    public ArrayList<Card> getCards() {

        String[] projection = {
                DbContract.CARD_ID,
                DbContract.CARD_UUID,
                DbContract.CARD_NAME,
                DbContract.CARD_BRAND,
                DbContract.CARD_TYPE,
                DbContract.CARD_EXP_MONTH,
                DbContract.CARD_EXP_YEAR,
                DbContract.CARD_LAST_FOUR_DIGITS
        };

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(DbContract.CARDS_TABLE_NAME, projection,
                null, null, null, null, DbContract.CARD_ID + " DESC");

        ArrayList<Card> cards = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Card card = new Card(context);
                card.setId(cursor.getInt(cursor.getColumnIndex(DbContract.CARD_ID)));
                card.setUuid(cursor.getString(cursor.getColumnIndex(DbContract.CARD_UUID)));
                card.setName(cursor.getString(cursor.getColumnIndex(DbContract.CARD_NAME)));
                card.setBrand(cursor.getString(cursor.getColumnIndex(DbContract.CARD_BRAND)));
                card.setCardType(cursor.getString(cursor.getColumnIndex(DbContract.CARD_TYPE)));
                card.setExpMonth(cursor.getString(cursor.getColumnIndex(DbContract.CARD_EXP_MONTH)));
                card.setExpYear(cursor.getString(cursor.getColumnIndex(DbContract.CARD_EXP_YEAR)));
                card.setLastFourDigits(cursor.getString(cursor.getColumnIndex(DbContract.CARD_LAST_FOUR_DIGITS)));
                cards.add(card);
            }
            cursor.close();
        }

        database.close();
        return cards;
    }


    /**
     * Get banks info from SQLite Database
     * @return
     */
    public ArrayList<Bank> getBanks() {

        String[] projection = {
                DbContract.BANK_ID,
                DbContract.BANK_UID,
                DbContract.BANK_NAME,
                DbContract.BANK_CODE
        };

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(DbContract.BANKS_TABLE_NAME, projection,
                null, null, null, null, null);

        ArrayList<Bank> banks = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Bank bank = new Bank(context);
                bank.setId(cursor.getInt(cursor.getColumnIndex(DbContract.BANK_ID)));
                bank.setUid(cursor.getInt(cursor.getColumnIndex(DbContract.BANK_UID)));
                bank.setName(cursor.getString(cursor.getColumnIndex(DbContract.BANK_NAME)));
                bank.setCode(cursor.getString(cursor.getColumnIndex(DbContract.BANK_CODE)));
                banks.add(bank);
            }
            cursor.close();
        }

        database.close();
        return banks;
    }


    /**
     * Get bank accounts info from SQLite Database
     * @return
     */
    public ArrayList<BankAccount> getBankAccounts() {

        String[] projection = {
                DbContract.BANK_ACCOUNT_ID,
                DbContract.BANK_ACCOUNT_UUID,
                DbContract.BANK_ACCOUNT_NAME,
                DbContract.BANK_ACCOUNT_BANK_NAME,
                DbContract.BANK_ACCOUNT_NUMBER,
                DbContract.BANK_ACCOUNT_RECIPIENT,
        };

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(DbContract.BANK_ACCOUNTS_TABLE_NAME, projection,
                null, null, null, null, DbContract.BANK_ACCOUNT_ID + " DESC");

        ArrayList<BankAccount> accounts = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                BankAccount account = new BankAccount(context);
                account.setId(cursor.getInt(cursor.getColumnIndex(DbContract.BANK_ACCOUNT_ID)));
                account.setUuid(cursor.getString(cursor.getColumnIndex(DbContract.BANK_ACCOUNT_UUID)));
                account.setAccountName(cursor.getString(cursor.getColumnIndex(DbContract.BANK_ACCOUNT_NAME)));
                account.setBankName(cursor.getString(cursor.getColumnIndex(DbContract.BANK_ACCOUNT_BANK_NAME)));
                account.setAccountNumber(cursor.getString(cursor.getColumnIndex(DbContract.BANK_ACCOUNT_NUMBER)));
                account.setRecipientCode(cursor.getString(cursor.getColumnIndex(DbContract.BANK_ACCOUNT_RECIPIENT)));
                accounts.add(account);
            }
            cursor.close();
        }

        database.close();
        return accounts;
    }

    /**
     * check if card exist in SQLite Database
     * @param cardUUID
     * @return
     */
    public boolean cardExist(String cardUUID) {
        SQLiteDatabase database = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = database.rawQuery("SELECT * FROM " + DbContract.CARDS_TABLE_NAME + " WHERE "
                + DbContract.CARD_UUID + " =?", new String[]{cardUUID});

        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
            database.close();
            return count > 0;
        }
        return false;
    }

    /**
     * check if bank exist in SQLite Database
     * @param bankID
     * @return
     */
    public boolean bankExist(int bankID) {
        SQLiteDatabase database = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = database.rawQuery("SELECT * FROM " + DbContract.BANKS_TABLE_NAME + " WHERE "
                + DbContract.BANK_UID + " =?", new String[]{String.valueOf(bankID)});

        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
            database.close();
            return count > 0;
        }
        return false;
    }

    /**
     * check if bank account exist in SQLite Database
     * @param accountUUID
     * @return
     */
    public boolean bankAccountExist(String accountUUID) {
        SQLiteDatabase database = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = database.rawQuery("SELECT * FROM " + DbContract.BANK_ACCOUNTS_TABLE_NAME + " WHERE "
                + DbContract.BANK_ACCOUNT_UUID + " =?", new String[]{accountUUID});

        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
            database.close();
            return count > 0;
        }
        return false;
    }


    /**
     * Delete user from SQLite Database
     * @return
     */
    public boolean deleteUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        int affectedRows = db.delete(DbContract.USERS_TABLE_NAME, null, null);
        db.close();
        return affectedRows > 0;
    }


    /**
     * Delete cards from SQLite Database
     * @return
     */
    public boolean deleteAllCards() {
        SQLiteDatabase db = this.getWritableDatabase();
        int affectedRows = db.delete(DbContract.CARDS_TABLE_NAME, null, null);
        db.close();
        return affectedRows > 0;
    }


    /**
     * Delete bank accounts from SQLite Database
     * @return
     */
    public boolean deleteAllBankAccounts() {
        SQLiteDatabase db = this.getWritableDatabase();
        int affectedRows = db.delete(DbContract.BANK_ACCOUNTS_TABLE_NAME, null, null);
        db.close();
        return affectedRows > 0;
    }


    /**
     * Delete card info from SQLite Database
     * @param cardUUID
     * @return
     */
    public boolean deleteCard(String cardUUID) {
        SQLiteDatabase db = this.getWritableDatabase();
        int affectedRows = db.delete(DbContract.CARDS_TABLE_NAME, DbContract.CARD_UUID + " = ?", new String[]{cardUUID});
        db.close();
        return affectedRows > 0;
    }


    /**
     * Delete bank account info from SQLite Database
     * @param accountUUID
     * @return
     */
    public boolean deleteBankAccount(String accountUUID) {
        SQLiteDatabase db = this.getWritableDatabase();
        int affectedRows = db.delete(DbContract.BANK_ACCOUNTS_TABLE_NAME, DbContract.BANK_ACCOUNT_UUID + " = ?", new String[]{accountUUID});
        db.close();
        return affectedRows > 0;
    }


    /**
     * Delete all data in SQLite Database when logging out
     */
    public void dropAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        dropTables(db);
        db.close();
    }

}
