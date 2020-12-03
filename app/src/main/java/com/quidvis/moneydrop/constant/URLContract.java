package com.quidvis.moneydrop.constant;

/**
 * Created by Wisdom Emenike.
 * Date: 6/21/2017
 * Time: 12:34 AM
 */

public class URLContract {

    private static final String URL_HTTP_SCHEME = "http://";
    private static final String URL_HTTPS_SCHEME = "https://";
    public static final String URL_SCHEME = URL_HTTP_SCHEME;
    public static final String HOST_URL = "192.168.43.210:8000";
    private static final String API_URL = URL_SCHEME + HOST_URL + "/api/v1";
    public static final String LOGIN_URL = API_URL + "/auth/login";
    public static final String REGISTRATION_URL = API_URL + "/auth/register";
    public static final String FORGOT_PASSWORD_URL = API_URL + "/auth/password/forgot";
    public static final String RESET_PASSWORD_URL = API_URL + "/auth/password/reset";
    public static final String VERIFY_EMAIL_REQUEST_URL = API_URL + "/auth/verification/email/request";
    public static final String VERIFY_EMAIL_URL = API_URL + "/auth/verification/email/verify";
    public static final String DASHBOARD_REQUEST_URL = API_URL + "/dashboard";
    public static final String PROFILE_UPDATE_REQUEST_URL = API_URL + "/profile/update";
    public static final String LOAN_REQUEST_LIST_URL = API_URL + "/user/loan/requests";
    public static final String LOAN_OFFERS_LIST_URL = API_URL + "/user/loan/offers";
    public static final String TRANSACTION_LIST_URL = API_URL + "/user/transactions";

}