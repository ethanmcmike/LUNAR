package com.lunar.models;

import com.lunar.R;
import com.lunar.views.AircraftInfoView;

public class Aircraft {

    public String title;
    public int address;

    public float lat, lon;
    public int altitude;
    public float temp;

    public AircraftInfoView view;

    public Aircraft(String title, int address){
        this.title = title;
        this.address = address;
    }

    public void set(int alt, float temp, float lat, float lon){
        this.altitude = alt;
        this.temp = temp;
        this.lat = lat;
        this.lon = lon;
    }

    public String getAlt(){
        return String.valueOf(altitude);
    }

    public int getAltUnit(){
        return R.string.meters;
    }

    public String getTemp(){
        return String.valueOf(temp);
    }

    public int getTempUnit(){
        return R.string.celcius;
    }

    public String getLat(){
        return String.valueOf(lat);
    }

    public int getLatUnit(){
        return (lat > 0) ? R.string.north : R.string.south;
    }

    public String getLon(){
        return String.valueOf(lon);
    }

    public int getLonUnit(){
        return (lon > 0) ? R.string.east : R.string.west;
    }
}
