package com.tapleader.test;

import android.app.Application;

import com.tapleader.Tapleader;

/**
 * Created by mehdi akbarian on 2017-06-21.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Tapleader.initialize(this);
    }
}
