package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.firebase.messaging.FirebaseMessaging;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.Bank;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.preference.Session;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.FirebaseMessageReceiver;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.Validator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class LoginActivity extends CustomCompatActivity {

    public static final String TITLE = "intent-title";
    public static final String MESSAGE = "intent-message";
    public static final String EMAIL = "intent-email";
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
        String email = intent.getStringExtra(EMAIL);

        if (title != null && message != null) {
            showDialogMessage(title, message);
        } else if (message != null) {
            Utility.toastMessage(this, message);
        }

        session = new Session(this);
        dbHelper = new DbHelper(this);

        if (TextUtils.isEmpty(session.getFirebaseToken())) {
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> FirebaseMessageReceiver.storeRegIdInPref(session, s));
        }

        TextView appName = findViewById(R.id.tv_app_name);
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        TextView signUpBtn = findViewById(R.id.signUpBtn);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        loginBtn = findViewById(R.id.loginBtn);

        if (email != null && Validator.isValidEmail(email)) {
            etEmail.setText(email);
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getResources().getString(R.string.app_name));
        spannableStringBuilder.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new android.text.style.RelativeSizeSpan(1.1f), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        appName.setText(spannableStringBuilder);

        etPassword.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                login();
                return true;
            }
            return false;
        });

        forgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        signUpBtn.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, VerifyEmailActivity.class)));

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

        HttpRequest httpRequest = new HttpRequest(this, URLContract.LOGIN_URL, Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                params.put("pn_token", session.getFirebaseToken());
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
            protected void onRequestCompleted(boolean onError) {

                loginBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    JSONObject userData = object.getJSONObject("response");
                    JSONArray cards = userData.getJSONArray("cards");
                    JSONObject banks = userData.getJSONObject("banks");

                    User user = new User(LoginActivity.this, userData.getJSONObject("user"));

                    if (dbHelper.saveUser(user)) {

                        for (int i = 0; i < cards.length(); i++) {
                            JSONObject cardObject = cards.getJSONObject(i);
                            com.quidvis.moneydrop.model.Card card = new com.quidvis.moneydrop.model.Card(LoginActivity.this);
                            card.setUuid(cardObject.getString("uuid"));
                            card.setName(cardObject.getString("name"));
                            card.setCardType(cardObject.getString("card_type"));
                            card.setLastFourDigits(cardObject.getString("last4"));
                            card.setBrand(cardObject.getString("brand"));
                            card.setExpMonth(cardObject.getString("exp_month"));
                            card.setExpYear(cardObject.getString("exp_year"));
                            dbHelper.saveCard(card);
                        }

                        for (Iterator<String> it = banks.keys(); it.hasNext();) {
                            String key = it.next();
                            JSONObject bankObject = banks.getJSONObject(key);
                            Bank bank = new Bank(LoginActivity.this);
                            bank.setUid(bankObject.getInt("id"));
                            bank.setName(bankObject.getString("name"));
                            bank.setCode(bankObject.getString("code"));
                            dbHelper.saveBank(bank);
                        }

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


                    JSONObject errors = object.getJSONObject("errors");

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
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    public void showDialogMessage(String title, String message) {
        Utility.alertDialog(LoginActivity.this, title, message, "Ok", Dialog::dismiss);
    }

    @Override
    public void onStart() {
        super.onStart();
        Utility.requestFocus(etEmail, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Utility.clearFocus(etEmail, this);
        Utility.clearFocus(etPassword, this);
    }
}