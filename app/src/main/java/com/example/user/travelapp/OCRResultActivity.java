package com.example.user.travelapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import org.json.JSONObject;

public class OCRResultActivity extends AppCompatActivity {

    private TextView banglaText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocrresult);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        banglaText = (TextView) findViewById(R.id.bangla_text);

        Bundle extra = getIntent().getExtras();

        if(extra != null){
            String text = extra.getString("response");

            try{
                JSONObject object = new JSONObject(text);

                if(object.getString("status").equals("success")){

                    String serverText = object.getString("response");

                    banglaText.setText(serverText);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

}
