package com.quidvis.moneydrop.activity;

import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.android.volley.Request;
import com.google.android.material.textfield.TextInputLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import com.apachat.loadingbutton.core.customViews.CircularProgressButton;

public class PasswordResetActivity extends CustomCompatActivity {

    private TextInputLayout emailHolder;
    private EditText etEmail, etOTP;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private CircularProgressButton resetBtn;

    private String email;
    static final String EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        Intent intent = getIntent();
        if (intent != null) email = intent.getStringExtra(EMAIL);

        emailHolder = findViewById(R.id.email_holder);
        etEmail = findViewById(R.id.etEmail);
        etOTP = findViewById(R.id.etOTP);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        resetBtn = findViewById(R.id.resetBtn);

        if (email != null && !email.isEmpty()) emailHolder.setVisibility(View.GONE);
        else emailHolder.setVisibility(View.VISIBLE);

        etConfirmPassword.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                resetPassword();
                return true;
            }
            return false;
        });

        resetBtn.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {

        final String otp = Objects.requireNonNull(etOTP.getText()).toString();
        final String password = Objects.requireNonNull(etPassword.getText()).toString();
        final String passwordConfirmation = Objects.requireNonNull(etConfirmPassword.getText()).toString();

        if (email == null || email.isEmpty())
            email = Objects.requireNonNull(etEmail.getText()).toString();

        if (!Validator.isValidEmail(email)) {
            Utility.toastMessage(PasswordResetActivity.this, "Please enter a valid email address");
            return;
        }

        if (otp.isEmpty()) {
            etOTP.setError("Please enter a OTP");
            return;
        } else etOTP.setError(null);

        if (otp.length() < 5) {
            etOTP.setError("Please enter a complete OTP");
            return;
        } else etOTP.setError(null);

        if (password.isEmpty()) {
            etPassword.setError("Please enter your password");
            Utility.requestFocus(etPassword, PasswordResetActivity.this);
            return;
        }

        if (password.length() < 8) {
            etPassword.setError("Your password should be at least 8 characters");
            Utility.requestFocus(etPassword, PasswordResetActivity.this);
            return;
        } else etPassword.setError(null);

        if (!password.equals(passwordConfirmation)) {
            etConfirmPassword.setError("Password do not match");
            Utility.requestFocus(etConfirmPassword, PasswordResetActivity.this);
            return;
        } else etConfirmPassword.setError(null);

        HttpRequest httpRequest = new HttpRequest(this, URLContract.RESET_PASSWORD_URL, Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("otp", otp);
                params.put("password", password);
                params.put("password_confirmation", passwordConfirmation);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                return null;
            }

            @Override
            public byte[] getBody() {
                return null;
            }
        }) {
            @Override
            protected void onRequestStarted() {
                Utility.clearFocus(etEmail, PasswordResetActivity.this);
                Utility.clearFocus(etOTP, PasswordResetActivity.this);
                Utility.clearFocus(etPassword, PasswordResetActivity.this);
                Utility.clearFocus(etConfirmPassword, PasswordResetActivity.this);

                Utility.disableEditText(etEmail);
                Utility.disableEditText(etOTP);
                Utility.disableEditText(etPassword);
                Utility.disableEditText(etConfirmPassword);
                resetBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                Utility.enableEditText(etEmail);
                Utility.enableEditText(etOTP);
                Utility.enableEditText(etPassword);
                Utility.enableEditText(etConfirmPassword);
                resetBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);

                    etEmail.setText("");
                    etOTP.setText("");
                    etPassword.setText("");
                    etConfirmPassword.setText("");

                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(PasswordResetActivity.this);
                    dialog.setCancelable(false);
                    dialog.setTitle(object.getString("title"));
                    dialog.setMessage(object.getString("message"));
                    dialog.setPositiveButton("Ok", dialog1 -> {
                        dialog1.dismiss();
                        Intent intent = new Intent(PasswordResetActivity.this, LoginActivity.class);
                        intent.putExtra(LoginActivity.EMAIL, email);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finishAffinity();
                    });
                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(PasswordResetActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(PasswordResetActivity.this);

                    dialog.setTitle(object.getString("title"));
                    dialog.setMessage(object.getString("message"));
                    dialog.setPositiveButton("Ok");
                    dialog.display();

                    if (object.has("errors")) {

                        JSONObject errors = object.getJSONObject("errors");

                        if (errors.length() > 0) {
                            for (Iterator<String> it = errors.keys(); it.hasNext(); ) {
                                String key = it.next();
                                String value = errors.getString(key);
                                if (TextUtils.isEmpty(value)) continue;
                                switch (key) {
                                    case "email":
                                        Utility.toastMessage(PasswordResetActivity.this, value);
                                        break;
                                    case "otp":
                                        etOTP.setError(value);
                                        break;
                                    case "password":
                                        etPassword.setError(value);
                                        break;
                                    case "password_confirmation":
                                        etConfirmPassword.setError(value);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(PasswordResetActivity.this, statusCode == 503 ? error :
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
        super.onBackPressed();
    }
}