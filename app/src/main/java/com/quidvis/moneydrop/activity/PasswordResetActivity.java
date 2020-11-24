package com.quidvis.moneydrop.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.interfaces.OnAwesomeDialogClickListener;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class PasswordResetActivity extends AppCompatActivity {

    private EditText etOTP;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private CircularProgressButton resetBtn;

    private String email;
    static final String EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) email = intent.getStringExtra(EMAIL);

        setContentView(R.layout.activity_password_reset);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        ImageView backBtn = toolbar.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        etOTP = findViewById(R.id.etOTP);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        resetBtn = findViewById(R.id.resetBtn);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Utility.requestFocus(etOTP, PasswordResetActivity.this);
            }
        }, 1000);

        etConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    resetPassword();
                    return true;
                }
                return false;
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void resetPassword() {

        final String otp = Objects.requireNonNull(etOTP.getText()).toString();
        final String password = Objects.requireNonNull(etPassword.getText()).toString();
        final String passwordConfirmation = Objects.requireNonNull(etConfirmPassword.getText()).toString();

        if (!Validator.isValidEmail(email)) {
            Utility.toastMessage(PasswordResetActivity.this, "Please enter a valid email address");
            return;
        }

        if (otp.isEmpty()) {
            etOTP.setError("Please enter a OTP");
            return;
        } else etOTP.setError(null);

        if (otp.length() < 4) {
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

        HttpRequest httpRequest = new HttpRequest(URLContract.RESET_PASSWORD_URL, Request.Method.POST, new HttpRequestParams() {
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
                Utility.disableEditText(etOTP);
                Utility.disableEditText(etPassword);
                Utility.disableEditText(etConfirmPassword);
                Utility.clearFocus(etOTP, PasswordResetActivity.this);
                Utility.clearFocus(etPassword, PasswordResetActivity.this);
                Utility.clearFocus(etConfirmPassword, PasswordResetActivity.this);
                resetBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);

                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(PasswordResetActivity.this);
                    dialog.setCancelable(false);
                    dialog.setTitle(object.getString("title"));
                    dialog.setMessage(object.getString("message"));
                    dialog.setPositiveButton("Ok", new OnAwesomeDialogClickListener() {
                        @Override
                        public void onClick(AwesomeAlertDialog dialog) {
                            dialog.dismiss();
                            Intent intent = new Intent(PasswordResetActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.enableEditText(etOTP);
                    Utility.enableEditText(etPassword);
                    Utility.enableEditText(etConfirmPassword);
                    Utility.toastMessage(PasswordResetActivity.this, "Something unexpected happened. Please try that again.");
                }
                resetBtn.revertAnimation();
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


                    JSONObject errors = object.getJSONObject("error");

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

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(PasswordResetActivity.this, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }

                Utility.enableEditText(etOTP);
                Utility.enableEditText(etPassword);
                Utility.enableEditText(etConfirmPassword);
                resetBtn.revertAnimation();
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send(PasswordResetActivity.this);
    }
}