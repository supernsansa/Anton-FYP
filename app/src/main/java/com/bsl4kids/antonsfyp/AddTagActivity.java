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

//This activity handles the creation of new tags
public class AddTagActivity extends AppCompatActivity {

    private String wordName;
    private String tagInput;
    private boolean login_status = false;
    private String username = "null";
    private EditText editTagText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tag);

        wordName = getIntent().getStringExtra("WORD_NAME");

        login_status = getIntent().getBooleanExtra("LOGIN_STATUS",false);
        if(login_status == true) {
            username = getIntent().getStringExtra("USERNAME");
        }
    }

    public void addTagSubmit(View view) {
        editTagText = (EditText) findViewById(R.id.editTagText);
        tagInput = editTagText.getText().toString();

        new AddTagTask().execute();
    }

    public class AddTagTask extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(AddTagActivity.this);
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
                url = new URL("http://192.168.1.173:8080/FYP_Scripts/addTag.php");

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
                String post_data = URLEncoder.encode("wordName", "UTF-8") + "=" + URLEncoder.encode(wordName, "UTF-8")+"&"
                        +URLEncoder.encode("tagName","UTF-8")+"="+URLEncoder.encode(tagInput,"UTF-8");

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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddTagActivity.this);
            alertDialogBuilder.setTitle("Status:");
            alertDialogBuilder.setMessage(result);
            alertDialogBuilder.setCancelable(false);

            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    //Go back to previous activity
                    finish();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
}