package com.lunar.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Set;

import com.lunar.R;
import com.lunar.utils.BTController;
import com.lunar.utils.GPSTracker;
import com.lunar.views.ArrowView;
import com.lunar.views.LevelView;

public class NDBActivity extends AppCompatActivity implements SensorEventListener {

    public static final int REQUEST_LOCATION = 0;

    public static final int GPS_TIME = 500;
    public static final float GPS_MOVE = 0.01f;

    private TextView latitudeText, longitudeText;
    private ArrowView arrowView;
    private Button locateButton;
    private LevelView levelView;

    private SensorManager sensorManager;
    private Sensor gyroscope, compass;
    private GPSTracker gpsTracker;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String provider;

    private BTController bt;
    private static String RECEIVER_NAME = "Light Ring";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ndb_activity);

        latitudeText = findViewById(R.id.latitude);
        longitudeText = findViewById(R.id.longitude);
        arrowView = findViewById(R.id.arrow_view);
        locateButton = findViewById(R.id.locate_button);
        levelView = findViewById(R.id.level_view);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        compass = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        gpsTracker = new GPSTracker(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(criteria, true);

        locationListener = new LocationListener();

        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        //Init bluetooth
        bt = BTController.getInstance();


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

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, compass, SensorManager.SENSOR_DELAY_GAME);
//        locationListener.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
        locationListener.stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_LOCATION) {
//            locationListener.start();
        }
    }

    float[] accelVals, compassVals;

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch(event.sensor.getType()){

            case Sensor.TYPE_ACCELEROMETER:
                accelVals = event.values;

                if(compassVals != null) {

                    float R[] = new float[9];
                    float I[] = new float[9];

                    if (SensorManager.getRotationMatrix(R, I, accelVals, compassVals)) {

                        float orientation[] = new float[3];
                        SensorManager.getOrientation(R, orientation);

                        float azimut = (float)(orientation[0] * 180 / Math.PI);
                        GeomagneticField geo = new GeomagneticField(32.5007f, 94.7405f, 113, System.currentTimeMillis());
                        float heading = azimut + geo.getDeclination();
                        arrowView.setAngle(-azimut);

                        //Tilt values [-1, 1] where 1 is a 45 degree tilt
                        float tiltY = (float)(4*orientation[1]/Math.PI);
                        float tiltX = (float)(4*orientation[2]/Math.PI);
                        levelView.set(-tiltX, -tiltY);
//                        System.out.println( tiltX + " " + tiltY);
                    }
                }
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                compassVals = event.values;
                break;

            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class LocationListener implements android.location.LocationListener{

        private boolean registered;

        public LocationListener(){
            registered = false;
        }

        public void start(){

            if(registered)
                return;

//            System.out.println("Start");

            boolean fine = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean coarse = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (fine && coarse) {
                locationManager.requestLocationUpdates(provider, GPS_TIME, GPS_MOVE, locationListener);
            }

            registered = true;
        }

        public void stop(){

//            System.out.println("Stop");

            locationManager.removeUpdates(locationListener);

            registered = false;
        }

        @Override
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            double alt = location.getAltitude();

            String latitude = new DecimalFormat("#.####").format(Math.abs(lat));
            latitude += ((lat >= 0) ? "N" : "S");
            latitudeText.setText(latitude);

            String longitude = new DecimalFormat("#.####").format(Math.abs(lon));
            longitude += ((lon >= 0) ? "E" : "W");
            longitudeText.setText(longitude);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    private void connect(){

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        //No bluetooth hardware
        if(btAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth not available on this device!", Toast.LENGTH_LONG).show();
            return;
        }

        //Bluetooth not enabled
        else {
            if (!btAdapter.isEnabled()) {
                Intent requestBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(requestBT, 1);
            }
        }

        //Notify when bluetooth state changed
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, filter);

        //Search for bluetooth devices
        Set<BluetoothDevice> devices = btAdapter.getBondedDevices();

        for(BluetoothDevice device : devices){

            if(device.getName().equals(RECEIVER_NAME)){

                Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();

                try {
                    bt.connect(device.getAddress());
                } catch (BTController.ConnectException e) {

                }

                if(bt.isConnected()) {
                    Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_LONG).show();
                    bt.start();
//                    bt.addDataListener(new DataListener() {
//                        @Override
//                        void onDataReceived(ArrayList<String> data) {
//
//                            for(String s : data){
//                                System.out.println(s);
//                            }
//
//                            System.out.println(data.size());
//
//                            if(data.size() == 2){
//                                System.out.println("Got data");
//                                latitudeText.setText(data.get(0));
//                                longitudeText.setText(data.get(1));
//                            }
//                        }
//                    });
                }
                else
                    Toast.makeText(getApplicationContext(), "Could not connect to device...", Toast.LENGTH_LONG).show();

                return;
            }
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {

                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {

                    case BluetoothAdapter.STATE_CONNECTED:
                        System.out.println("Bluetooth connected!");
                        Toast.makeText(NDBActivity.this, "Bluetooth Connected", Toast.LENGTH_SHORT).show();
                        break;

                    case BluetoothAdapter.STATE_DISCONNECTED:
                        System.out.println("Bluetooth disconnected!");
                        Toast.makeText(NDBActivity.this, "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };
}
