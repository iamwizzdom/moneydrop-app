package com.quidvis.moneydrop.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.chaos.view.PinView;
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
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class VerificationActivity extends CustomCompatActivity {

    public static final String COUNT_DOWN_TIME = "countDownTime",
            VERIFICATION_TYPE = "verificationType",
            VERIFICATION_DATA = "verificationData",
            VERIFIED = "verified", OLD_DATA = "oldData";

    private CountDownTimer countDownTimer;
    private boolean verified = false;

    private PinView pvOTP;
    private TextView tvResend;
    private CircularProgressButton submitBtn;
    private AwesomeAlertDialog dialog;

    private String oldData, data, type;
    private int countDownTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Intent intent = getIntent();

        if (intent == null) {
            Utility.toastMessage(this, "No data passed to verification activity");
            finish();
            return;
        }

        oldData = intent.getStringExtra(OLD_DATA);
        data = intent.getStringExtra(VERIFICATION_DATA);
        type = intent.getStringExtra(VERIFICATION_TYPE);
        countDownTime = intent.getIntExtra(COUNT_DOWN_TIME, 0);

        if (type == null || data == null) {
            Utility.toastMessage(this, "Invalid passed to verification activity");
            finish();
            return;
        }

        dialog = new AwesomeAlertDialog(VerificationActivity.this);

        pvOTP = findViewById(R.id.otp);
        TextView tvMessage = findViewById(R.id.message);
        tvResend = findViewById(R.id.resend_otp);
        submitBtn = findViewById(R.id.submit);

        submitBtn.setOnClickListener(v -> verify());

        tvMessage.setText(String.format("Enter the 5 digit OTP we sent your %s at %s", type, data));

        startCountDown();
    }

    private void verify() {

        final String otp = Objects.requireNonNull(pvOTP.getText()).toString();

        if (type.equals("phone") && !Validator.isValidPhone(data)) {
            Utility.toastMessage(this, "Please enter a valid phone number");
            return;
        } else if (type.equals("email") && !Validator.isValidEmail(data)) {
            Utility.toastMessage(this, "Please enter a valid email address");
            return;
        }

        if (otp.length() < 5) {
            Utility.toastMessage(this, "Please enter a complete OTP");
            return;
        }

        HttpRequest httpRequest = new HttpRequest(this, type.equals("phone") ? URLContract.VERIFY_PHONE_URL : URLContract.VERIFY_EMAIL_URL,
                Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (oldData != null) params.put("old_" + type, oldData);
                params.put(type, data);
                params.put("code", otp);
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
                Utility.clearFocus(pvOTP, VerificationActivity.this);
                Utility.disableEditText(pvOTP);
                submitBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                submitBtn.revertAnimation();
                Utility.enableEditText(pvOTP);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);
                    verified = object.getBoolean("status");
                    dialog.setTitle(object.getString("title"));
                    if (verified && countDownTimer != null) countDownTimer.cancel();
                    dialog.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_success, null));
                    dialog.setMessage(object.getString("message"));
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Ok", dialog1 -> {
                        dialog1.dismiss();
                        onBackPressed();
                    });
                    dialog.setOnDismissListener(dialog -> {
                        dialog.dismiss();
                        onBackPressed();
                    });
                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(VerificationActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    System.out.println(error);
                    JSONObject object = new JSONObject(error);

                    dialog.setTitle(object.getString("title"));

                    if (object.has("errors")) {
                        JSONObject errors = object.getJSONObject("errors");
                        if (errors.length() > 0) dialog.setMessage(Utility.serializeObject(errors));
                        else dialog.setMessage(object.getString("message"));
                    } else dialog.setMessage(object.getString("message"));

                    if (statusCode == HttpURLConnection.HTTP_CONFLICT) {
                        if (countDownTimer != null) countDownTimer.cancel();
                        dialog.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_success, null));
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("Ok", dialog1 -> {
                            dialog1.dismiss();
                            onBackPressed();
                        });
                        dialog.setOnDismissListener(dialog -> {
                            dialog.dismiss();
                            onBackPressed();
                        });
                        verified = true;
                    } else dialog.setPositiveButton("Ok");

                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(VerificationActivity.this, statusCode == 503 ? error :
                            "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }

        };
        httpRequest.send();
    }

    private void startCountDown() {

        countDownTimer = new CountDownTimer(countDownTime * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                String resentTxt = String.format("Didn't receive OTP? Wait for %s:%s", minutes > 9 ? minutes : "0" + minutes, seconds > 9 ? seconds : "0" + seconds);
                int start = resentTxt.lastIndexOf("for") + 4, end = resentTxt.length();
                SpannableString spannableString = new SpannableString(resentTxt);
                spannableString.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new android.text.style.RelativeSizeSpan(1.1f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new android.text.style.ForegroundColorSpan(getResources().getColor(R.color.colorAccent, null)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvResend.setText(spannableString);
            }

            @Override
            public void onFinish() {

                String resentTxt = "Didn't get OTP? Resend code";
                int start = resentTxt.lastIndexOf("?") + 2, end = resentTxt.length();
                SpannableString spannableString = new SpannableString(resentTxt);
                spannableString.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new android.text.style.RelativeSizeSpan(1.1f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new android.text.style.ForegroundColorSpan(getResources().getColor(R.color.colorAccent, null)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ClickableSpan clickHandler = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View v) {
                        onBackPressed();
                    }
                    @Override
                    public void updateDrawState(@NonNull TextPaint tp) {
                        super.updateDrawState(tp);
                        tp.setUnderlineText(false);
                    }
                };
                spannableString.setSpan(clickHandler, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvResend.setText(spannableString);
                tvResend.setMovementMethod(LinkMovementMethod.getInstance());

            }
        };

        countDownTimer.start();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(VERIFIED, verified);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}
