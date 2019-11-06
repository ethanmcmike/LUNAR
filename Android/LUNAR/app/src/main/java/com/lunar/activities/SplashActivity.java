package com.lunar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.lunar.R;

public class SplashActivity extends AppCompatActivity {

    private static final int DELAY = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        new Thread(new Runnable() {

            long startTime = System.currentTimeMillis();

            @Override
            public void run() {

                while(System.currentTimeMillis() < startTime + DELAY){

                }

                gotoMain();
            }
        }).start();
    }

    private void gotoMain(){
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
