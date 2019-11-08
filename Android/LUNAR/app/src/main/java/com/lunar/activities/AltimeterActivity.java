package com.lunar.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.lunar.R;
import com.lunar.utils.BTController;
import com.lunar.interfaces.DataListener;

public class AltimeterActivity extends AppCompatActivity {

    private Button setButton;

    private BTController bt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.altimeter_activity);
        setTitle(getResources().getString(R.string.activity_altimeter));

        setButton = findViewById(R.id.set_altimeter_button);

        setButton.setOnClickListener(buttonListener);

        //Get bluetooth controller instance
        bt = BTController.getInstance();
        bt.addDataListener(new AltitudeReceivedListener());
    }

    Button.OnClickListener buttonListener = new Button.OnClickListener(){

        @Override
        public void onClick(View view) {

            //Send altimeter setting to rocket
            finish();
        }
    };

    private class AltitudeReceivedListener implements DataListener{

        @Override
        public void onDataReceived(byte[] data, int size) {

            //Validate message

            //Close activity
            finish();
        }
    }
}
