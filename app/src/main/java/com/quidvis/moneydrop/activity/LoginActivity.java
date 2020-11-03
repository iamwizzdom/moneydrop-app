package com.quidvis.moneydrop.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
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

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private CircularProgressButton loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView appName = findViewById(R.id.appName);
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        TextView signUpBtn = findViewById(R.id.signUpBtn);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        loginBtn = findViewById(R.id.loginBtn);

        SpannableStringBuilder string = new SpannableStringBuilder(getResources().getString(R.string.app_name));
        string.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        appName.setText(string);

        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    login();
                    return true;
                }
                return false;
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, VerificationActivity.class));
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {

        final String email = Objects.requireNonNull(etEmail.getText()).toString();
        final String password = Objects.requireNonNull(etPassword.getText()).toString();

        if (!Validator.isValidEmail(email)) {
            etEmail.setError("Please enter a valid email address");
            return;
        } else etEmail.setError(null);

        if (password.isEmpty()) {
            etPassword.setError("Please enter your password");
            Utility.requestFocus(etPassword, LoginActivity.this);
            return;
        }

        if (password.length() < 8) {
            etPassword.setError("Your password should be at least 8 characters");
            Utility.requestFocus(etPassword, LoginActivity.this);
            return;
        } else etPassword.setError(null);

        HttpRequest httpRequest = new HttpRequest(URLContract.LOGIN_URL, Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
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
                Utility.disableEditText(etEmail, Color.WHITE);
                Utility.disableEditText(etPassword, Color.WHITE);
                Utility.clearFocus(etEmail, LoginActivity.this);
                Utility.clearFocus(etPassword, LoginActivity.this);
                loginBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);
                    Utility.toastMessage(LoginActivity.this, object.getString("message"));
                    Intent intent = new Intent(LoginActivity.this, RegistrationSuccessfulActivity.class);
                    intent.putExtra(RegistrationSuccessfulActivity.USER_DATA, object.getJSONObject("response").toString());
                    startActivity(intent);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.enableEditText(etEmail, Color.WHITE);
                    Utility.enableEditText(etPassword, Color.WHITE);
                    Utility.toastMessage(LoginActivity.this, "Something unexpected happened. Please try that again.");
                }
                loginBtn.revertAnimation();
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(LoginActivity.this);

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
                                    etEmail.setError(value);
                                    break;
                                case "password":
                                    etPassword.setError(value);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(LoginActivity.this, error.contains("{") ? "Something unexpected happened. Please try that again." : error);
                }
                Utility.enableEditText(etEmail, Color.WHITE);
                Utility.enableEditText(etPassword, Color.WHITE);
                loginBtn.revertAnimation();
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send(LoginActivity.this);
    }
}