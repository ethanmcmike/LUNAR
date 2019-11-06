package com.lunar.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.lunar.R;
import com.lunar.fragments.CommandFragment;
import com.lunar.fragments.DataFragment;
import com.lunar.fragments.LogFragment;
import com.lunar.fragments.MapFragment;
import com.lunar.interfaces.CommandHandler;
import com.lunar.models.InputData;
import com.lunar.utils.BTController;
import com.lunar.utils.DataListener;
import com.lunar.views.TimelineFragment;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH = 1;

    private static BTController bt = new BTController();

    private BottomNavigationView navView;

    //Fragments
    private DataFragment dataFragment;
    private MapFragment mapFragment;
    private CommandFragment commandFragment;
    private TimelineFragment timelineFragment;
    private LogFragment logFragment;

    private static final String RECEIVER_NAME = "Light Ring";

    private static final int COMMAND_DROGUE     = 0;
    private static final int COMMAND_BODY       = 1;
    private static final int COMMAND_MAIN       = 2;
    private static final int COMMAND_PAYLOAD    = 3;

    private boolean[] lights = new boolean[4];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        navView = findViewById(R.id.navigation_view);
        navView.setOnNavigationItemSelectedListener(navListener);

        dataFragment = DataFragment.getInstance();
        mapFragment = MapFragment.newInstance();
        commandFragment = new CommandFragment();
        commandFragment.setCommandHandler(commandHandler);
        timelineFragment = TimelineFragment.getInstance();
        logFragment = LogFragment.newInstance();

        //Set initial page
        setFragment(mapFragment);
        navView.setSelectedItemId(R.id.nav_map);

        //Setup bluetooth
        bt = BTController.getInstance();

        //Tie bluetooth to view listeners
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.bt_connect:
                connect();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    private void connect() {

        Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_LONG).show();

        try {
            bt.connect(RECEIVER_NAME);

        } catch (BTController.ConnectException e) {

            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

            switch (e.type) {

                case DISABLED:
                    Intent requestBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(requestBT, REQUEST_BLUETOOTH);
                    break;
            }

            return;
        }

        if (bt.isConnected()) {
            Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_LONG).show();
            bt.addDataListener(new AircraftInfoReceiver());

            logFragment.connect();
            timelineFragment.update(0);
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

    private class AircraftInfoReceiver extends DataListener {

        char start = '[';
        char end = ']';
        char del = ';';
        String buffer = "";

        @Override
        public void onDataReceived(byte[] data, int size) {

            System.out.println("Received data");

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

                final int addr = Integer.valueOf(data[0]);
                final float temp = Float.parseFloat(data[1]);
                final int alt = (int)Float.parseFloat(data[2]);
                final float lat = Float.parseFloat(data[3]);
                final float lon = Float.parseFloat(data[4]);

                final InputData inputData = new InputData(alt, temp, lat, lon);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dataFragment.update(inputData);
//                        logFragment.receive();
                        mapFragment.moveRocket(lat, lon);
                    }
                });
            }
        }
    }

    BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch(menuItem.getItemId()) {

                case R.id.nav_data:
                    setFragment(dataFragment);
                    break;

                case R.id.nav_map:
                    setFragment(mapFragment);
                    break;

                case R.id.nav_timeline:
                    setFragment(timelineFragment);
                    break;

                case R.id.nav_commands:
                    setFragment(commandFragment);
                    break;

                case R.id.nav_log:
                    setFragment(logFragment);
                    break;
            }

            return true;
        }
    };

    CommandHandler commandHandler = new CommandHandler() {
        @Override
        public void onCommandSent(int stringId) {

            String msg;

            switch(stringId){

                case R.string.command_drogue:
                    System.out.println("Drogue");
                    lights[0] = !lights[0];
                    msg = "[0;";
                    msg += lights[0] ? 1:0;
                    msg += "]";
                    bt.send(msg);
                    break;

                case R.string.command_body:
                    System.out.println("Body");
                    lights[1] = !lights[1];
                    msg = "[1;";
                    msg += lights[1] ? 1:0;
                    msg += "]";
                    bt.send(msg);
                    break;

                case R.string.command_main:
                    System.out.println("Main");
                    lights[2] = !lights[2];
                    msg = "[2;";
                    msg += lights[2] ? 1:0;
                    msg += "]";
                    bt.send(msg);
                    break;

                case R.string.command_payload:
                    System.out.println("Payload");
                    lights[3] = !lights[3];
                    msg = "[3;";
                    msg += lights[3] ? 1:0;
                    msg += "]";
                    bt.send(msg);
                    break;
            }
        }
    };
}
