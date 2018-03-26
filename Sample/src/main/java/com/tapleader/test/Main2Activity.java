package com.tapleader.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tapleader.Tapleader;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Tapleader.event("Main activity 2",0);
    }
}
