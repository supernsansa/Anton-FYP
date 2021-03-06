package com.bsl4kids.antonsfyp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class BrowseActivity extends AppCompatActivity implements OnItemClickListener {

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    private RecyclerView browseTable;
    private WordPreviewAdapter wordPreviewAdapter;
    private String searchTerms;
    List<WordPreview> data = new ArrayList<>();
    private boolean login_status = false;
    private String username = "null";
    private boolean tag_search_status = false;
    private String tagName = "null";
    private String sortMode;
    private boolean netError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        searchTerms = getIntent().getStringExtra("EXTRA_SEARCH_TERMS");
        sortMode = getIntent().getStringExtra("SORT_BY");
        System.out.println("Sort mode " + sortMode);
        new FetchTask().execute();

        //Handles searching using the search bar
        SearchView searchView = findViewById(R.id.BrowseSearch);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Load in search results
                Intent intent = new Intent (BrowseActivity.this, BrowseActivity.class);
                intent.putExtra("EXTRA_SEARCH_TERMS", query);
                intent.putExtra("USERNAME", username);
                intent.putExtra("LOGIN_STATUS", login_status);
                intent.putExtra("SORT_BY", "A");
                startActivity(intent);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //Get login status and (if true) the user's username
        login_status = getIntent().getBooleanExtra("LOGIN_STATUS",false);
        if(login_status == true) {
            username = getIntent().getStringExtra("USERNAME");
        }

        //Check if this is from a tag click redirect, if so, get the tag name and disable the search bar
        tag_search_status = getIntent().getBooleanExtra("TAG_SEARCH",false);
        if(tag_search_status == true) {
            tagName = getIntent().getStringExtra("EXTRA_TAG_NAME");
            searchView.setVisibility(View.GONE);
        }
    }

    //Takes user to word page when word is tapped
    @Override
    public void onClick(View view, int position) {
        // The onClick implementation of the RecyclerView item click
        final String wordName = data.get(position).getName();
        Intent intent = new Intent(this, WordActivity.class);
        intent.putExtra("EXTRA_WORD_NAME", wordName);
        intent.putExtra("EXTRA_SEARCH_TERMS",searchTerms);
        intent.putExtra("USERNAME", username);
        intent.putExtra("LOGIN_STATUS", login_status);
        startActivity(intent);
    }

    //Creates a dialog to select how the search results are sorted
    public void sortDialog(View view) {
        String[] options = {"A-Z", "Z-A", "Most Likes", "Fewest Likes"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort Mode:");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Reload BrowseActivity with
                Intent intent = new Intent (BrowseActivity.this, BrowseActivity.class);
                intent.putExtra("EXTRA_SEARCH_TERMS", searchTerms);
                intent.putExtra("USERNAME", username);
                intent.putExtra("LOGIN_STATUS", login_status);
                intent.putExtra("TAG_SEARCH", tag_search_status);
                intent.putExtra("EXTRA_TAG_NAME", tagName);

                if(which == 0) {
                    intent.putExtra("SORT_BY", "A");
                }
                else if(which == 1) {
                    intent.putExtra("SORT_BY", "Z");
                }
                else if(which == 2) {
                    intent.putExtra("SORT_BY", "ML");
                }
                else if(which == 3) {
                    intent.putExtra("SORT_BY", "FL");
                }

                startActivity(intent);
                finish();
            }
        });
        builder.show();
    }

    //Creates an alert dialog
    public void netErrorDialog() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(BrowseActivity.this);
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

    @Override
    public void onBackPressed() {
        finish();
    }

    public class FetchTask extends AsyncTask<String, String, String> {

        Context context;
        ProgressDialog pdLoading = new ProgressDialog(BrowseActivity.this);
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

        //Select the relevant fetching method depending on circumstance
        @Override
        protected String doInBackground(String... strings) {
            if (tag_search_status == true) {
                return getTagWords();
            }
            else {
                if (searchTerms.equals("*")) {
                    return getAllWords();
                }
                else {
                    return getSearchResults();
                }
            }
        }

        //Gets all words from the database
        protected String getAllWords() {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL("http://" + MainActivity.ip_address + "/FYP_Scripts/fetchWordBrowse.php");

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
                String post_data = URLEncoder.encode("sortMode","UTF-8")+"="+URLEncoder.encode(sortMode,"UTF-8");

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
                netError = true;
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
                url = new URL("http://" + MainActivity.ip_address + "/FYP_Scripts/searchDB.php");

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
                String post_data = URLEncoder.encode("wordName","UTF-8")+"="+URLEncoder.encode(searchTerms,"UTF-8")+"&"
                        +URLEncoder.encode("sortMode","UTF-8")+"="+URLEncoder.encode(sortMode,"UTF-8");

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
                netError = true;
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        //Request all words associated with a given tag
        protected String getTagWords() {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL("http://" + MainActivity.ip_address + "/FYP_Scripts/searchByTag.php");

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
                String post_data = URLEncoder.encode("tagName","UTF-8")+"="+URLEncoder.encode(tagName,"UTF-8")+"&"
                        +URLEncoder.encode("sortMode","UTF-8")+"="+URLEncoder.encode(sortMode,"UTF-8");

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
                netError = true;
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

            try {

                JSONArray jArray = new JSONArray(result);

                // Extract data from json and store into ArrayList as class objects
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    WordPreview wordPreview = new WordPreview("null", 0);

                    wordPreview.setName(json_data.getString("WordName"));
                    wordPreview.setLikes(json_data.getInt("Likes"));

                    data.add(wordPreview);
                }

                //If a network error occurs
                if(netError == true) {
                    netErrorDialog();
                }

                //If no tags are found
                if(data.size() == 0) {
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(BrowseActivity.this);
                    alertDialogBuilder.setTitle("Error:");
                    alertDialogBuilder.setMessage("No words found");
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

                // Setup and Handover data to recyclerview
                browseTable = (RecyclerView) findViewById(R.id.wordTable);
                browseTable.addItemDecoration(new DividerItemDecoration(BrowseActivity.this, DividerItemDecoration.VERTICAL));
                wordPreviewAdapter = new WordPreviewAdapter(BrowseActivity.this, data);
                wordPreviewAdapter.setClickListener(BrowseActivity.this); // Bind the listener
                browseTable.setAdapter(wordPreviewAdapter);
                browseTable.setLayoutManager(new LinearLayoutManager(BrowseActivity.this));

            } catch (JSONException e) {
                Toast.makeText(BrowseActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                netErrorDialog();
            }

            conn.disconnect();

        }
    }
}


