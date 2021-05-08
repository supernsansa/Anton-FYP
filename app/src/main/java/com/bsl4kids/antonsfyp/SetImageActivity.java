package com.bsl4kids.antonsfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.exoplayer2.MediaItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

//This activity handles the setting of custom thumbnails for words
public class SetImageActivity extends AppCompatActivity {

    private static final int SELECT_IMAGE = 2;
    private static Intent imageFileData;
    private String wordName;
    private String wordDesc;
    private String filename;
    private boolean login_status = false;
    private String username = "null";
    UploadUtility uploadUtility;
    private int wordID;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_image);

        wordName = getIntent().getStringExtra("WORD_NAME");
        wordDesc = getIntent().getStringExtra("WORD_DESC");
        wordID = getIntent().getIntExtra("WORD_ID", 0);
        uploadUtility = new UploadUtility(this);
        uploadUtility.setImageMode(true);

        login_status = getIntent().getBooleanExtra("LOGIN_STATUS", false);
        if (login_status == true) {
            username = getIntent().getStringExtra("USERNAME");
        }
    }

    //Get image for upload
    public void chooseImageFromGallery(View view) {
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an Image "), SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageFileData = data;

        //Set image preview
        ImageView img = (ImageView) findViewById(R.id.imageView);
        img.setImageURI(imageFileData.getData());

        //Get file extension
        String pathFromUri = new URIPathHelper().getPath(this, imageFileData.getData());
        String extension = MimeTypeMap.getFileExtensionFromUrl(new File(pathFromUri).getPath());
        System.out.println(extension);
        //Create filename for storage on server
        filename = String.valueOf(wordID) + "." + extension;
        System.out.println(filename);
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

    public void uploadData(View view) {
        if (imageFileData != null) {
            new UploadImageTask().execute();
        } else {
            //If user has not provided an image, display an error message
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Error:");
            alertDialogBuilder.setMessage("Please provide an image");
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
        finish();
    }

    //Opens a thread to upload
    public class UploadImageTask extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(SetImageActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            return uploadUtility.uploadFile(imageFileData.getData(), filename);
        }

        @Override
        protected void onPostExecute(String result) {
            //pdLoading.dismiss();
            System.out.println(result);
            if (result.equals("success")) {
                new SetImageTask().execute();
            } else {
                //TODO Error dialog
                return;
            }
        }
    }

    //Opens a thread to update the wordb with the uploaded image's filename
    public class SetImageTask extends AsyncTask<String, String, String> {

        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                url = new URL("http://" + MainActivity.ip_address + "/FYP_Scripts/editImage.php");

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
                        + URLEncoder.encode("fileName", "UTF-8") + "=" + URLEncoder.encode(filename, "UTF-8");

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

            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SetImageActivity.this);
            alertDialogBuilder.setTitle("Status:");
            alertDialogBuilder.setMessage(result);
            alertDialogBuilder.setCancelable(false);

            final String response = result;
            System.out.println(response);

            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    //Take user back to word page
                    finish();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

}