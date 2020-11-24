package com.quidvis.moneydrop.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.preference.Session;

import java.util.Calendar;
import java.util.Objects;

public class Splash extends AppCompatActivity {

    private static boolean isFirstTimeSplash = true;
    private final Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    private final Runnable runnable = () -> {
        isFirstTimeSplash = false;
        startActivity();
    };
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        TextView appName = findViewById(R.id.appName);
        TextView tvCopyRight = findViewById(R.id.tvCopyRight);

        session = new Session(this);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        String copyRight = String.format(getResources().getString(R.string.app_name) + ". © %s", year);
        if (tvCopyRight != null) tvCopyRight.setText(copyRight);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getResources().getString(R.string.app_name));
        spannableStringBuilder.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new android.text.style.RelativeSizeSpan(1.1f), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        appName.setText(spannableStringBuilder);
    }

    private void startActivity() {
        if (isFinishing()) return;
        startActivity(new Intent(Splash.this, session.isLoggedIn() ? MainActivity.class : GetStartedActivity.class));
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
        else if (handler != null) handler.postDelayed(runnable, 500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) handler.removeCallbacks(runnable);
    }
}
