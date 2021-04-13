package com.quidvis.moneydrop.utility;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.State;
import com.quidvis.moneydrop.network.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class ImportStates {

    private final Context context;
    private final DbHelper dbHelper;
    private final Handler handler;

    protected ImportStates(Context context) {
        this.context = context;
        this.dbHelper = new DbHelper(context);
        handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    }

    private void startImport() {

        HttpRequest httpRequest = new HttpRequest((AppCompatActivity) context, URLContract.IMPORT_STATES_URL, Request.Method.GET,
                new HttpRequestParams() {
                    @Override
                    public Map<String, String> getParams() {
                        return null;
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put("Authorization", String.format("Basic %s", Base64.encodeToString(Constant.SERVER_CREDENTIAL.getBytes(), Base64.NO_WRAP)));
                        return params;
                    }

                    @Override
                    public byte[] getBody() {
                        return null;
                    }
                }) {
            @Override
            protected void onRequestStarted() {

                handler.post(ImportStates.this::onImportStarted);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                new Thread(() -> {

                    try {

                        JSONObject object = new JSONObject(response);

                        JSONObject stateData = object.getJSONObject("response");

                        if (stateData.has("states")) {

                            JSONArray statesList = stateData.getJSONArray("states");
                            int size = statesList.length(); boolean alerted = false;
                            for (int i = 0; i < size; i++) {
                                JSONObject stateObject = statesList.getJSONObject(i);
                                State state = new State(context, stateObject);
                                dbHelper.saveState(state);
                                if (i > (size / 2) && !alerted) {
                                    alerted = true;
                                    ((AppCompatActivity) context).runOnUiThread(() -> Utility.toastMessage(context, "Please be patient, we're almost done."));
                                }
                            }

                        }

                        handler.post(() -> ImportStates.this.onImportCompleted(false));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        handler.post(() -> ImportStates.this.onImportCompleted(true));
                        handler.post(() -> ImportStates.this.onImportFailed("Something unexpected happened while importing states."));
                    }

                }).start();

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    String message = object.getString("message");
                    handler.post(() -> ImportStates.this.onImportFailed(message));

                } catch (JSONException e) {
                    e.printStackTrace();
                    handler.post(() -> ImportStates.this.onImportFailed(statusCode == 503 ? error : "Something unexpected happened while importing states."));
                }
            }

            @Override
            protected void onRequestCancelled() {

                handler.post(() -> ImportStates.this.onImportFailed("State importation cancelled"));
            }
        };

        httpRequest.send(true);

    }

    public void start() {
        startImport();
    }

    protected abstract void onImportStarted();

    protected abstract void onImportCompleted(boolean onError);

    protected abstract void onImportFailed(String error);
}
