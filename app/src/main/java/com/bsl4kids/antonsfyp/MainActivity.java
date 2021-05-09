package com.bsl4kids.antonsfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private boolean login_status = false;
    private String username = "null";
    //Change this to your IP address (on Windows use ipconfig on cmd and find "default gateway"), the port shouldn't be needed (depends on your XAMPP setup).
    public static String ip_address = "192.168.1.173:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //If user has logged in/registered, get their status and user name
        login_status = getIntent().getBooleanExtra("LOGIN_STATUS",false);
        if(login_status == true) {
            username = getIntent().getStringExtra("USERNAME");
            Button loginButton = (Button) findViewById(R.id.profile_button);
            //If user is logged in, the login button should say logout instead
            loginButton.setText("Logout");
            //Display username
            TextView usernameText = (TextView) findViewById(R.id.username);
            usernameText.setText(username);
        }
        //If user isn't logged in, add button should be restricted
        else {
            //Disable and gray out add word/phrase button
            Button addButton = (Button) findViewById(R.id.add_word_button);
            addButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            addButton.setClickable(false);
        }
    }

    //Do nothing if back button is pressed as this is the whole app's parent activity
    @Override
    public void onBackPressed() {
        return;
    }

    //Takes user to add word/phrase screen
    public void moveToAddMenu (View view) {
        Intent intent = new Intent (this, AddWordActivity.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("LOGIN_STATUS", login_status);
        startActivity(intent);
    }

    //Takes the user to the search by word/phrase screen
    public void moveToBrowseMenu(View view) {
        Intent intent = new Intent (this, BrowseActivity.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("LOGIN_STATUS", login_status);
        intent.putExtra("EXTRA_SEARCH_TERMS", "*");
        intent.putExtra("TAG_SEARCH", false);
        intent.putExtra("SORT_BY", "A");
        startActivity(intent);
    }

    //Takes the user to the search by tag screen
    public void moveToTagBrowseMenu(View view) {
        Intent intent = new Intent (this, BrowseTagActivity.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("LOGIN_STATUS", login_status);
        intent.putExtra("EXTRA_SEARCH_TERMS", "*");
        intent.putExtra("SORT_BY", "A");
        startActivity(intent);
    }

    public void moveToLoginMenu(View view) {
        //If user isn't logged in, take them to the login screen
        if(login_status == false) {
            Intent intent = new Intent (this, LoginActivity.class);
            startActivity(intent);
        }
        //If they are logged in, show prompt asking if the want to logout
        else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Are you sure?");
            alertDialogBuilder.setCancelable(false);

            //If yes, log tem out and return to main menu
            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    //Take user to next activity
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra("USERNAME", "null");
                    intent.putExtra("LOGIN_STATUS", false);
                    startActivity(intent);
                    finish();
                }
            });

            //If no, do nothing
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    //Do nothing
                    return;
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
}