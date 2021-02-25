package com.quidvis.moneydrop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.preference.Session;

public class RegistrationSuccessfulActivity extends CustomCompatActivity {

    public static final String USER_EMAIL = "USER_EMAIL";
    private String email;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_successful);
        session = new Session(this);
        Intent intent = getIntent();
        email = intent.getStringExtra(USER_EMAIL);
    }

    public void startUsing(View view) {
        boolean isLoggedIn = session.isLoggedIn();
        Intent intent = new Intent(this, isLoggedIn ? MainActivity.class : LoginActivity.class);
        if (!isLoggedIn && email != null) intent.putExtra(LoginActivity.EMAIL, email);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}