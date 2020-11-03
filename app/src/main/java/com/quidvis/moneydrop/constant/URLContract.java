package com.quidvis.moneydrop.constant;

/**
 * Created by Wisdom Emenike.
 * Date: 6/21/2017
 * Time: 12:34 AM
 */

public class URLContract {

    private static final String URL_HTTP_SCHEME = "http://";
    private static final String URL_HTTPS_SCHEME = "https://";
    private static final String URL_SCHEME = URL_HTTPS_SCHEME;
    private static final String HOST_URL = "29de1efbe688.ngrok.io/api/v1";
    private static final String API_URL = URL_SCHEME + HOST_URL;
    public static final String COUNTRY_LIST_REQUEST_URL = API_URL + "/country-list";
    public static final String LOGIN_URL = API_URL + "/login";
    public static final String VERIFY_EMAIL_REQUEST_URL = API_URL + "/verification/email/request";
    public static final String VERIFY_EMAIL_URL = API_URL + "/verification/email/verify";
    public static final String REGISTRATION_URL = API_URL + "/register";
    public static final String FORGOT_PASSWORD_URL = API_URL + "/password/forgot";
    public static final String RESET_PASSWORD_URL = API_URL + "/password/reset";

}