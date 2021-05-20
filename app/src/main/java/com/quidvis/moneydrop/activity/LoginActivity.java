package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import com.android.volley.Request;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.messaging.FirebaseMessaging;
import com.quidvis.moneydrop.BuildConfig;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.interfaces.OnAwesomeDialogClickListener;
import com.quidvis.moneydrop.model.BankAccount;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.preference.Session;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.FingerprintHandler;
import com.quidvis.moneydrop.utility.FirebaseMessageReceiver;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.Validator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class LoginActivity extends CustomCompatActivity {

    public static final String TITLE = "intent-title";
    public static final String MESSAGE = "intent-message";
    public static final String EMAIL = "intent-email";
    private EditText etEmail;
    private EditText etPassword;
    private TextInputLayout etPasswordInputLayout;
    private CircularProgressButton loginBtn, googleSignInBtn;
    private TextView signUpBtn;
    private Session session;
    private DbHelper dbHelper;
    private GoogleSignInClient mGoogleSignInClient;

    private static final String KEY_NAME = BuildConfig.APPLICATION_ID;
    private static final int RC_SIGN_IN = 2039;

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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestId()
                .requestIdToken(Constant.GOOGLE_CLIENT_ID)
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FingerprintHandler fingerprintHandler = new FingerprintHandler(this, new FingerprintHandler.FingerprintCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                Utility.toastMessage(LoginActivity.this, errString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                try {
                    String encryptedInfo = session.getLastPassword();
                    byte[] bytes = Base64.decode(encryptedInfo, Base64.NO_WRAP);
                    String userInfo = new String(Objects.requireNonNull(Objects.requireNonNull(result.getCryptoObject()).getCipher()).doFinal(bytes));
                    int index = userInfo.indexOf("-");
                    String email = userInfo.substring(0, index);
                    if (!email.equalsIgnoreCase(etEmail.getText().toString())) {
                        Utility.alertDialog(LoginActivity.this, "Biometrics Failed",
                                "That's a new credential, you must first login with account password.");
                        return;
                    }
                    String password = userInfo.substring((index + 1));
                    login(password, true);
                } catch (Exception e) {
                    Utility.alertDialog(LoginActivity.this, "Biometrics Failed",
                            "Your biometrics seem to have been invalidate, you must login with account password once again.");
                    e.printStackTrace();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                Utility.toastMessage(LoginActivity.this, "Authentication failed!");
            }
        }) {
            @Override
            protected String getTitle() {
                return "Sign in with Fingerprint";
            }

            @Override
            protected String getSubtitle() {
                return null;
            }
        };

        TextView appName = findViewById(R.id.tv_app_name);
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        signUpBtn = findViewById(R.id.signUpBtn);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordInputLayout = findViewById(R.id.etPasswordLayout);
        loginBtn = findViewById(R.id.loginBtn);
        googleSignInBtn = findViewById(R.id.googleSignInBtn);

        if (email == null) email = session.getLastEmail();

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

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                etPasswordInputLayout.setEndIconDrawable(s.length() == 0 ? R.drawable.ic_fingerprint : R.drawable.show_password_selector);
            }
        });


        try {
            if (getSecretKey() == null) {
                generateSecretKey(new KeyGenParameterSpec.Builder(
                        KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setUserAuthenticationRequired(false)
                        .build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        etPasswordInputLayout.setEndIconOnClickListener(v -> {

            EditText editText = etPasswordInputLayout.getEditText();

            if (editText == null || editText.getText().length() == 0) {

                if (TextUtils.isEmpty(session.getLastPassword())) {
                    Utility.toastMessage(LoginActivity.this, "For the first time you must login with your password.");
                    return;
                }

                if (!Validator.isValidEmail(etEmail.getText().toString())) {
                    etEmail.setError("Please enter a valid email address");
                    return;
                } else etEmail.setError(null);


                Utility.clearFocus(etEmail, this);
                Utility.clearFocus(etPassword, this);

                BiometricManager biometricManager = androidx.biometric.BiometricManager.from(this);
                switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {

                    // this means we can use biometric sensor
                    case BiometricManager.BIOMETRIC_SUCCESS:

                        try {
                            fingerprintHandler.authenticate(new BiometricPrompt.CryptoObject(Objects.requireNonNull(initCipher(Cipher.DECRYPT_MODE))));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;

                    // this means that the device doesn't have fingerprint sensor
                    case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                        Utility.toastMessage(LoginActivity.this, "Your Device does not have a Fingerprint Sensor");
                        break;

                    // this means that biometric sensor is not available
                    case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                        Utility.toastMessage(LoginActivity.this, "The biometric sensor is currently unavailable");
                        break;

                    // this means that the device doesn't contain your fingerprint
                    case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                        Utility.toastMessage(LoginActivity.this, "Register at least one fingerprint in Settings");
                        break;
                    case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                        Utility.toastMessage(LoginActivity.this, "Biometric security update required");
                        break;
                    case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                        Utility.toastMessage(LoginActivity.this, "Biometrics are not supported");
                        break;
                    case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                        Utility.toastMessage(LoginActivity.this, "Unexpected biometrics error occurred");
                        break;
                }

                return;
            }

            // Store the current cursor position
            final int selection = editText.getSelectionEnd();
            if (hasPasswordTransformation()) {
                editText.setTransformationMethod(null);
            } else {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            // And restore the cursor position
            if (selection >= 0) {
                editText.setSelection(selection);
            }

            etPasswordInputLayout.refreshEndIconDrawableState();

        });

        forgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        signUpBtn.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, VerifyEmailActivity.class)));

        loginBtn.setOnClickListener(v -> login());
        googleSignInBtn.setOnClickListener(v -> googleSignIn());
    }

    private boolean hasPasswordTransformation() {
        EditText editText = etPasswordInputLayout.getEditText();
        return editText != null && editText.getTransformationMethod() instanceof PasswordTransformationMethod;
    }

    private void generateSecretKey(KeyGenParameterSpec keyGenParameterSpec) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }

    private SecretKey getSecretKey() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null);
        return ((SecretKey) keyStore.getKey(KEY_NAME, null));
    }

    private Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    }

    private Cipher initCipher(int cipherMode) {
        try {

            Cipher cipher = getCipher();
            SecretKey secretKey = getSecretKey();
            if (cipherMode == Cipher.ENCRYPT_MODE) {
                cipher.init(cipherMode, secretKey);
                session.setCipherIV(Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP));
            } else {
                byte[] iv = Base64.decode(session.getCipherIV(), Base64.NO_WRAP);
                IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
                cipher.init(cipherMode, secretKey, ivParameterSpec);
            }
            return cipher;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void googleSignIn() {
        googleSignInBtn.startAnimation();
        Utility.clearFocus(etEmail, LoginActivity.this);
        Utility.clearFocus(etPassword, LoginActivity.this);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void login() {
        login(Objects.requireNonNull(etPassword.getText()).toString(), false);
    }

    private void login(String password, boolean isFingerprintAuth) {

        final String email = Objects.requireNonNull(etEmail.getText()).toString();

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
                params.put("is_fingerprint_auth", String.valueOf(isFingerprintAuth ? 1 : 0));
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

                    if (object.getBoolean("status")) {

                        JSONObject userData = object.getJSONObject("response");
                        JSONArray cards = userData.getJSONArray("cards");
                        JSONArray bankAccounts = userData.getJSONArray("bank-accounts");
//                    JSONObject banks = userData.getJSONObject("banks");

                        User user = new User(LoginActivity.this, userData.getJSONObject("user"));

                        if (dbHelper.saveUser(user)) {

                            for (int i = 0; i < cards.length(); i++) {
                                JSONObject cardObject = cards.getJSONObject(i);
                                com.quidvis.moneydrop.model.Card card = new com.quidvis.moneydrop.model.Card(LoginActivity.this);
                                card.setUuid(cardObject.getString("uuid"));
                                card.setName(cardObject.getString("name"));
                                card.setLastFourDigits(cardObject.getString("last4digits"));
                                card.setBrand(cardObject.getString("brand"));
                                card.setExpMonth(cardObject.getString("exp_month"));
                                card.setExpYear(cardObject.getString("exp_year"));
                                dbHelper.saveCard(card);
                            }

                            for (int i = 0; i < bankAccounts.length(); i++) {
                                JSONObject accountObject = bankAccounts.getJSONObject(i);
                                BankAccount account = new BankAccount(LoginActivity.this);
                                account.setUuid(accountObject.getString("uuid"));
                                account.setAccountName(accountObject.getString("account_name"));
                                account.setAccountNumber(accountObject.getString("acct_no"));
                                account.setBankName(accountObject.getString("bank_name"));
                                account.setRecipientCode(accountObject.getString("recipient_code"));
                                dbHelper.saveBankAccount(account);
                            }

//                        for (Iterator<String> it = banks.keys(); it.hasNext();) {
//                            String key = it.next();
//                            JSONObject bankObject = banks.getJSONObject(key);
//                            Bank bank = new Bank(LoginActivity.this);
//                            bank.setUid(bankObject.getInt("id"));
//                            bank.setName(bankObject.getString("name"));
//                            bank.setCode(bankObject.getString("code"));
//                            dbHelper.saveBank(bank);
//                        }

                            Utility.toastMessage(LoginActivity.this, object.getString("message"));

                            session.setLoggedIn(true);
                            session.setLastEmail(user.getEmail());

                            Cipher cipher = initCipher(Cipher.ENCRYPT_MODE);
                            if (cipher != null) {
                                try {
                                    String userInfo = String.format("%s-%s", email, password);
                                    byte[] bytes = cipher.doFinal(userInfo.getBytes());
                                    String encryptedPass = Base64.encodeToString(bytes, Base64.NO_WRAP);
                                    session.setLastPassword(encryptedPass);
                                } catch (BadPaddingException | IllegalBlockSizeException e) {
                                    e.printStackTrace();
                                }
                            }

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finishAffinity();

                        } else {
                            Utility.toastMessage(LoginActivity.this, "Failed to start log in session. Please try again later.");
                        }

                    } else {

                        Utility.toastMessage(LoginActivity.this, object.getString("message"));
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

                    if (object.has("errors")) {

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

    private void loginWithGoogle(String email, String googleId) {

        if (!Validator.isValidEmail(email)) {
            Utility.toastMessage(this, "Please enter a valid email address");
            return;
        }

        if (googleId.isEmpty()) {
            Utility.toastMessage(this, "Please enter a valid google ID");
            return;
        }

        HttpRequest httpRequest = new HttpRequest(this, URLContract.LOGIN_WITH_GOOGLE_URL, Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("google_id", googleId);
                params.put("pn_token", session.getFirebaseToken());
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
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                googleSignInBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) {

                        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {

                            try {

                                JSONObject userData = object.getJSONObject("response");
                                JSONArray cards = userData.getJSONArray("cards");
                                JSONArray bankAccounts = userData.getJSONArray("bank-accounts");
//                                JSONObject banks = userData.getJSONObject("banks");

                                User user = new User(LoginActivity.this, userData.getJSONObject("user"));

                                if (dbHelper.saveUser(user)) {

                                    for (int i = 0; i < cards.length(); i++) {
                                        JSONObject cardObject = cards.getJSONObject(i);
                                        com.quidvis.moneydrop.model.Card card = new com.quidvis.moneydrop.model.Card(LoginActivity.this);
                                        card.setUuid(cardObject.getString("uuid"));
                                        card.setName(cardObject.getString("name"));
                                        card.setLastFourDigits(cardObject.getString("last4digits"));
                                        card.setBrand(cardObject.getString("brand"));
                                        card.setExpMonth(cardObject.getString("exp_month"));
                                        card.setExpYear(cardObject.getString("exp_year"));
                                        dbHelper.saveCard(card);
                                    }

                                    for (int i = 0; i < bankAccounts.length(); i++) {
                                        JSONObject accountObject = bankAccounts.getJSONObject(i);
                                        BankAccount account = new BankAccount(LoginActivity.this);
                                        account.setUuid(accountObject.getString("uuid"));
                                        account.setAccountName(accountObject.getString("account_name"));
                                        account.setAccountNumber(accountObject.getString("acct_no"));
                                        account.setBankName(accountObject.getString("bank_name"));
                                        account.setRecipientCode(accountObject.getString("recipient_code"));
                                        dbHelper.saveBankAccount(account);
                                    }

//                                    for (Iterator<String> it = banks.keys(); it.hasNext(); ) {
//                                        String key = it.next();
//                                        JSONObject bankObject = banks.getJSONObject(key);
//                                        Bank bank = new Bank(LoginActivity.this);
//                                        bank.setUid(bankObject.getInt("id"));
//                                        bank.setName(bankObject.getString("name"));
//                                        bank.setCode(bankObject.getString("code"));
//                                        dbHelper.saveBank(bank);
//                                    }

                                    Utility.toastMessage(LoginActivity.this, object.getString("message"));

                                    session.setLoggedIn(true);
                                    session.setLastEmail(user.getEmail());

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finishAffinity();

                                } else {
                                    Utility.toastMessage(LoginActivity.this, "Failed to start log in session. Please try again later.");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Utility.toastMessage(LoginActivity.this, "Something unexpected happened. Please try that again.");
                            }

                        }).addOnFailureListener(e -> Utility.toastMessage(LoginActivity.this, "Sign failed at this time, let's try it again."));

                    } else {

                        Utility.toastMessage(LoginActivity.this, object.getString("message"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(LoginActivity.this, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    if (statusCode == 404) mGoogleSignInClient.signOut();

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(LoginActivity.this);

                    dialog.setTitle(object.getString("title"));
                    String message;
                    if (object.has("errors") && object.getJSONObject("errors").length() > 0) {
                        message = Utility.serializeObject(object.getJSONObject("errors"));
                    } else message = object.getString("message");
                    dialog.setMessage(message);
                    dialog.setPositiveButton(statusCode == 404 ? "Sign Up" : "Ok", dialog1 -> {
                        if (statusCode == 404) signUpBtn.performClick();
                        dialog1.dismiss();
                    });

                    dialog.display();

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        AwesomeAlertDialog dialog = new AwesomeAlertDialog(LoginActivity.this);

        dialog.setTitle("Sign in Failed");
        dialog.setPositiveButton("Ok", Dialog::dismiss);

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && !account.isExpired()) {
                loginWithGoogle(account.getEmail(), account.getId());
            } else {
                if (account != null && account.isExpired()) {
                    Utility.toastMessage(this, "Retrying");
                    mGoogleSignInClient.signOut().addOnCompleteListener(task -> googleSignIn()).addOnFailureListener(e -> {
                        googleSignInBtn.revertAnimation();
                        Utility.toastMessage(this, "Sign in failed");
                    });
                    return;
                }
                googleSignInBtn.revertAnimation();
                dialog.setMessage("Sorry, we couldn't not sign you in at this time with the selected Google account.");
                dialog.display();
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            googleSignInBtn.revertAnimation();
            dialog.setMessage("Sorry, an unexpected error occurred while trying you to sign you in with the selected Google account.");
            dialog.display();
        }
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