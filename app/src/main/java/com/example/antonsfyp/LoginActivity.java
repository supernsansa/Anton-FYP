package com.example.antonsfyp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    //TODO Login method and ASyncTask

    public void moveToRegisterMenu(View view) {
        Intent intent = new Intent (this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

}
