package com.quidvis.moneydrop.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.preference.Session;

import java.util.Calendar;

public class SplashActivity extends CustomCompatActivity {

    private static boolean isFirstTimeSplash = true;
    private Session session;
    private final Handler handler = new Handler();
    private final Runnable runnable = () -> {
        isFirstTimeSplash = false;
        startActivity();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        session = new Session(this);

        if(isFirstTimeSplash) {

            TextView appName = findViewById(R.id.tv_app_name);
            TextView tvCopyRight = findViewById(R.id.tvCopyRight);
            int year = Calendar.getInstance().get(Calendar.YEAR);
            String copyRight = String.format("%s. © %s", getResources().getString(R.string.app_name), year);
            if (tvCopyRight != null) tvCopyRight.setText(copyRight);

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getResources().getString(R.string.app_name));
            spannableStringBuilder.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new android.text.style.RelativeSizeSpan(1.1f), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (appName != null) appName.setText(spannableStringBuilder);

            handler.postDelayed(runnable, 2000L);

        } else startActivity();
    }

    private void startActivity() {
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
        else if (handler != null) handler.postDelayed(runnable, 2000L);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!isFirstTimeSplash) startActivity();
        else if (handler != null) handler.postDelayed(runnable, 2000L);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (handler != null) handler.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) handler.removeCallbacks(runnable);
    }
}
