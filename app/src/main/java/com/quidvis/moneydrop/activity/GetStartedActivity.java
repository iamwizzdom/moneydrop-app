package com.quidvis.moneydrop.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.preference.Session;

public class GetStartedActivity extends AppCompatActivity {

    private Session session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new Session(this);

        if (!session.isFirstTimeLaunch()) {
            //start login activity
            startLoginActivity();
            return;
        }

        setContentView(R.layout.activity_get_started);

        Button createAcctBtn = findViewById(R.id.create_acct_btn);
        TextView loginBtn = findViewById(R.id.login_btn);

        createAcctBtn.setOnClickListener(v -> {
            session.setFirstTimeLaunch(false);
            startActivity(new Intent(GetStartedActivity.this, VerificationActivity.class));
            finish();
        });

        loginBtn.setOnClickListener(v -> {
            session.setFirstTimeLaunch(false);
            startLoginActivity();
        });
    }

    private void startLoginActivity() {
        startActivity(new Intent(GetStartedActivity.this, LoginActivity.class));
        finish();
    }
}
