package com.bsl4kids.antonsfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

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

//Users can browse, tap, and search for tags on this screen
//Made using retooled BrowseActivity code
public class BrowseTagActivity extends AppCompatActivity implements OnItemClickListener {

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    private RecyclerView browseTable;
    private WordPreviewAdapter wordPreviewAdapter;
    private String searchTerms;
    List<WordPreview> data = new ArrayList<>();
    private boolean login_status = false;
    private String username = "null";
    private boolean word_spec = false;
    private String word_name = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        searchTerms = getIntent().getStringExtra("EXTRA_SEARCH_TERMS");
        new BrowseTagActivity.FetchTask().execute();

        SearchView searchView = findViewById(R.id.BrowseSearch);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //When users search on this screen, take them to another instance of this class with relevant search results
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Load in search results
                Intent intent = new Intent (BrowseTagActivity.this, BrowseTagActivity.class);
                intent.putExtra("EXTRA_SEARCH_TERMS", query);
                intent.putExtra("USERNAME", username);
                intent.putExtra("LOGIN_STATUS", login_status);
                startActivity(intent);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //Disable the sort button
        ImageButton sortButton = (ImageButton) findViewById(R.id.sortButton);
        sortButton.setVisibility(View.GONE);

        //Get login status and username
        login_status = getIntent().getBooleanExtra("LOGIN_STATUS",false);
        if(login_status == true) {
            username = getIntent().getStringExtra("USERNAME");
        }

        //Get word for which tags will be displayed and disable search bar
        word_spec = getIntent().getBooleanExtra("WORD_SPEC",false);
        if(word_spec == true) {
            word_name = getIntent().getStringExtra("WORD_NAME");
            searchView.setVisibility(View.GONE);
        }
    }

    //Takes user to list of relevant words when a tag is tapped
    @Override
    public void onClick(View view, int position) {
        // The onClick implementation of the RecyclerView item click
        final String tagName = data.get(position).getName();
        Intent intent = new Intent(this, BrowseActivity.class);
        intent.putExtra("TAG_SEARCH", true);
        intent.putExtra("EXTRA_TAG_NAME", tagName);
        intent.putExtra("EXTRA_SEARCH_TERMS",searchTerms);
        intent.putExtra("USERNAME", username);
        intent.putExtra("LOGIN_STATUS", login_status);
        intent.putExtra("SORT_BY", "A");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    //Creates an alert dialog
    public void netErrorDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BrowseTagActivity.this);
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

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //Fetches all tag types from tagdb
    public class FetchTask extends AsyncTask<String, String, String> {

        Context context;
        ProgressDialog pdLoading = new ProgressDialog(BrowseTagActivity.this);
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
            if (searchTerms.equals("*")) {
                return getAllTags();
            }
            else if(word_spec == true) {
                return getWordTags();
            }
            else {
                return getSearchResults();
            }
        }

        //Gets all tags
        protected String getAllTags() {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL("http://" + MainActivity.ip_address + "/FYP_Scripts/fetchTagBrowse.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                netErrorDialog();
                return e.toString();
            }

            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");

                // setDoOutput to true as we receive data from json file
                conn.setDoOutput(true);

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                netErrorDialog();
                return e1.toString();
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {
                    netErrorDialog();
                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                netErrorDialog();
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        //Request regular text search
        protected String getSearchResults() {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL("http://" + MainActivity.ip_address + "/FYP_Scripts/searchTagDB.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                netErrorDialog();
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
                netErrorDialog();
                return e1.toString();
            }

            try {
                //Encode data to post
                String post_data = URLEncoder.encode("tagName","UTF-8")+"="+URLEncoder.encode(searchTerms,"UTF-8");

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
                netErrorDialog();
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        //Get all words associated with a tag
        protected String getWordTags() {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL("http://" + MainActivity.ip_address + "/FYP_Scripts/fetchTagWord.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                netErrorDialog();
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
                netErrorDialog();
                return e1.toString();
            }

            try {
                //Encode data to post
                String post_data = URLEncoder.encode("wordName","UTF-8")+"="+URLEncoder.encode(word_name,"UTF-8");

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
                netErrorDialog();
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            pdLoading.dismiss();
            try {

                JSONArray jArray = new JSONArray(result);

                // Extract data from json and store into ArrayList as class objects
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    WordPreview wordPreview = new WordPreview("null", 0);

                    wordPreview.setName(json_data.getString("TagName"));
                    wordPreview.setLikes(-1);

                    data.add(wordPreview);
                }

                //If no tags are found
                if(data.size() == 0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BrowseTagActivity.this);
                    alertDialogBuilder.setTitle("Error:");
                    alertDialogBuilder.setMessage("No tags found");
                    alertDialogBuilder.setCancelable(false);

                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            //Return to previous activity
                            finish();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

                // Setup and Handover data to recyclerview
                browseTable = (RecyclerView) findViewById(R.id.wordTable);
                browseTable.addItemDecoration(new DividerItemDecoration(BrowseTagActivity.this, DividerItemDecoration.VERTICAL));
                wordPreviewAdapter = new WordPreviewAdapter(BrowseTagActivity.this, data);
                wordPreviewAdapter.setClickListener(BrowseTagActivity.this); // Bind the listener
                browseTable.setAdapter(wordPreviewAdapter);
                browseTable.setLayoutManager(new LinearLayoutManager(BrowseTagActivity.this));

            } catch (JSONException e) {
                Toast.makeText(BrowseTagActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }

            conn.disconnect();

        }
    }
}