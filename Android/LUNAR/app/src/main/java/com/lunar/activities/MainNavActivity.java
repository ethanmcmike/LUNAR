package com.lunar.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import com.lunar.R;
import com.lunar.models.Aircraft;
import com.lunar.models.MainVM;
import com.lunar.utils.BTController;
import com.lunar.interfaces.DataListener;
import com.lunar.views.AircraftInfoView;
import com.lunar.views.ArrowView;

public class MainNavActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH = 1;

    private AircraftInfoView rocketView, payloadView;
    private Map<Integer, Aircraft> aircraftMap;
    private ArrowView compassView;

    private MainVM model;

    private static final String RECEIVER_NAME = "Light Ring";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_fragment);

        //Init models
        model = new MainVM();
        model.rocket = new Aircraft("Rocket", 2);
        model.payload = new Aircraft("Payload", 3);

        aircraftMap = new HashMap();
        aircraftMap.put(model.rocket.address, model.rocket);
        aircraftMap.put(model.payload.address, model.payload);

        //Find views
//        rocketView = findViewById(R.id.rocket_view);
//        rocketView.setAircraft(model.rocket);
//        payloadView = findViewById(R.id.payload_view);
//        payloadView.setAircraft(model.payload);
//        compassView = findViewById(R.id.arrow_view);

//        TimelineAdapter timelineAdapter = new TimelineAdapter(this, timelineView.getTimeline());
//        timelineView.setAdapter(timelineAdapter);

        //Setup bluetooth
        model.bt = BTController.getInstance();

        //Tie bluetooth to views
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.bt_connect:
                connect();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void connect(){

        Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_LONG).show();

        try {
            model.bt.connect(RECEIVER_NAME);

        } catch (BTController.ConnectException e) {

            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

            switch (e.type){

                case DISABLED:
                    Intent requestBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(requestBT, REQUEST_BLUETOOTH);
                    break;
            }

            return;
        }

        if(model.bt.isConnected()) {
            Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_LONG).show();
            model.bt.addDataListener(new AircraftInfoReceiver());
        }

        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth was not enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AircraftInfoReceiver implements DataListener {

        char start = '[';
        char end = ']';
        char del = ';';
        String buffer = "";

        @Override
        public void onDataReceived(byte[] data, int size) {

            //Parse data
            for(int i=0; i<size; i++) {

                char c = (char)data[i];

                if(c == start){
                    buffer = "";
                }
                else if(c == end){
                    handleData();
                    buffer = "";
                }
                else if(buffer.length() <= 256){

                    if(Character.isDigit(c) || c == del || c == '.') {
                        buffer += c;
                    }
                }
                else{
                    buffer = "";
                }
            }
        }

        private void handleData(){

            String[] data = buffer.split(Character.toString(del));

            if(data.length == 5){

                int addr = Integer.valueOf(data[0]);
                float temp = Float.parseFloat(data[1]);
                int alt = (int)Float.parseFloat(data[2]);
                float lat = Float.parseFloat(data[3]);
                float lon = Float.parseFloat(data[4]);

                final Aircraft aircraft = aircraftMap.get(addr);

                if(aircraft != null) {
                    aircraft.temp = temp;
                    aircraft.altitude = alt;
                    aircraft.lat = lat;
                    aircraft.lon = lon;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            aircraft.view.update();
                        }
                    });
                }
            }
        }
    }
}
