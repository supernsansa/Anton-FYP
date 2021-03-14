package com.example.antonsfyp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddVideoActivity extends AppCompatActivity {

    private static final int SELECT_VIDEO = 3;
    private String selectedPath = "";
    private static Intent VideoFileData;
    private String wordName;
    private String wordDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);
        wordName = getIntent().getStringExtra("WORD_NAME");
        wordDesc = getIntent().getStringExtra("WORD_DESC");
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

    //TODO Display video preview

    public void uploadData(View view) {
        //TODO change below to support multiple videos per word in db
        //Upload video
        UploadUtility uploadUtility = new UploadUtility(this);
        uploadUtility.uploadFile(VideoFileData.getData(),(wordName + ".mp4"));
        //Create NetworkTask instance
        //TODO change line below
        String type = "AddWord";
        NetworkTask task = new NetworkTask(this);
        task.execute(type,wordName,wordDesc);

        //Take user back to main menu
        Intent intent = new Intent (this, MainActivity.class);
        startActivity(intent);
    }
}
