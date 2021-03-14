package com.example.antonsfyp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

//This activity takes user input for the definition of a new/existing word
public class AddDescActivity extends AppCompatActivity {

    private String wordName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_desc);
        wordName = getIntent().getStringExtra("WORD_NAME");
    }

    //Takes user input and proceeds to next activity
    public void moveToVideoSelect(View view) {
        EditText editWordDesc = (EditText) findViewById(R.id.editDefBox);
        String descInput = editWordDesc.getText().toString();
        //TODO get word name from AddWordActivity
        //TODO pass word name and descInput to video select activity
        //Take user to next activity
        Intent intent = new Intent (this, AddVideoActivity.class);
        intent.putExtra("WORD_NAME",wordName);
        intent.putExtra("WORD_DESC",descInput);
        startActivity(intent);
    }

}
