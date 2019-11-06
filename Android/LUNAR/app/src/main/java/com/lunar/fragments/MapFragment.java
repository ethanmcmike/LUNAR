package com.lunar.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback{

    private static final String ROCKET_NAME = "Rocket";
    private static final String PAYLOAD_NAME = "Payload";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static MapFragment instance;

    private static GoogleMap map;
    private static Marker rocketMarker, payloadMarker;
    private static MarkerOptions rocketMarkerOpt, payloadMarkerOpt;
    private static Polyline rocketPath;
    private static PolylineOptions rocketPathOpt, payloadPathOpt;
    private static ArrayList<LatLng> points;

    public static MapFragment newInstance(){

        instance = new MapFragment();

        //Setup rocket marker and path
        rocketMarkerOpt = new MarkerOptions();
        rocketMarkerOpt.title(ROCKET_NAME);
        rocketPathOpt = new PolylineOptions();
        rocketPathOpt.color(Color.RED);
        points = new ArrayList();

        return instance;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {

        this.map = map;

        //Add existing marker and path to map
        int numPoints = points.size();
        if(numPoints > 0){
            rocketMarker = map.addMarker(rocketMarkerOpt);
            rocketMarker.setPosition(points.get(numPoints-1));
            rocketPath = map.addPolyline(rocketPathOpt);
            rocketPath.setPoints(points);
        }

        //Map settings
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        map.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        map.setOnMyLocationClickListener(onMyLocationClickListener);
        enableMyLocationIfPermitted();

//        //Initialize Google Play Services
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(getContext(),
//                    Manifest.permission.ACCESS_FINE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED) {
//                //Location Permission already granted
//                buildGoogleApiClient();
//                map.setMyLocationEnabled(true);
//            } else {
//                //Request Location Permission
//                checkLocationPermission();
//            }
//        }
//        else {
//            buildGoogleApiClient();
//            map.setMyLocationEnabled(true);
//        }
    }

    public void moveRocket(float lat, float lon){
        points.add(new LatLng(lat, lon));
        update();
    }

    private void update(){

        int numPoints = points.size();

        //First point added
        if(numPoints == 1){
            rocketMarkerOpt.position(points.get(0));
            rocketMarker = map.addMarker(rocketMarkerOpt);
            rocketPath = map.addPolyline(rocketPathOpt);
        }

        else if(numPoints > 0){
            //Place marker on latest pos
            rocketMarker.setPosition(points.get(numPoints-1));
            //Add existing points to rocket path
            rocketPath.setPoints(points);
        }
    }

    private void enableMyLocationIfPermitted() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else if (map != null) {
            map.setMyLocationEnabled(true);
        }
    }

//    private void showDefaultLocation() {
//        Toast.makeText(getContext(), "Location permission not granted, " +
//                        "showing default location",
//                Toast.LENGTH_SHORT).show();
//        LatLng redmond = new LatLng(47.6739881, -122.121512);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(redmond));
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case LOCATION_PERMISSION_REQUEST_CODE: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    enableMyLocationIfPermitted();
//                } else {
//                    showDefaultLocation();
//                }
//                return;
//            }
//
//        }
//    }

    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
        new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                int numPoints = points.size();

                if(numPoints > 0){
                    LatLng pos = points.get(numPoints-1);
                    map.moveCamera(CameraUpdateFactory.newLatLng(pos));
                }

//                Toast.makeText(getContext(), "My location not added yet...", Toast.LENGTH_LONG).show();
                return false;
            }
        };

    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener =
            new GoogleMap.OnMyLocationClickListener() {
                @Override
                public void onMyLocationClick(@NonNull Location location) {

                    map.setMinZoomPreference(12);

                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(new LatLng(location.getLatitude(),
                            location.getLongitude()));

                    circleOptions.radius(200);
                    circleOptions.fillColor(Color.RED);
                    circleOptions.strokeWidth(6);

                    map.addCircle(circleOptions);
                }
            };


