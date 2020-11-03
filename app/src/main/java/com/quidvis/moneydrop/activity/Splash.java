package com.quidvis.moneydrop.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.quidvis.moneydrop.R;

import java.util.Calendar;

public class Splash extends AppCompatActivity {

    private static boolean isFirstTimeSplash = true;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            isFirstTimeSplash = false;
            startActivity();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        TextView appName = findViewById(R.id.appName);
        TextView copyRight = findViewById(R.id.tvCopyRight);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        String copy_right = String.format(getResources().getString(R.string.app_name) + ". © %s", year);
        copyRight.setText(copy_right);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getResources().getString(R.string.app_name));
        spannableStringBuilder.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new android.text.style.RelativeSizeSpan(1.1f), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        appName.setText(spannableStringBuilder);
    }

    private void startActivity() {
        if (isFinishing()) return;
        startActivity(new Intent(Splash.this, GetStartedActivity.class));
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
        else if (handler != null) handler.postDelayed(runnable, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) handler.removeCallbacks(runnable);
    }
}
