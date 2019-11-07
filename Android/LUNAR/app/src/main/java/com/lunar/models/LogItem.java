package com.lunar.models;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogItem {

    public enum EventType{CONNECTED, DISCONNECTED, RECEIVED, SENT}

    public String time;
    public EventType event;

    public LogItem(EventType event){
        this.event = event;

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        time = format.format(new Date());
    }
}
