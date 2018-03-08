package com.tapleader.test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity{

    private static final int REQUEST_READ_PHONE_STATE = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Main2Activity.class));
            }
        });

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.GET_ACCOUNTS}, REQUEST_READ_PHONE_STATE);
        }

        findViewById(R.id.btn_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double value=null;
                int count=0;
                try {
                    String name = ((EditText) findViewById(R.id.eventName)).getText().toString();
                    try {
                        value = Double.valueOf(((EditText) findViewById(R.id.eventValue)).getText().toString());
                    }catch (Exception e){
                        Toast.makeText(MainActivity.this,"مقدار value باید عددی باشد",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    try {
                        if(!((EditText) findViewById(R.id.eventValue)).getText().toString().isEmpty())
                            count = Integer.valueOf(((EditText) findViewById(R.id.count)).getText().toString());
                    }catch (Exception e){
                        Toast.makeText(MainActivity.this,"مقدار count باید عددی باشد",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    HashMap<String, Double> d = new HashMap<>();
                    for (int i = 0; i < count; i++) {
                        d.put("My details key " + i, new Random().nextDouble());
                    }
                    if(name!=null && value!=null)
                        com.tapleader.Tapleader.event(name, value, d);
                    else
                        Toast.makeText(MainActivity.this,"فیلد name و value اجباریست!",Toast.LENGTH_SHORT).show();

                    Toast.makeText(getBaseContext(), "Event saved!", Toast.LENGTH_SHORT).show();
                    ((EditText) findViewById(R.id.eventName)).setText("Sample Name");
                    ((EditText) findViewById(R.id.eventValue)).setText("1.4");
                    ((EditText) findViewById(R.id.count)).setText("0");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                }
                break;

            default:
                break;
        }
    }

}

