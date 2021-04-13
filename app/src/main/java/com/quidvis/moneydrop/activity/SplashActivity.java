package com.quidvis.moneydrop.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.model.Country;
import com.quidvis.moneydrop.preference.Session;
import com.quidvis.moneydrop.utility.ImportCountries;
import com.quidvis.moneydrop.utility.ImportStates;
import com.quidvis.moneydrop.utility.Utility;

import java.util.ArrayList;
import java.util.Calendar;

public class SplashActivity extends CustomCompatActivity {

    private static boolean isFirstTimeSplash = true, paused = false;
    private Session session;
    private final Handler handler = new Handler();
    private final Runnable runnable = this::startActivity;
    private ProgressBar splashProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        session = new Session(this);
        DbHelper dbHelper = new DbHelper(this);

        if(isFirstTimeSplash) {

            ImportStates importStates = new ImportStates(this) {
                @Override
                protected void onImportStarted() {

                    splashProgress.setVisibility(View.VISIBLE);
                    Utility.toastMessage(SplashActivity.this, "Still importing assets, please wait...");
                }

                @Override
                protected void onImportCompleted(boolean onError) {
                    if (!onError) session.setCompletedStateImport(true);
                    startActivity();
                    Utility.toastMessage(SplashActivity.this, onError ? "Asset importation ended unsuccessfully." : "Asset importation completed successfully.");
                }

                @Override
                protected void onImportFailed(String error) {
                    startActivity();
                    Utility.toastMessage(SplashActivity.this, error);
                }
            };

            ImportCountries importCountries = new ImportCountries(this) {
                @Override
                protected void onImportStarted() {

                    splashProgress.setVisibility(View.VISIBLE);
                    Utility.toastMessage(SplashActivity.this, "Importing assets, please wait...");
                }

                @Override
                protected void onImportCompleted(boolean onError) {

                    if (!onError) {

                        session.setCompletedCountryImport(true);

                        if (!session.hasCompletedStateImport()) {
                            dbHelper.deleteStates();
                            importStates.start();
                        } else {
                            startActivity();
                            Utility.toastMessage(SplashActivity.this, "Asset importation completed successfully.");
                        }

                    } else {
                        startActivity();
                        Utility.toastMessage(SplashActivity.this, "Asset importation ended unsuccessfully.");
                    }
                }

                @Override
                protected void onImportFailed(String error) {
                    startActivity();
                    Utility.toastMessage(SplashActivity.this, error);
                }
            };

            splashProgress = findViewById(R.id.splashProgress);
            TextView appName = findViewById(R.id.tv_app_name);
            TextView tvCopyRight = findViewById(R.id.tvCopyRight);
            int year = Calendar.getInstance().get(Calendar.YEAR);
            String copyRight = String.format("%s. © %s", getResources().getString(R.string.app_name), year);
            if (tvCopyRight != null) tvCopyRight.setText(copyRight);

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getResources().getString(R.string.app_name));
            spannableStringBuilder.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new android.text.style.RelativeSizeSpan(1.1f), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (appName != null) appName.setText(spannableStringBuilder);

            if (!session.hasCompletedCountryImport()) {
                dbHelper.deleteCountries();
                importCountries.start();
            } else if (!session.hasCompletedStateImport()) {
                dbHelper.deleteStates();
                importStates.start();
            } else handler.postDelayed(runnable, 2000L);

        } else startActivity();
    }

    private void startActivity() {
        isFirstTimeSplash = false;
        if (isFinishing()) return;
        startActivity(new Intent(SplashActivity.this, session.isLoggedIn() ? MainActivity.class : GetStartedActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (handler != null) handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstTimeSplash) startActivity();
        else if (handler != null && paused) handler.postDelayed(runnable, 2000L);
        paused = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!isFirstTimeSplash) startActivity();
        else if (handler != null && paused) handler.postDelayed(runnable, 2000L);
        paused = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (handler != null) handler.removeCallbacks(runnable);
        paused = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) handler.removeCallbacks(runnable);
        paused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) handler.removeCallbacks(runnable);
        paused = true;
    }
}
