package com.quidvis.moneydrop.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.preference.Session;

import java.util.Calendar;

public class Splash extends AppCompatActivity {

    private static boolean isFirstTimeSplash = true;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        TextView appName = findViewById(R.id.tv_app_name);
        TextView tvCopyRight = findViewById(R.id.tvCopyRight);

        session = new Session(this);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        String copyRight = String.format("%s. © %s", getResources().getString(R.string.app_name), year);
        if (tvCopyRight != null) tvCopyRight.setText(copyRight);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getResources().getString(R.string.app_name));
        spannableStringBuilder.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new android.text.style.RelativeSizeSpan(1.1f), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        appName.setText(spannableStringBuilder);
    }

    private void startActivity() {
        if (isFinishing()) return;
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (isFirstTimeSplash) SystemClock.sleep(1000L);
                isFirstTimeSplash = false;
                startActivity(new Intent(Splash.this, session.isLoggedIn() ? MainActivity.class : GetStartedActivity.class));
                finish();
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
