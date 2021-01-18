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
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private CircularProgressButton sendOTPBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        etEmail = findViewById(R.id.etEmail);
        sendOTPBtn = findViewById(R.id.sendOTPBtn);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Utility.requestFocus(etEmail, ForgotPasswordActivity.this);
            }
        }, 1000);

        etEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    sendOTP();
                    return true;
                }
                return false;
            }
        });

        sendOTPBtn.setOnClickListener(v -> sendOTP());
    }

    public void onBackPressed(View view) {
        super.onBackPressed();
    }

    private void sendOTP() {

        final String email = Objects.requireNonNull(etEmail.getText()).toString();

        if (!Validator.isValidEmail(email)) {
            etEmail.setError("Please enter a valid email address");
            return;
        } else etEmail.setError(null);

        HttpRequest httpRequest = new HttpRequest(this, URLContract.FORGOT_PASSWORD_URL, Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
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
                Utility.disableEditText(etEmail);
                Utility.clearFocus(etEmail, ForgotPasswordActivity.this);
                sendOTPBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                sendOTPBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);
                    Utility.toastMessage(ForgotPasswordActivity.this, object.getString("message"));
                    Intent intent = new Intent(ForgotPasswordActivity.this, PasswordResetActivity.class);
                    intent.putExtra(PasswordResetActivity.EMAIL, email);
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.enableEditText(etEmail);
                    Utility.toastMessage(ForgotPasswordActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(ForgotPasswordActivity.this);

                    dialog.setTitle(object.getString("title"));
                    dialog.setMessage(object.getString("message"));

                    if (statusCode == HttpURLConnection.HTTP_CONFLICT) {
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("Ok", new OnAwesomeDialogClickListener() {
                            @Override
                            public void onClick(AwesomeAlertDialog dialog) {
                                dialog.dismiss();
                                Intent intent = new Intent(ForgotPasswordActivity.this, PasswordResetActivity.class);
                                intent.putExtra(PasswordResetActivity.EMAIL, email);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else dialog.setPositiveButton("Ok");

                    dialog.display();


                    JSONObject errors = object.getJSONObject("error");

                    if (errors.length() > 0) {
                        for (Iterator<String> it = errors.keys(); it.hasNext(); ) {
                            String key = it.next();
                            String value = errors.getString(key);
                            if (TextUtils.isEmpty(value)) continue;
                            if ("email".equals(key)) {
                                etEmail.setError(value);
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(ForgotPasswordActivity.this, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }
                Utility.enableEditText(etEmail);
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }
}