package com.bsl4kids.antonsfyp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jibble.simpleftp.*;

public class AddVideoActivity extends AppCompatActivity {

    private static final int SELECT_VIDEO = 3;
    private String selectedPath = "";
    private static Intent VideoFileData;
    private String wordName;
    private String wordDesc;
    private String type;
    private int vidIndex;
    private String filename;
    private boolean login_status = false;
    private String username = "null";
    UploadUtility uploadUtility;
    private int wordID;
    private Uri fileUri;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);
        wordName = getIntent().getStringExtra("WORD_NAME");
        wordDesc = getIntent().getStringExtra("WORD_DESC");
        type = getIntent().getStringExtra("TYPE");
        uploadUtility = new UploadUtility(this);

        login_status = getIntent().getBooleanExtra("LOGIN_STATUS",false);
        if(login_status == true) {
            username = getIntent().getStringExtra("USERNAME");
        }

        if (type.equals("edit")) {
            TextView textView = (TextView) findViewById(R.id.addVideoTitle);
            textView.setText("Add a Video");
            vidIndex = getIntent().getIntExtra("NUM_VIDEOS",0);
            wordID = getIntent().getIntExtra("WORD_ID",0);
            //TODO change filename system to wordID+vidIndex.mp4
            filename = String.valueOf(wordID) + String.valueOf(vidIndex);
        }

    }

    //Get video for upload
    public void chooseVideoFromGallery(View view) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select a Video "), SELECT_VIDEO);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VideoFileData = data;

        //the selected video
        fileUri = data.getData();
        System.out.println(fileUri.getPath());
        file = new File(fileUri.getPath());
        try {
            System.out.println(file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(file.getPath());
        System.out.println(file.getName());
        System.out.println(new URIPathHelper().getPath(this, fileUri));
        file = new File(new URIPathHelper().getPath(this, fileUri));
        System.out.println(file.getPath());
    }

    //TODO Display video preview

    public void uploadData(View view) {
        if (type.equals("add")) {
            new AddWordTask().execute();
            //Upload video
            //TODO change filename system to wordID.mp4
            //uploadUtility.uploadFile(VideoFileData.getData(),(wordName + ".mp4"));
            //Create NetworkTask instance
            //String type = "AddWord";
            //NetworkTask task = new NetworkTask(this);
            //task.execute(type,wordName,wordDesc,username);
        }
        else {
            new AddVideoTask().execute();
        }
    }

    //This task is responsible for creating an entry for a new word in the worddb
    public class AddWordTask extends AsyncTask<String, String, String> {

        //ProgressDialog pdLoading = new ProgressDialog(AddVideoActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Show loading dialog
           // pdLoading.setMessage("\tLoading...");
            //pdLoading.setCancelable(false);
            //pdLoading.show();

        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL("http://192.168.1.173:8080/FYP_Scripts/addWord.php");

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
                String post_data = URLEncoder.encode("wordName", "UTF-8") + "=" + URLEncoder.encode(wordName, "UTF-8") + "&"
                        + URLEncoder.encode("wordDesc", "UTF-8") + "=" + URLEncoder.encode(wordDesc, "UTF-8");

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
            //pdLoading.dismiss();

            try {

                JSONArray jArray = new JSONArray(result);

                // Extract data from json
                JSONObject json_data = jArray.getJSONObject(0);

                wordID = json_data.getInt("WordID");
                filename = String.valueOf(wordID) + String.valueOf(vidIndex);

                new uploadTask().execute();

            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Opens a thread to create an entry into the videodb
    public class AddVideoTask extends AsyncTask<String, String, String> {

        //ProgressDialog pdLoading = new ProgressDialog(AddVideoActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Show loading dialog
            //pdLoading.setMessage("\tLoading...");
            //pdLoading.setCancelable(false);
            //pdLoading.show();

        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL("http://192.168.1.173:8080/FYP_Scripts/addVideo.php");

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
                        +URLEncoder.encode("fileName","UTF-8")+"="+URLEncoder.encode(filename,"UTF-8")+"&"
                        +URLEncoder.encode("userName","UTF-8")+"="+URLEncoder.encode(username,"UTF-8");

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
            }
            finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //pdLoading.dismiss();

            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddVideoActivity.this);
            alertDialogBuilder.setTitle("Status:");
            alertDialogBuilder.setMessage(result);
            alertDialogBuilder.setCancelable(false);

            final String response = result;
            System.out.println(response);

            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    if(response.equals("Video entry created")) {
                        System.out.println("Got here");
                    }

                    //TODO try and change this so that loading dialog can finish first
                    //Take user back to main menu
                    Intent intent = new Intent(AddVideoActivity.this , MainActivity.class);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("LOGIN_STATUS", login_status);
                    startActivity(intent);
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    //Opens a thread to create an entry into the videodb
    public class uploadTask extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(AddVideoActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /**
            //Show loading dialog
            pdLoading.setMessage("\tUploading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
             */

        }

        @Override
        protected String doInBackground(String... strings) {
            /**
            try
            {
                SimpleFTP ftp = new SimpleFTP();

                // Connect to an FTP server on port 21.
                ftp.connect("unix.sussex.ac.uk", 22, "anoc20", "Kasuba123!");

                // Set binary mode.
                ftp.bin();

                // Change to a new working directory on the FTP server.
                //ftp.cwd("");

                // You can also upload from an InputStream, e.g.
                ftp.stor(new FileInputStream(file), filename);

                // Quit from the FTP server.
                ftp.disconnect();
                return "success";
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return "failure";
            }
             */
            return uploadUtility.uploadFile(VideoFileData.getData(),(filename + ".mp4"));
        }

        @Override
        protected void onPostExecute(String result) {
            //pdLoading.dismiss();
            System.out.println(result);
            if(result.equals("success")) {
                new AddVideoTask().execute();
            }
            else {
                return;
            }
        }
    }
}
