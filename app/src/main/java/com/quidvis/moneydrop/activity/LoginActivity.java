package com.quidvis.moneydrop.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
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
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.interfaces.OnCustomDialogClickListener;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.preference.Session;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.CustomAlertDialog;
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

    public static final String TITLE = "intent-title";
    public static final String MESSAGE = "intent-message";
    private EditText etEmail;
    private EditText etPassword;
    private CircularProgressButton loginBtn;
    private Session session;
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        String title = intent.getStringExtra(TITLE);
        String message = intent.getStringExtra(MESSAGE);

        if (title != null && message != null) {
            showDialogMessage(title, message);
        } else if (message != null) {
            Utility.toastMessage(this, message);
        }

        session = new Session(this);
        dbHelper = new DbHelper(this);

        TextView appName = findViewById(R.id.appName);
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        TextView signUpBtn = findViewById(R.id.signUpBtn);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        loginBtn = findViewById(R.id.loginBtn);

        SpannableStringBuilder string = new SpannableStringBuilder(getResources().getString(R.string.app_name));
        string.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        appName.setText(string);

        etPassword.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                login();
                return true;
            }
            return false;
        });

        forgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        signUpBtn.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, VerificationActivity.class)));

        loginBtn.setOnClickListener(v -> login());
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

                    JSONObject userData = object.getJSONObject("response");
                    JSONObject userObject = userData.getJSONObject("user");

                    User user = new User(LoginActivity.this);
                    user.setFirstname(userObject.getString("firstname"));
                    user.setMiddlename(userObject.getString("middlename"));
                    user.setLastname(userObject.getString("lastname"));
                    user.setPhone(userObject.getString("phone"));
                    user.setEmail(userObject.getString("email"));
                    user.setBvn(userObject.getString("bvn"));
                    user.setPicture(userObject.getString("picture"));
                    user.setDob(userObject.getString("dob"));
                    user.setGender(Integer.parseInt(Utility.isNull(userObject.getString("gender"), "0")));
                    user.setAddress(userObject.getString("address"));
                    user.setCountry(userObject.getString("country"));
                    user.setState(userObject.getString("state"));
                    user.setStatus(userObject.getInt("status"));
                    JSONObject verified = userObject.getJSONObject("verified");
                    user.setVerifiedEmail(verified.getBoolean("email"));
                    user.setVerifiedPhone(verified.getBoolean("phone"));
                    user.setToken(userData.getString("token"));

                    if (dbHelper.saveUser(user)) {
                        session.setLoggedIn(true);
                        Utility.toastMessage(LoginActivity.this, object.getString("message"));
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Utility.toastMessage(LoginActivity.this, "Failed to start log in session. Please try again later.");
                    }

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
                    Utility.toastMessage(LoginActivity.this, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
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

    public void showDialogMessage(String title, String message) {
        Utility.alertDialog(LoginActivity.this, title, message, "Ok", Dialog::dismiss);
    }
}