package ethanmcmike.ndb.utils;

import java.util.ArrayList;

public abstract class DataListener {
    public abstract void onDataReceived(byte[] data, int size);
}