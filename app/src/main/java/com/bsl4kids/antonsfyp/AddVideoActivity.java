 package com.bsl4kids.antonsfyp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;

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
    private SimpleExoPlayer player;
    private boolean netError;
    private static final int STORAGE_PERMISSION_CODE = 101;

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
            filename = String.valueOf(wordID) + String.valueOf(vidIndex);
        }

        //Make instance of exoplayer
        player = new SimpleExoPlayer.Builder(AddVideoActivity.this).build();
        // Bind the player to the view.
        StyledPlayerView playerView = (StyledPlayerView) findViewById(R.id.previewPlayer);
        playerView.setPlayer(player);
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

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    //Checks if permission was granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Get video for upload
    public void chooseVideoFromGallery(View view) {
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select a Video "), SELECT_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VideoFileData = data;

        // Build the media item.
        MediaItem mediaItem = MediaItem.fromUri(new URIPathHelper().getPath(this, VideoFileData.getData()));
        //Mute the player
        player.setVolume(0f);
        //Prepare player
        player.setMediaItem(mediaItem);
        player.prepare();
    }

    public void uploadData(View view) {
        if (VideoFileData != null) {
            if (type.equals("add")) {
                new AddWordTask().execute();
            }
            else {
                new UploadTask().execute();
            }
        }
        else {
            //If user has not provided a video, display an error message
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddVideoActivity.this);
            alertDialogBuilder.setTitle("Error:");
            alertDialogBuilder.setMessage("Please provide a video");
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        player.release();
        finish();
    }

    //This task is responsible for creating an entry for a new word in the worddb
    public class AddWordTask extends AsyncTask<String, String, String> {

        //ProgressDialog pdLoading = new ProgressDialog(AddVideoActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL("http://" + MainActivity.ip_address + "/FYP_Scripts/addWord.php");

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
                netError = true;
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //pdLoading.dismiss();

            if(netError == true) {
                netErrorDialog();
            }

            try {

                JSONArray jArray = new JSONArray(result);

                // Extract data from json
                JSONObject json_data = jArray.getJSONObject(0);

                wordID = json_data.getInt("WordID");
                filename = String.valueOf(wordID) + String.valueOf(vidIndex);

                new UploadTask().execute();

            }
            catch (JSONException e) {
                e.printStackTrace();
                netErrorDialog();
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
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL("http://" + MainActivity.ip_address + "/FYP_Scripts/addVideo.php");
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
                netError = true;
                return e.toString();
            }
            finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //pdLoading.dismiss();

            if(netError == true) {
                netErrorDialog();
            }

            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddVideoActivity.this);
            alertDialogBuilder.setTitle("Status:");
            alertDialogBuilder.setMessage(result);
            alertDialogBuilder.setCancelable(false);

            final String response = result;
            System.out.println(response);

            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    if(response.equals("Success!")) {
                        System.out.println("Got here");
                    }
                    //Take user back to main menu
                    player.release();
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

    //Opens a thread to upload
    public class UploadTask extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(AddVideoActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
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
                netErrorDialog();
            }
        }
    }
}
