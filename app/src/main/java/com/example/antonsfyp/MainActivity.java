package com.example.antonsfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //TODO Redo buttons and colour scheme
    //TODO ACCOUNT SYSTEM

    private boolean login_status = false;
    private String username = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //If user has logged in/registered, get their status and user name
        login_status = getIntent().getBooleanExtra("LOGIN_STATUS",false);
        if(login_status == true) {
            username = getIntent().getStringExtra("USERNAME");
            Button loginButton = (Button) findViewById(R.id.profile_button);
            loginButton.setText(username);
        }
    }

    //Takes user to add word/phrase screen when user taps relevant button
    public void moveToAddMenu (View view) {
        Intent intent = new Intent (this, AddWordActivity.class);
        //TODO: In later version pass on login details for POST request to db
        startActivity(intent);
    }

    public void moveToBrowseMenu(View view) {
        Intent intent = new Intent (this, BrowseActivity.class);
        //TODO: In later version pass on login details for POST request to db
        intent.putExtra("EXTRA_SEARCH_TERMS", "*");
        startActivity(intent);
    }

    public void moveToLoginMenu(View view) {
        Intent intent = new Intent (this, LoginActivity.class);
        startActivity(intent);
    }
}