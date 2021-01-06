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
        setContentView(R.layout.activity_get_started);

        session = new Session(this);

        Button createAcctBtn = findViewById(R.id.create_acct_btn);
        TextView loginBtn = findViewById(R.id.login_btn);

        createAcctBtn.setOnClickListener(v -> startActivity(new Intent(GetStartedActivity.this, VerificationActivity.class)));

        loginBtn.setOnClickListener(v -> startActivity(new Intent(GetStartedActivity.this, LoginActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session.isLoggedIn()) finish();
    }
}
