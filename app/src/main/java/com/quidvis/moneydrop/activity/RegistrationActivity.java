package com.quidvis.moneydrop.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hbb20.CountryCodePicker;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.preference.Session;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.FirebaseMessageReceiver;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class RegistrationActivity extends CustomCompatActivity implements DatePickerDialog.OnDateSetListener {

    public static final String EMAIL = "email";
    public static final String PHONE = "phone";

    private String email, phone;
    private EditText etFirstname;
    private EditText etLastname;
    private EditText etPhone;
    private CountryCodePicker ccp;
    private EditText etEmail;
    private EditText etDOB;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private TextView tvDobError;
    private TextView tvEmailError;
    private CircularProgressButton signupBtn;
    private final Calendar calendar = Calendar.getInstance();
    private final DateFormat formatter = new SimpleDateFormat(
            "yyyy-MM-dd", new java.util.Locale("en","ng"));

    private Session session;
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);

        session = new Session(this);
        dbHelper = new DbHelper(this);

        if (TextUtils.isEmpty(session.getFirebaseToken())) {
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> FirebaseMessageReceiver.storeRegIdInPref(session, s));
        }

        Intent intent = getIntent();

        if (intent != null) {
            email = intent.getStringExtra(EMAIL);
            phone = intent.getStringExtra(PHONE);
        }

        etFirstname = findViewById(R.id.etFirstname);
        etLastname = findViewById(R.id.etLastname);
        etDOB = findViewById(R.id.etDOB);
        tvDobError = findViewById(R.id.tvDobError);
        ccp = findViewById(R.id.ccp);
        etPhone = findViewById(R.id.etPhone);
        ccp.registerCarrierNumberEditText(etPhone);
        etEmail = findViewById(R.id.etEmail);
        tvEmailError = findViewById(R.id.tvEmailError);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        signupBtn = findViewById(R.id.signUpBtn);

        if (!TextUtils.isEmpty(email)) {
            etEmail.setText(email);
            Utility.disableEditText(etEmail);
        }

        if (!TextUtils.isEmpty(phone)) {
            ccp.setFullNumber(phone);
            ccp.setCcpClickable(false);
            ccp.setContentColor(Color.GRAY);
            Utility.disableEditText(etPhone);
        }

        etDOB.setOnClickListener(v -> setDate());

        etConfirmPassword.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                signup();
                return true;
            }
            return false;
        });

        signupBtn.setOnClickListener(v -> signup());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void signup() {

        final String firstname = Objects.requireNonNull(etFirstname.getText()).toString();
        final String lastname = Objects.requireNonNull(etLastname.getText()).toString();
        final String phone = ccp.getFullNumberWithPlus();
        final String password = Objects.requireNonNull(etPassword.getText()).toString();
        final String confirmPassword = Objects.requireNonNull(etConfirmPassword.getText()).toString();
        final String dob = Objects.requireNonNull(etDOB.getText()).toString();

        if (!Validator.isValidEmail(email)) {
            etEmail.setError("Please enter a valid email address");
            tvEmailError.setText(R.string.invalid_email);
            tvEmailError.setVisibility(View.VISIBLE);
            return;
        } else tvEmailError.setVisibility(View.GONE);

        if (firstname.isEmpty()) {
            etFirstname.setError("Please enter your first name");
            Utility.requestFocus(etFirstname, RegistrationActivity.this);
            return;
        } else etFirstname.setError(null);

        if (lastname.isEmpty()) {
            etLastname.setError("Please enter your last name");
            Utility.requestFocus(etLastname, RegistrationActivity.this);
            return;
        } else etLastname.setError(null);

        if (phone.isEmpty()) {
            etPhone.setError("Please enter your phone number");
            Utility.requestFocus(etPhone, RegistrationActivity.this);
            return;
        }

        if (!Validator.isValidPhone(phone)) {
            etPhone.setError("Please enter a valid phone number");
            Utility.requestFocus(etPhone, RegistrationActivity.this);
            return;
        } else etPhone.setError(null);

        if (!dob.matches("^[0-9]{4}-(0[1-9]|1[0-2])-(1[0-9]|0[1-9]|3[0-1]|2[1-9])$")) {
            tvDobError.setText(R.string.select_dob);
            tvDobError.setVisibility(View.VISIBLE);
            return;
        } else tvDobError.setVisibility(View.GONE);

        if (password.isEmpty()) {
            etPassword.setError("Please enter your password");
            Utility.requestFocus(etPassword, RegistrationActivity.this);
            return;
        }

        if (password.length() < 8) {
            etPassword.setError("Your password should be at least 8 characters");
            Utility.requestFocus(etPassword, RegistrationActivity.this);
            return;
        } else etPassword.setError(null);

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Password do not match");
            Utility.requestFocus(etConfirmPassword, RegistrationActivity.this);
            return;
        } else etConfirmPassword.setError(null);

        HttpRequest httpRequest = new HttpRequest(this, URLContract.REGISTRATION_URL, Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("firstname", firstname);
                params.put("lastname", lastname);
                params.put("phone", phone);
                params.put("email", email);
                params.put("dob", dob);
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
                Utility.disableEditText(etFirstname);
                Utility.disableEditText(etLastname);
                Utility.disableEditText(etPhone);
                Utility.disableEditText(etPassword);
                Utility.disableEditText(etConfirmPassword);

                Utility.clearFocus(etFirstname, RegistrationActivity.this);
                Utility.clearFocus(etLastname, RegistrationActivity.this);
                Utility.clearFocus(etPhone, RegistrationActivity.this);
                Utility.clearFocus(etPassword, RegistrationActivity.this);
                Utility.clearFocus(etConfirmPassword, RegistrationActivity.this);
                signupBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                signupBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);
                    if (object.getBoolean("status")) {

                        Utility.toastMessage(RegistrationActivity.this, object.getString("message"));

                        JSONObject userData = object.getJSONObject("response");

                        User user = new User(RegistrationActivity.this, userData.getJSONObject("user"));

                        Intent intent = new Intent(RegistrationActivity.this, RegistrationSuccessfulActivity.class);

                        if (dbHelper.saveUser(user)) session.setLoggedIn(true);
                        else intent.putExtra(RegistrationSuccessfulActivity.USER_EMAIL, email);

                        startActivity(intent);
                        finish();

                    } else showMessage(object);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.enableEditText(etFirstname);
                    Utility.enableEditText(etLastname);
                    Utility.enableEditText(etPhone);
                    Utility.enableEditText(etPassword);
                    Utility.enableEditText(etConfirmPassword);
                    Utility.toastMessage(RegistrationActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(error);
                    showMessage(object);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(RegistrationActivity.this, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }
                Utility.enableEditText(etFirstname);
                Utility.enableEditText(etLastname);
                Utility.enableEditText(etPhone);
                Utility.enableEditText(etPassword);
                Utility.enableEditText(etConfirmPassword);
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();
    }

    private void showMessage(JSONObject object) {
        try {

            AwesomeAlertDialog dialog = new AwesomeAlertDialog(RegistrationActivity.this);

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
                            case "firstname":
                                etFirstname.setError(value);
                                break;
                            case "lastname":
                                etLastname.setError(value);
                                break;
                            case "phone":
                                etPhone.setError(value);
                                break;
                            case "email":
                                tvEmailError.setText(value);
                                tvEmailError.setVisibility(View.VISIBLE);
                                break;
                            case "dob":
                                tvDobError.setText(value);
                                tvDobError.setVisibility(View.VISIBLE);
                                break;
                            case "password":
                                etPassword.setError(value);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }

        } catch (JSONException e) {

            Utility.toastMessage(RegistrationActivity.this,
                    "Something unexpected happened. Please try that again.");
        }
    }

    private void setDate() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(RegistrationActivity.this,
                RegistrationActivity.this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    /**
     * @param view       the picker associated with the dialog
     * @param year       the selected year
     * @param month      the selected month (0-11 for compatibility with
     *                   {@link Calendar#MONTH})
     * @param dayOfMonth the selected day of the month (1-31, depending on
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        calendar.set(year, month, dayOfMonth);
        etDOB.setText(formatter.format(calendar.getTime()));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        Utility.clearFocus(etFirstname, this);
        Utility.clearFocus(etLastname, this);
        Utility.clearFocus(etPhone, this);
        Utility.clearFocus(etPassword, this);
        Utility.clearFocus(etConfirmPassword, this);
    }

    public void onBackPressed(View view) {
        super.onBackPressed();
    }
}
