package com.example.antonsfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;

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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WordActivity extends AppCompatActivity {

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    private String wordName;
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    private Word word;
    private List<Video> videoList = new ArrayList<>();
    private SimpleExoPlayer player;
    private String searchTerms;
    private boolean login_status = false;
    private String username = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        wordName = getIntent().getStringExtra("EXTRA_WORD_NAME");
        searchTerms = getIntent().getStringExtra("EXTRA_SEARCH_TERMS");

        //If user has logged in/registered, get their status and user name
        login_status = getIntent().getBooleanExtra("LOGIN_STATUS",false);
        if(login_status == true) {
            username = getIntent().getStringExtra("USERNAME");
        }
        //If user isn't logged in, edit and add video buttons should be restricted
        else {
            //Disable and gray out Edit button
            Button editButton = (Button) findViewById(R.id.editButton);
            editButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            editButton.setClickable(false);
            //Disable and gray out Add Video button
            Button addVidButton = (Button) findViewById(R.id.addVideoButton);
            addVidButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            addVidButton.setClickable(false);
            //Disable and gray out add tag button
            Button addTagButton = (Button) findViewById(R.id.addTagButton);
            addTagButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            addTagButton.setClickable(false);
        }

        new WordTask().execute();
    }

    @Override
    public void onBackPressed() {
        player.release();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        new VideoTask().execute();
    }

    //Takes user to edit description (not the word name itself)
    public void editWord(View view) {
        player.release();
        Intent intent = new Intent (this, AddDescActivity.class);
        intent.putExtra("TYPE", "edit");
        intent.putExtra("WORD_NAME" , wordName);
        intent.putExtra("CURRENT_DESC" , word.getDefinition());
        intent.putExtra("USERNAME", username);
        intent.putExtra("LOGIN_STATUS", login_status);
        startActivity(intent);
    }

    //Takes user to add video
    public void addVideo(View view) {
        player.release();
        Intent intent = new Intent (this, AddVideoActivity.class);
        intent.putExtra("TYPE", "edit");
        intent.putExtra("WORD_NAME" , wordName);
        intent.putExtra("CURRENT_DESC" , word.getDefinition());
        intent.putExtra("NUM_VIDEOS" , videoList.size());
        intent.putExtra("USERNAME", username);
        intent.putExtra("LOGIN_STATUS", login_status);
        startActivity(intent);
    }

    //Takes user to tag browse screen
    public void getTags(View view) {
        player.release();
        Intent intent = new Intent(this, BrowseTagActivity.class);
        intent.putExtra("TAG_SEARCH", false);
        intent.putExtra("EXTRA_TAG_NAME", "null");
        intent.putExtra("EXTRA_SEARCH_TERMS","null");
        intent.putExtra("USERNAME", username);
        intent.putExtra("LOGIN_STATUS", login_status);
        intent.putExtra("WORD_SPEC",true);
        intent.putExtra("WORD_NAME",wordName);
        startActivity(intent);
    }

    //This task fetches all the info associated with a word
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
                TextView wordDef = (TextView) findViewById(R.id.DescText);
                wordDef.setMovementMethod(new ScrollingMovementMethod());
                wordDef.setText(word.getDefinition());
                //TODO Username and date uploaded (not mandatory)
                //TextView dateUploaded = (TextView) findViewById(R.id.DateText);
                //dateUploaded.setText(word.getDateAdded());

                //Move to VideoTask
                new VideoTask().execute();

            } catch (JSONException e) {
                Toast.makeText(WordActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }

        }
    }

    //This task fetches the filenames of all videos associated with a word and adds these videos to the exoplayer playlist
    public class VideoTask extends AsyncTask<String, String, String> {

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
        protected String doInBackground(String... strings) {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL("http://192.168.1.173:8080/FYP_Scripts/fetchVideos.php");

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
                String post_data = URLEncoder.encode("wordName", "UTF-8") + "=" + URLEncoder.encode(wordName, "UTF-8");

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

                // Extract data from json and store into Video objects
                for(int i=0;i<jArray.length();i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    Video video = new Video("null","null","null","null");
                    video.setWord(json_data.getString("Word"));
                    video.setFileName(json_data.getString("FileName"));
                    video.setDateUploaded(json_data.getString("DateUploaded"));
                    video.setUserName(json_data.getString("User"));
                    videoList.add(video);
                }

                //Make instance of exoplayer
                player = new SimpleExoPlayer.Builder(WordActivity.this).build();
                // Bind the player to the view.
                StyledPlayerView playerView = (StyledPlayerView) findViewById(R.id.playerView);
                playerView.setPlayer(player);

                // Loop through videos and add to exoplayer playlist
                for (Video video: videoList) {
                    String uriPath = ("http://192.168.1.173:8080/FYP_Scripts/Videos/" + video.getFileName() + ".mp4");
                    Uri uri = Uri.parse(uriPath);
                    // Build the media item.
                    MediaItem mediaItem = MediaItem.fromUri(uri);
                    // Set the media item to be played.
                    player.addMediaItem(mediaItem);
                    //Mute the player
                    player.setVolume(0f);
                }

                // Prepare the player.
                player.prepare();
                // Start the playback.
                //player.play();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}