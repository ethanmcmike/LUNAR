package com.lunar.models;

import java.util.Date;

public class InputData {

    public Date date;
    public int altitude;
    public float temp;
    public float lat, lon;

    public InputData(int altitude, float temp, float lat, float lon){
        this.altitude = altitude;
        this.temp = temp;
        this.lat = lat;
        this.lon = lon;
    }
}
