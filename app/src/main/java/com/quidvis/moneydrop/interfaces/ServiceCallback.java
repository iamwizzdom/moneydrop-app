package com.quidvis.moneydrop.interfaces;

import org.json.JSONArray;
import org.json.JSONObject;

public interface ServiceCallback {
    void passJsonObjectResponse(JSONObject response);
    void passJsonArrayResponse(JSONArray response);
    void passStringResponse(String response);
    void onErrorResponse();
    void refreshAdapter();
    void toggleProgressBar();
    void toggleProgressBar(boolean status);
    void toggleProgressBar(int type, boolean status);
    void logoutUser();
    void logoutUser(String message);
    void showDialogMessage(String title, String message);
    void toastMessage(String message);
    boolean isOldVersion(String version);
}
