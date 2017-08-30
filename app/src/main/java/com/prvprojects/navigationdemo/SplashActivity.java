package com.prvprojects.navigationdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.prvprojects.navigationdemo.utils.Constants;

import com.prvprojects.navigationdemo.activities.NavigationActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    Handler handler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(SplashActivity.this, NavigationActivity.class);
                startActivity(intent);
                finish();

            }
        }, Constants.TIME_OUT_SPLASH);

    }

}
