package com.bsl4kids.antonsfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class AddWordActivity extends AppCompatActivity {

    private static final int SELECT_VIDEO = 3;
    private String selectedPath = "";
    private static Intent VideoFileData;
    private String wordName = "";
    private boolean login_status = false;
    private String username = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        login_status = getIntent().getBooleanExtra("LOGIN_STATUS",false);
        if(login_status == true) {
            username = getIntent().getStringExtra("USERNAME");
        }
    }

    //Takes user input and adds a word entry to database
    public void addWordToDB(View view) {
        //Get editText boxes
        EditText editWordName = (EditText) findViewById(R.id.editWordName);
        //Extract strings from editText
        wordName = editWordName.getText().toString();

        if(wordName.trim().length() > 0 && wordName != null) {
            //Probe DB to check availability
            new ProbeTask().execute();
        }
        else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Error:");
            alertDialogBuilder.setMessage("Please provide a word/phrase");
            alertDialogBuilder.setCancelable(false);

            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

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

    public class ProbeTask extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(AddWordActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Show loading dialog
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL("http://" + MainActivity.ip_address + "/FYP_Scripts/probeDB.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            }

            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);

                // setDoOutput to true as we receive data from json file
                conn.setDoOutput(true);

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return e1.toString();
            }

            try {
                //Encode data to post
                String post_data = URLEncoder.encode("wordName","UTF-8")+"="+URLEncoder.encode(wordName,"UTF-8");

                //Send encoded data
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write(post_data);
                wr.flush();

                // Read data sent from server
                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                //Debug
                System.out.println(result);

                // Pass data to onPostExecute method
                return (result.toString());

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            pdLoading.dismiss();
            //If word isn't already in the db...
            if (result.equals("proceed")) {
                //Take user to next activity
                Intent intent = new Intent (AddWordActivity.this, AddDescActivity.class);
                intent.putExtra("TYPE","add");
                intent.putExtra("WORD_NAME",wordName);
                intent.putExtra("USERNAME", username);
                intent.putExtra("LOGIN_STATUS", login_status);
                startActivity(intent);
            }
            else if (result.equals("exists")) {
                //Display dialog to notify user that the word already exists
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddWordActivity.this);
                alertDialogBuilder.setTitle("An entry for this word already exists");
                alertDialogBuilder.setCancelable(false);

                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        return;
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            else {
                //If some unknown error occurs
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddWordActivity.this);
                alertDialogBuilder.setTitle("Error:");
                alertDialogBuilder.setMessage("Please check your internet connection");
                alertDialogBuilder.setCancelable(false);

                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                        return;
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
    }
}