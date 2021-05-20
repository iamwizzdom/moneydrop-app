package com.quidvis.moneydrop.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;

import java.util.concurrent.Executor;

public abstract class FingerprintHandler {

    private final Context context;
    private final FingerprintCallback fingerprintCallback;
    public static final int FINGERPRINT_PERMISSION = 1101;

    // Constructor
    public FingerprintHandler(Context context, FingerprintCallback fingerprintCallback) {
        this.context = context;
        this.fingerprintCallback = fingerprintCallback;
    }

    protected abstract String getTitle();
    protected abstract String getSubtitle();

    public void authenticate(BiometricPrompt.CryptoObject cryptoObject) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            FingerprintHandler.this.fingerprintCallback.onAuthenticationError(BiometricPrompt.ERROR_HW_UNAVAILABLE, "Fingerprint permission not granted.");
            ActivityCompat.requestPermissions((Activity) this.context, new String[]{Manifest.permission.USE_FINGERPRINT}, FingerprintHandler.FINGERPRINT_PERMISSION);
            return;
        }

        String title = getTitle();
        String subtitle = getSubtitle();

        if (title == null || TextUtils.isEmpty(title)) {
            FingerprintHandler.this.fingerprintCallback.onAuthenticationError(BiometricPrompt.ERROR_CANCELED, "Title must be set and non-empty.");
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this.context);
        BiometricPrompt biometricPrompt = new BiometricPrompt((CustomCompatActivity) this.context, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        FingerprintHandler.this.fingerprintCallback.onAuthenticationError(errorCode, errString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        FingerprintHandler.this.fingerprintCallback.onAuthenticationSucceeded(result);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        FingerprintHandler.this.fingerprintCallback.onAuthenticationFailed();
                    }
                });
        BiometricPrompt.PromptInfo.Builder promptInfoBuilder = new BiometricPrompt.PromptInfo.Builder();
        promptInfoBuilder = promptInfoBuilder.setTitle(title);
        if (subtitle != null && !TextUtils.isEmpty(subtitle))
            promptInfoBuilder = promptInfoBuilder.setSubtitle(subtitle);
        promptInfoBuilder = promptInfoBuilder.setNegativeButtonText("Use account password");
        BiometricPrompt.PromptInfo promptInfo = promptInfoBuilder.build();

        if (cryptoObject != null) biometricPrompt.authenticate(promptInfo, cryptoObject);
        else biometricPrompt.authenticate(promptInfo);
    }

    public interface FingerprintCallback {
        void onAuthenticationError(int errorCode, @NonNull CharSequence errString);

        void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result);

        void onAuthenticationFailed();
    }

}
