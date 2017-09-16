package com.tapleader.test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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

        int permissionCheck_phone_state = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int permissionCheck_get_accounts = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        int permissionCheck_fine_location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck_coarse_location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permissionCheck_phone_state != PackageManager.PERMISSION_GRANTED
                || permissionCheck_coarse_location != PackageManager.PERMISSION_GRANTED
                || permissionCheck_fine_location != PackageManager.PERMISSION_GRANTED
                || permissionCheck_get_accounts != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this
                    ,new String[]{Manifest.permission.READ_PHONE_STATE
                            , Manifest.permission.GET_ACCOUNTS
                            , Manifest.permission.ACCESS_FINE_LOCATION
                            , Manifest.permission.ACCESS_COARSE_LOCATION}
                    , REQUEST_READ_PHONE_STATE);
        }

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

