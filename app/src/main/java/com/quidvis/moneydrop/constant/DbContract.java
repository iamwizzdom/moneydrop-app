package com.quidvis.moneydrop.constant;

/**
 * Created by Wisdom Emenike.
 * Date: 6/21/2017
 * Time: 12:33 AM
 */

@SuppressWarnings("ALL")
public class DbContract {
    /**
     * db name variable
     */
    public static final String DATABASE_NAME = "moneydropDB";

    /**
     * user db table variables
     */
    public static final String USERS_TABLE_NAME = "users";
    public static final String USER_UID = "_id";
    public static final String USER_FIRSTNAME = "firstname";
    public static final String USER_MIDDLENAME = "middlename";
    public static final String USER_LASTNAME = "lastname";
    public static final String USER_EMAIL = "email";
    public static final String USER_PHONE = "phone";
    public static final String USER_PICTURE = "picture";
    public static final String USER_DOB = "dob";
    public static final String USER_GENDER = "gender";
    public static final String USER_ADDRESS = "address";
    public static final String USER_COUNTY = "country";
    public static final String USER_STATE = "state";
    public static final String USER_STATUS = "status";
    public static final String USER_BVN = "bvn";
    public static final String USER_TOKEN = "token";
    public static final String USER_VERIFIED_EMAIL = "verified_email";
    public static final String USER_VERIFIED_PHONE = "verified_phone";


    /**
     * card db table variables
     */
    public static final String CARDS_TABLE_NAME = "cards";
    public static final String CARD_UID = "_id";
    public static final String CARD_UUID = "uuid";
    public static final String CARD_NAME = "name";
    public static final String CARD_TYPE = "cardType";
    public static final String CARD_BRAND = "brand";
    public static final String CARD_LAST_FOUR_DIGITS = "lastFourDigits";
    public static final String CARD_EXP_MONTH = "expMonth";
    public static final String CARD_EXP_YEAR = "expYear";

}
