package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.hbb20.CountryCodePicker;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import com.apachat.loadingbutton.core.customViews.CircularProgressButton;

public class VerifyPhoneActivity extends CustomCompatActivity {

    public final static int VERIFY_PHONE_KEY = 122;
    private AwesomeAlertDialog dialog;
    private EditText etPhone;
    private CountryCodePicker ccp;
    private CircularProgressButton verifyBtn;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        Intent intent = getIntent();

        if (intent != null) email = intent.getStringExtra(RegistrationActivity.EMAIL);

        dialog = new AwesomeAlertDialog(this);

        ccp = findViewById(R.id.ccp);
        etPhone = findViewById(R.id.etPhone);
        ccp.registerCarrierNumberEditText(etPhone);

        verifyBtn = findViewById(R.id.verifyBtn);

        etPhone.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                requestVerification();
                return true;
            }
            return false;
        });

        verifyBtn.setOnClickListener(v -> requestVerification());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            Utility.toastMessage(this, "Failed to capture verification data.");
            return;
        }

        if (requestCode != VERIFY_PHONE_KEY) {
            Utility.toastMessage(this, "Failed to capture verification data.");
            return;
        }

        boolean verified = data.getBooleanExtra(VerificationActivity.VERIFIED, false);
        if (verified) startRegistration();
        else Utility.toastMessage(this, "Phone verification failed.");
    }

    private void startRegistration() {

        if (email == null || !Validator.isValidEmail(email)) {
            Utility.toastMessage(this, "Can't start registration without a valid email address");
            return;
        }

        String phone = ccp.getFullNumberWithPlus();

        if (!Validator.isValidPhone(phone)) {
            Utility.toastMessage(this, "Can't start registration without a valid phone number");
            return;
        }

        Intent intent = new Intent(VerifyPhoneActivity.this, RegistrationActivity.class);
        intent.putExtra(RegistrationActivity.EMAIL, email);
        intent.putExtra(RegistrationActivity.PHONE, phone);
        startActivity(intent);
        finish();
    }

    private void requestVerification() {

        final String phone = ccp.getFullNumberWithPlus();

        if (!Validator.isValidPhone(phone)) {
            Utility.toastMessage(this, "Please enter a valid phone number");
            return;
        }

        HttpRequest httpRequest = new HttpRequest(this, URLContract.VERIFY_PHONE_REQUEST_URL, Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("phone", phone);
                return params;
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
                Utility.disableEditText(etPhone);
                ccp.setCcpClickable(false);
                ccp.setContentColor(Color.GRAY);
                Utility.clearFocus(etPhone, VerifyPhoneActivity.this);
                verifyBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {
                Utility.enableEditText(etPhone);
                ccp.setCcpClickable(true);
                ccp.setContentColor(Color.BLACK);
                verifyBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) {

                        Intent intent = new Intent(VerifyPhoneActivity.this, VerificationActivity.class);
                        intent.putExtra(VerificationActivity.VERIFICATION_DATA, phone);
                        intent.putExtra(VerificationActivity.VERIFICATION_TYPE, "phone");
                        intent.putExtra(VerificationActivity.COUNT_DOWN_TIME, object.getInt("expire"));
                        startActivityForResult(intent, VERIFY_PHONE_KEY);
                        Utility.toastMessage(VerifyPhoneActivity.this, object.getString("message"));

                    } else {
                        dialog.setTitle(object.getString("title"));
                        dialog.setMessage(object.getString("message"));
                        dialog.setPositiveButton("Ok", Dialog::dismiss);
                        dialog.display();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(VerifyPhoneActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);

                    if (statusCode == HttpURLConnection.HTTP_CONFLICT) {

                        Utility.toastMessage(VerifyPhoneActivity.this, object.getString("message"));
                        startRegistration();

                    } else {

                        dialog.setTitle(object.getString("title"));
                        if (object.has("errors")) {
                            JSONObject errors = object.getJSONObject("errors");
                            if (errors.length() > 0) dialog.setMessage(Utility.serializeObject(errors));
                            else dialog.setMessage(object.getString("message"));
                        } else dialog.setMessage(object.getString("message"));
                        dialog.setPositiveButton("Ok");
                        dialog.display();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(VerifyPhoneActivity.this, statusCode == 503 ? error :
                            "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }

        };
        httpRequest.send();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}