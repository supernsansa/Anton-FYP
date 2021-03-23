package com.example.antonsfyp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class NetworkTask extends AsyncTask<String, Void, String> {

    Context context;
    AlertDialog alertDialog;

    NetworkTask(Context ctx) {
        context = ctx;
    }
    @Override
    protected String doInBackground(String[] objects) {
        String type = objects[0];
        String wordName = objects[1];
        String wordDesc = objects[2];
        String username = objects[3];
        String login_url = "http://192.168.1.173:8080/FYP_Scripts/addWord.php";

        if(type.equals("AddWord")) {
            try {

                //Input data to post
                String post_data = URLEncoder.encode("wordName","UTF-8")+"="+URLEncoder.encode(wordName,"UTF-8")+"&"
                        +URLEncoder.encode("wordDesc","UTF-8")+"="+URLEncoder.encode(wordDesc,"UTF-8")+"&"
                        +URLEncoder.encode("userName","UTF-8")+"="+URLEncoder.encode(username,"UTF-8");

                //Post input data to PHP script
                URL url = new URL(login_url);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write(post_data);
                wr.flush();

                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                return sb.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String o) {
        //alertDialog.setMessage(o);
        //alertDialog.show();
        System.out.println(o);
        //Take user back to main menu
        Intent intent = new Intent (context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onPreExecute() {
        //alertDialog = new AlertDialog.Builder(context).create();
        //alertDialog.setTitle("Status");
    }

}
