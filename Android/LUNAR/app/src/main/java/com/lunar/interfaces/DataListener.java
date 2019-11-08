package com.lunar.interfaces;

public interface DataListener {
    void onDataReceived(byte[] data, int size);
}