//    private static PolylineOptions path;
//
//    private GoogleMap map;
//    SupportMapFragment mapFrag;
//    LocationRequest mLocationRequest;
//    GoogleApiClient mGoogleApiClient;
//    Location mLastLocation;
//    Marker mCurrLocationMarker;
//
//    public static MapFragment newInstance() {
//
//        Bundle args = new Bundle();
//
//        MapFragment fragment = new MapFragment();
//        fragment.setArguments(args);
//
//        path = new PolylineOptions().color(Color.RED);
//
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle bundle) {
//        super.onCreate(bundle);
//
//        // Construct a GeoDataClient.
//        mGeoDataClient = Places.getGeoDataClient(this, null);
//
//        // Construct a PlaceDetectionClient.
//        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
//
//        // Construct a FusedLocationProviderClient.
//        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//    }
//
//    public void setMap(GoogleMap map) {
//        this.map = map;
//    }
//
//    public void addRocketCoord(LatLng coord) {
//
//        LatLng[] coords = {new LatLng(0, 10), new LatLng(10, 20), new LatLng(0, 30)};
//
//        map.addMarker(new MarkerOptions().position(coords[0]));
//
//        for (LatLng c : coords) {
//            path.add(c);
//        }
//
//        map.clear();
//        map.addPolyline(path);
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
////                ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        map.getUiSettings().setMyLocationButtonEnabled(true);
//        map.setMyLocationEnabled(true);
//
//        map.moveCamera(CameraUpdateFactory.newLatLng(coords[0]));
//    }
//
//    private void getDeviceLocation() {
//        /*
//         * Get the best and most recent location of the device, which may be null in rare
//         * cases when a location is not available.
//         */
//        try {
//            if (hasLocationPermission()) {
//                fusedLocationClient
//                        .getLastLocation()
//                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                            @Override
//                            public void onSuccess(Location location) {
//                                // Got last known location. In some rare situations this can be null.
//                                if (location != null) {
//                                    lastLocation = location;
//                                    // Logic to handle location object
//                                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                                            new LatLng(location.getLatitude(),
//                                                    location.getLongitude()), DEFAULT_ZOOM));
//                                    map.setOnMyLocationButtonClickListener(MapsActivity.this);
//                                    map.setMyLocationEnabled(true);
//                                    map.getUiSettings().setMyLocationButtonEnabled(true);
//                                } else {
//                                    map.setMyLocationEnabled(false);
//                                    map.getUiSettings().setMyLocationButtonEnabled(false);
//                                }
//                            }
//                        });
//            }
//        } catch (SecurityException e) {
//            Log.e("Exception: %s", e.getMessage());
//        }
//    }
//

//    GoogleApiClient mGoogleApiClient;

//    protected synchronized void buildGoogleApiClient() {
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//        mGoogleApiClient.connect();
//    }

//    @Override
//    public void onConnected(Bundle bundle) {
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(1000);
//        mLocationRequest.setFastestInterval(1000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {}
//
//    @Override
//    public void onLocationChanged(Location location)
//    {
//        mLastLocation = location;
//        if (mCurrLocationMarker != null) {
//            mCurrLocationMarker.remove();
//        }
//
//        //Place current location marker
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Current Position");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//        mCurrLocationMarker = map.addMarker(markerOptions);
//
//        //move map camera
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));
//
//    }
//
//    @Override
//    public void onStatusChanged(String s, int i, Bundle bundle) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String s) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String s) {
//
//    }
//

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // location-related task you need to do.
//                    if (ContextCompat.checkSelfPermission(getContext(),
//                            Manifest.permission.ACCESS_FINE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED) {
//
//                        if (mGoogleApiClient == null) {
//                            buildGoogleApiClient();
//                        }
//                        map.setMyLocationEnabled(true);
//                    }
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    Toast.makeText(getContext(), "permission denied", Toast.LENGTH_LONG).show();
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//    }
}
