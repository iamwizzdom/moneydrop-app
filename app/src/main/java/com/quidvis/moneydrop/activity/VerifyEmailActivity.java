package com.quidvis.moneydrop.activity;

import androidx.annotation.Nullable;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.android.volley.Request;
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

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class VerifyEmailActivity extends CustomCompatActivity {

    public final static int VERIFY_EMAIL_KEY = 110;
    private AwesomeAlertDialog dialog;
    private EditText etEmail;
    private CircularProgressButton verifyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        dialog = new AwesomeAlertDialog(this);

        etEmail = findViewById(R.id.etEmail);

        verifyBtn = findViewById(R.id.verifyBtn);

        etEmail.setOnEditorActionListener((textView, id, keyEvent) -> {
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

        if (requestCode != VERIFY_EMAIL_KEY) {
            Utility.toastMessage(this, "Failed to capture verification data.");
            return;
        }

        boolean verified = data.getBooleanExtra(VerificationActivity.VERIFIED, false);
        if (verified) startPhoneVerification();
        else Utility.toastMessage(this, "Email verification failed.");
    }

    private void startPhoneVerification() {

        final String email = etEmail.getText().toString();

        if (!Validator.isValidEmail(email)) {
            Utility.toastMessage(this, "Can't start phone verification without a valid email address");
            return;
        }

        Intent intent = new Intent(VerifyEmailActivity.this, VerifyPhoneActivity.class);
        intent.putExtra(RegistrationActivity.EMAIL, email);
        startActivity(intent);
        finish();
    }

    private void requestVerification() {

        final String email = etEmail.getText().toString();

        if (!Validator.isValidEmail(email)) {
            Utility.toastMessage(this, "Please enter a valid email address");
            return;
        }

        HttpRequest httpRequest = new HttpRequest(this, URLContract.VERIFY_EMAIL_REQUEST_URL, Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
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
                Utility.disableEditText(etEmail);
                Utility.clearFocus(etEmail, VerifyEmailActivity.this);
                verifyBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {
                Utility.enableEditText(etEmail);
                verifyBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) {

                        Intent intent = new Intent(VerifyEmailActivity.this, VerificationActivity.class);
                        intent.putExtra(VerificationActivity.VERIFICATION_DATA, email);
                        intent.putExtra(VerificationActivity.VERIFICATION_TYPE, "email");
                        intent.putExtra(VerificationActivity.COUNT_DOWN_TIME, object.getInt("expire"));
                        startActivityForResult(intent, VERIFY_EMAIL_KEY);
                        Utility.toastMessage(VerifyEmailActivity.this, object.getString("message"));

                    } else {
                        dialog.setTitle(object.getString("title"));
                        dialog.setMessage(object.getString("message"));
                        dialog.setPositiveButton("Ok", Dialog::dismiss);
                        dialog.display();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(VerifyEmailActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);

                    if (statusCode == HttpURLConnection.HTTP_CONFLICT) {

                        Utility.toastMessage(VerifyEmailActivity.this, object.getString("message"));
                        startPhoneVerification();

                    } else {

                        dialog.setTitle(object.getString("title"));
                        JSONObject errors = object.getJSONObject("errors");
                        if (errors.length() > 0) dialog.setMessage(Utility.serializeObject(errors));
                        else dialog.setMessage(object.getString("message"));
                        dialog.setPositiveButton("Ok");
                        dialog.display();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(VerifyEmailActivity.this, statusCode == 503 ? error :
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