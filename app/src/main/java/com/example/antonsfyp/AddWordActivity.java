package com.example.antonsfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.Gallery;

import java.util.concurrent.TimeUnit;

public class AddWordActivity extends AppCompatActivity {

    private static final int SELECT_VIDEO = 3;
    private String selectedPath = "";
    private static Intent VideoFileData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);
    }

    //Takes user input and adds a word entry to database
    //TODO Validation
    //TODO Probe db for availability
    public void addWordToDB(View view) {
        //Get editText boxes
        EditText editWordName = (EditText) findViewById(R.id.editWordName);
        //EditText editWordDesc = (EditText) findViewById(R.id.editWordDesc);
        //Extract strings from editText
        String wordName = editWordName.getText().toString();
        //String wordDesc = editWordDesc.getText().toString();
        //Create NetworkTask instance
        //String type = "AddWord";
        //NetworkTask task = new NetworkTask(this);
        //task.execute(type,wordName,wordDesc);
        //Upload video
        //UploadUtility uploadUtility = new UploadUtility(this);
        //uploadUtility.uploadFile(VideoFileData.getData(),(wordName + ".mp4"));

        //Take user to next activity
        Intent intent = new Intent (this, AddDescActivity.class);
        intent.putExtra("WORD_NAME",wordName);
        startActivity(intent);
    }

    //Get video for upload
    public void chooseVideoFromGallery(View view) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select a Video "), SELECT_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VideoFileData = data;
    }

}