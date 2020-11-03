package com.quidvis.moneydrop.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.quidvis.moneydrop.R;

public class RegistrationSuccessfulActivity extends AppCompatActivity {

    public static final String USER_DATA = "USER_DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_successful);
    }
}