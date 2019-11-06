package com.lunar.utils;

public abstract class DataListener {
    public abstract void onDataReceived(byte[] data, int size);
}