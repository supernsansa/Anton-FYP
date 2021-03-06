package com.bsl4kids.antonsfyp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

//This activity takes user input for the definition of a new/existing word
public class AddDescActivity extends AppCompatActivity {

    private String type;
    private String wordName;
    private String current_desc;
    private EditText editWordDesc;
    private String descInput;
    private boolean login_status = false;
    private String username = "null";
    private boolean netError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_desc);
        type = getIntent().getStringExtra("TYPE");
        wordName = getIntent().getStringExtra("WORD_NAME");

        login_status = getIntent().getBooleanExtra("LOGIN_STATUS",false);
        if(login_status == true) {
            username = getIntent().getStringExtra("USERNAME");
        }

        if(type.equals("edit")) {
            current_desc = getIntent().getStringExtra("CURRENT_DESC");
            editWordDesc = (EditText) findViewById(R.id.editDefBox);
            editWordDesc.setText(current_desc);
            TextView textView = (TextView) findViewById(R.id.DescTitle);
            textView.setText("Edit Definition");
        }
    }

    //Takes user input and proceeds to next activity
    public void moveToVideoSelect(View view) {
        editWordDesc = (EditText) findViewById(R.id.editDefBox);
        descInput = editWordDesc.getText().toString();

        if(descInput.trim().length() > 0 && descInput != null) {
            if(type.equals("add")) {
                //Take user to next activity
                Intent intent = new Intent (this, AddVideoActivity.class);
                intent.putExtra("WORD_NAME",wordName);
                intent.putExtra("WORD_DESC",descInput);
                intent.putExtra("TYPE",type);
                intent.putExtra("USERNAME", username);
                intent.putExtra("LOGIN_STATUS", login_status);
                startActivity(intent);
            }
            else {
                new EditTask().execute();
            }
        }
        else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Error:");
            alertDialogBuilder.setMessage("Please enter a short definition/description");
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

    //Creates an alert dialog
    public void netErrorDialog() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Error:");
        alertDialogBuilder.setMessage("Please check your internet connection");
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //Return to previous activity
                finish();
            }
        });

        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public class EditTask extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(AddDescActivity.this);
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
                url = new URL("http://" + MainActivity.ip_address + "/FYP_Scripts/editWord.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                netError = true;
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
                netError = true;
                return e1.toString();
            }

            try {
                //Encode data to post
                String post_data = URLEncoder.encode("wordName", "UTF-8") + "=" + URLEncoder.encode(wordName, "UTF-8")+"&"
                        +URLEncoder.encode("wordDesc","UTF-8")+"="+URLEncoder.encode(descInput,"UTF-8");

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
                netError = true;
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            pdLoading.dismiss();

            if(netError == true) {
                netErrorDialog();
            }

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddDescActivity.this);
            alertDialogBuilder.setTitle("Status:");
            alertDialogBuilder.setMessage(result);
            alertDialogBuilder.setCancelable(false);

            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    //Take user back to main menu
                    Intent intent = new Intent(AddDescActivity.this , MainActivity.class);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("LOGIN_STATUS", login_status);
                    startActivity(intent);
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

}
