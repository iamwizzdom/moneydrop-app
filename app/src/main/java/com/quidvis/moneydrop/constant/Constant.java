package com.quidvis.moneydrop.constant;

import android.os.Environment;

/**
 * Created by Wisdom Emenike.
 * Date: 2/19/2018
 * Time: 12:32 AM
 */

@SuppressWarnings("ALL")
public class Constant {
    /**
     * Apache server credential
     */
    public static final String SERVER_CREDENTIAL = "Wizzdom007WasHere:PWBi2x4WKTSuperSonicBrain@91ga7bHWnKfr";

    public static final String FILE_PATH = String.format("%s/MoneyDrop", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath());

    public static final String MONO_KEY = "test_pk_QkHIcvviN20Tbyap1wnF";
    public static final String FLUTTERWAVE_PUBKEY = "FLWPUBK_TEST-52ece87389c981bffbff5283aa35f00f-X";
    public static final String FLUTTERWAVE_ENCKEY = "FLWSECK_TEST76766747e348";
    public static final String GOOGLE_CLIENT_ID = "629705934099-185d1ijs1uum9qra9k7klsvglfjsv2sg.apps.googleusercontent.com";

    /**
     * HTTP Request code
     */
    public static final int HTTP_SUCCESS_CODE = 200;
    public static final int HTTP_WARNING_CODE = 401;
    public static final int HTTP_ERROR_CODE = 403;

    /**
     * Volley retry time
     */
    public static final int RETRY_IN_10_SEC = 10000;
    public static final int RETRY_IN_20_SEC = 20000;
    public static final int RETRY_IN_30_SEC = 30000;
    public static final int RETRY_IN_60_SEC = 60000;

    /**
     * Gender
     */
    public static final int MALE = 1;
    public static final int FEMALE = 2;


    /**
     * Pagination
     */
    public static final int DEFAULT_RECORD_PER_VIEW = 30;
}
