package com.example.antonsfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class WordActivity extends AppCompatActivity {

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    private String wordName;
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    private Word word;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        wordName = getIntent().getStringExtra("EXTRA_WORD_NAME");
        new WordTask().execute();
    }

    //Takes user to edit description (not the word name itself)
    //TODO add support for multiple videos per word
    //TODO allow tag editing
    public void editWord(View view) {
        Intent intent = new Intent (this, AddDescActivity.class);
        intent.putExtra("TYPE", "edit");
        intent.putExtra("WORD_NAME" , wordName);
        intent.putExtra("CURRENT_DESC" , word.getDefinition());
        startActivity(intent);
    }

    public class WordTask extends AsyncTask<String, String, String> {

        Context context;
        ProgressDialog pdLoading = new ProgressDialog(WordActivity.this);
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
        protected String doInBackground(String[] objects) {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL("http://192.168.1.173:8080/FYP_Scripts/fetchWordInfo.php");

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

            //this method will be running on UI thread

            pdLoading.dismiss();

            pdLoading.dismiss();
            try {

                JSONArray jArray = new JSONArray(result);

                // Extract data from json and store into Word object
                JSONObject json_data = jArray.getJSONObject(0);
                word = new Word("null", "null", "null");

                word.setName(json_data.getString("WordName"));
                word.setDefinition(json_data.getString("Definition"));
                word.setDateAdded(json_data.getString("DateAdded"));

                //Place text in appropriate UI elements
                TextView wordTitle = (TextView) findViewById(R.id.WordNameText);
                wordTitle.setText(word.getName());
                TextView wordDef = (TextView) findViewById(R.id.DefinitionText);
                wordDef.setText(word.getDefinition());
                TextView dateUploaded = (TextView) findViewById(R.id.DateText);
                dateUploaded.setText(word.getDateAdded());
                //Load in video
                VideoView videoView = (VideoView) findViewById(R.id.videoView);
                MediaController mediacontroller = new MediaController(WordActivity.this);
                mediacontroller.setAnchorView(videoView);
                String uriPath = ("http://192.168.1.173:8080/FYP_Scripts/Videos/" + word.getName() + ".mp4");
                Uri uri = Uri.parse(uriPath);
                videoView.setMediaController(mediacontroller);
                videoView.setVideoURI(uri);
                videoView.requestFocus();
                videoView.start();

            } catch (JSONException e) {
                Toast.makeText(WordActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }

        }
    }
}