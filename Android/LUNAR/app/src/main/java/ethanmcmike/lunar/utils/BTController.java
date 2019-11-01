package ethanmcmike.ndb.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BTController implements Runnable{

    private static BTController instance;

    BluetoothAdapter adapter;

    public String address;
    public BluetoothSocket socket;
    public OutputStream os;
    public InputStream is;

    private Thread thread;
    private boolean running;
    private Set<DataListener> dataListeners;

    public BTController(){
        instance = this;
        adapter = BluetoothAdapter.getDefaultAdapter();
        dataListeners = new HashSet();
    }

    public static BTController getInstance(){
        if(instance == null)
            instance = new BTController();

        return instance;
    }

    public void connect(String name) throws ConnectException{

        this.address = address;

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        //No bluetooth hardware
        if(btAdapter == null) {
            throw new ConnectException("Bluetooth not available on this device", ConnectException.ErrorType.HARDWARE);
        }

        //Bluetooth not enabled
        else if(!btAdapter.isEnabled()){
            throw new ConnectException("Bluetooth is not enabled", ConnectException.ErrorType.DISABLED);
        }

        Set<BluetoothDevice> devices = adapter.getBondedDevices();

        for(BluetoothDevice device : devices){

            if(device.getName().equals(name)){

                try {
                    UUID myUUID = UUID.fromString(device.getUuids()[0].toString());
                    socket = device.createRfcommSocketToServiceRecord(myUUID);
                    socket.connect();

                    os = socket.getOutputStream();
                    is = socket.getInputStream();

                    start();

                    return;

                } catch(IOException e){

                    throw new ConnectException("Could not connect to device", ConnectException.ErrorType.CONNECT);
                }
            }
        }

        throw new ConnectException("Could not find " + address, ConnectException.ErrorType.NAME);
    }

    public boolean isConnected(){
        if(socket == null)
            return false;
        return socket.isConnected();
    }

    public void send(int val){
        if(os != null) {
            try {
                os.write(val);
            } catch (IOException e) {

            }
        }
    }

    public void send(int[] data){

        byte[] msg = new byte[data.length];
        for(int i=0; i<data.length; i++){
            msg[i] = intToByte(data[i]);
        }

        if(os != null) {
            try {
                os.write(msg);
                os.flush();
                System.out.println("Wrote " + msg);
            } catch (IOException e) {
                System.out.println("Could not write to OS!");
            }
        }
    }

    public void send(int[] data, int off, int len){

        byte[] msg = new byte[data.length];
        for(int i=0; i<data.length; i++){
            msg[i] = intToByte(data[i]);
        }

        if(os != null) {
            try {
                os.write(msg, off, len);
            } catch (IOException e) {

            }
        }
    }

//    public int read(){
//        if(is != null) {
//            try {
//                return is.read(buffer, 0, buffer.length);
//
//            } catch (IOException e) {
//
//            }
//        }
//
//        return 0;
//    }

    public void close(){
        try {
            os.close();
            is.close();
            socket.close();

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static int byteToInt(byte b){
        return b & 0xFF;
    }

    public static byte intToByte(int i){
        return (byte)i;
    }

    public void start(){
        thread = new Thread(this);
        thread.start();
    }

    public void stop(){
        running = false;
    }

    @Override
    public void run() {

        running = true;
        byte[] buffer = new byte[256];

        while(running){

            try {

                if(is.available() > 0){

                    int size = is.read(buffer);

                    for(DataListener listener : dataListeners){
                        listener.onDataReceived(buffer, size);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addDataListener(DataListener dataListener){
        dataListeners.add(dataListener);
    }

    public static class ConnectException extends Exception{

        public enum ErrorType{HARDWARE, DISABLED, NAME, CONNECT};

        public ErrorType type;

        public ConnectException(String msg, ErrorType type){
            super(msg);
            this.type = type;
        }
    }

//    private class DataReceiver extends DataListener{
//
//        byte[] buffer = new byte[256];
//        char start = '[';
//        char end = ']';
//        String msg = "";
//
//        @Override
//        public void onDataReceived(byte[] data) {
//
//            while(running){
//
//                try {
//
//                    if(is.available() > 0){
//
//                        int size = is.read(buffer, 0, 256);
//
//                        for(int i=0; i<size; i++){
//
//                            char c = (char)buffer[i];
//
//                            if(c == start){
//                                msg = "";
//                            }
//                            else if(c == end){
//
//                                String[] chunks = msg.split(",");
//
//                                int num = Integer.valueOf(chunks[0]);
//
//                                if(chunks.length-1 == num){
//
//                                    ArrayList<String> data = new ArrayList();
//
//                                    for(int j=1; j<chunks.length; j++){
//                                        data.add(chunks[j]);
//                                    }
//
//                                    for(DataListener listener : dataListeners) {
//                                        try {
//                                            listener.onDataReceived(data);
//                                        } catch (NullPointerException e) {
//
//                                        }
//                                    }
//                                }
//                            }
//                            else{
//                                msg += c;
//                            }
//                        }
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
}


