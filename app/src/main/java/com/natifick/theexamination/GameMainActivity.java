package com.natifick.theexamination;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.Window;

public class GameMainActivity extends AppCompatActivity {

    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new GameView(this));
        context = getApplicationContext();
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
    }
}