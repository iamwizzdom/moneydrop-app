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
//    public static final String HOST_URL = "moneydrop.test";
    public static final String HOST_URL = "192.168.0.109";
//    public static final String HOST_URL = "10.101.184.225:8000";
//    public static final String HOST_URL = "3.124.177.82/moneydrop/server";
    public static final String BASE_URL = URL_SCHEME + HOST_URL;
    private static final String API_URL = BASE_URL + "/api/app/v1";
    public static final String LOGIN_URL = API_URL + "/auth/login";
    public static final String REGISTRATION_URL = API_URL + "/auth/register";
    public static final String FORGOT_PASSWORD_URL = API_URL + "/auth/password/forgot";
    public static final String RESET_PASSWORD_URL = API_URL + "/auth/password/reset";
    public static final String VERIFY_EMAIL_REQUEST_URL = API_URL + "/auth/verification/email/request";
    public static final String VERIFY_EMAIL_URL = API_URL + "/auth/verification/email/verify";
    public static final String VERIFY_PHONE_REQUEST_URL = API_URL + "/auth/verification/phone/request";
    public static final String VERIFY_PHONE_URL = API_URL + "/auth/verification/phone/verify";
    public static final String DASHBOARD_REQUEST_URL = API_URL + "/dashboard";
    public static final String PROFILE_UPDATE_REQUEST_URL = API_URL + "/user/profile/update/%s";
    public static final String PROFILE_INFO_REQUEST_URL = API_URL + "/user/profile/info";
    public static final String PROFILE_RATE_REQUEST_URL = API_URL + "/user/rate";
    public static final String USER_LOAN_REQUEST_LIST_URL = API_URL + "/user/loan/requests";
    public static final String USER_LOAN_OFFERS_LIST_URL = API_URL + "/user/loan/offers";
    public static final String LOAN_REQUEST_LIST_URL = API_URL + "/loan/requests";
    public static final String LOAN_OFFERS_LIST_URL = API_URL + "/loan/offers";
    public static final String LOAN_APPLICANTS_URL = API_URL + "/loan/%s/applicants";
    public static final String LOAN_APPLICATION_GRANT_URL = API_URL + "/loan/%s/application/%s/grant";
    public static final String LOAN_APPLY_URL = API_URL + "/loan/%s/apply";
    public static final String LOAN_REVOKE_URL = API_URL + "/loan/%s/revoke";
    public static final String LOAN_APPLICATION_CANCEL_URL = API_URL + "/loan/%s/application/%s/cancel";
    public static final String TRANSACTION_LIST_URL = API_URL + "/user/transactions";
    public static final String CARD_TRANS_LOG_URL = API_URL + "/user/card/add/reference";
    public static final String CARD_VERIFICATION_URL = API_URL + "/user/card/add/verify";
    public static final String CARD_RETRIEVE_ALL_URL = API_URL + "/user/card/retrieve/all";
    public static final String CARD_REMOVE_URL = API_URL + "/user/card/remove/";
    public static final String BANK_ACCOUNT_ADD_URL = API_URL + "/user/bank/add-account";
    public static final String BANK_ACCOUNT_RETRIEVE_ALL_URL = API_URL + "/user/bank/retrieve/all";
    public static final String BANK_ACCOUNT_REMOVE_URL = API_URL + "/user/bank/remove/";
    public static final String WALLET_TOP_UP_URL = API_URL + "/user/wallet/top-up/";
    public static final String WALLET_CASH_OUT_URL = API_URL + "/user/wallet/cash-out/";
    public static final String LOAN_REQUEST_URL = API_URL + "/loan/request";
    public static final String LOAN_OFFER_URL = API_URL + "/loan/offer";
    public static final String LOAN_CONSTANTS_URL = API_URL + "/loan/constants";
    public static final String NOTIFICATIONS_URL = API_URL + "/notifications";
    public static final String HISTORY_URL = API_URL + "/history";
    public static final String LOAN_REPAYMENT_URL = API_URL + "/loan/application/%s/repayment";
    public static final String LOAN_REPAYMENT_HISTORY_URL = API_URL + "/loan/application/%s/repayment/history";
    public static final String LOAN_APPLICANT_REVIEW_URL = API_URL + "/loan/application/%s/review";
    public static final String USER_REVIEWS_URL = API_URL + "/user/%s/reviews";
    public static final String EDIT_REVIEW_URL = API_URL + "/review/%s/edit";
    public static final String DELETE_REVIEW_URL = API_URL + "/review/%s/delete";

}