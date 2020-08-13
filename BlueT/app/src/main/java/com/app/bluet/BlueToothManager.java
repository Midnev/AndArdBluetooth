package com.app.bluet;
/**
 * Created By Sungmin Hanyang Univ 2020
 */
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import java.util.UUID;

public class BlueToothManager {
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;
    private AppCompatActivity activity;

    private String targetAddress = "00:00:00:00:00:00";
    private String TAG = "BTManager";
    private UUID MY_UUID ;

    /**
     * call from onCreate
     */
    public BlueToothManager(AppCompatActivity activity) {
        this.activity = activity;
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//= UUID.randomUUID();
    }

    private String listenerEncoding = "UTF-8";//ASCII etc
    public void setListenerEncoding(String listenerEncoding){
        this.listenerEncoding= listenerEncoding;
    }

    public void setTargetAddress(String targetAddress) {
        this.targetAddress = targetAddress;
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
                //BluetoothDevice device1 = btAdapter.getRemoteDevice(targetAddress);
                //MY_UUID = device1.getUuids()[0].getUuid();


            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    /**
     * call from onResume
     */
    public void connectToDevice(){
        BluetoothDevice device = btAdapter.getRemoteDevice(targetAddress);
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e1) {
            Log.e(TAG, "Error creating Socket");
            return;
        }
        btAdapter.cancelDiscovery();
        Log.d(TAG, "...Connecting to : "+targetAddress);
        try {
            btSocket.connect();
            Log.d(TAG, "...Connection ok...");
        } catch (IOException e) {
            Log.e(TAG, "Error connecting Device: "+e.getMessage());
            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "Error closing Socket");

            }
            return;
        }
        Log.d(TAG, "...Created Socket...");

        try {
            outStream = btSocket.getOutputStream();
            inStream = btSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error setting output stream" );
        }
        checkBTState();
        if(listenerState)
            listenerThread.start();
    }

    /**
     * call from onPause
     */
    public void disconnect(){
        if (listenerThread != null) {
                listenerState = false;
                Log.i(TAG, "Stopping Listener" );
        }

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error flushing output stream" );
            }
        }
        try{
            btSocket.getInputStream().close();
            btSocket.getOutputStream().close();
            btSocket.close();
        } catch (IOException e2) {
            Log.e(TAG, "Error closing socket" );
        }

    }

    private void checkBTState() {
        if(btAdapter==null) {
            Log.e(TAG, "Bluetooth not supported" );
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    public void sendData(String message) {
        byte[] msgBuffer;
        try {
            //message += "\n";
            msgBuffer = message.getBytes();
        }catch (Exception e){
            Log.i(TAG,"failed to process Message ");
            msgBuffer = "0".getBytes();
        }
        Log.d(TAG, "...Send data: " + message);

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (targetAddress.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            Log.e(TAG, "Failed to write,m "+ msg);
        }
        Log.d(TAG, "data sent!");
    }


    private final byte delimiter = 10;
    private BlueToothManager.BluetoothMessageHandler bluetoothHandler;
    private Thread listenerThread;
    private boolean listenerState = false;
    private byte[] readBuffer;
    private int readBufferPosition;
    private Handler handler;
    private String result = "";
    public void addMessageReceiveHandler(final BlueToothManager.BluetoothMessageHandler bluetoothHandler){
        this.bluetoothHandler = bluetoothHandler;

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                bluetoothHandler.onDataReceived(result);

            }
        };

        listenerState = true;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        listenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()||listenerState){
                    try {
                        int bytesAvailable = inStream.available();
                        if (bytesAvailable >0){
                            byte[] packetBytes = new byte[bytesAvailable];
                            inStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++){
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, listenerEncoding);
                                    readBufferPosition = 0;
                                    result = data;
                                    handler.sendEmptyMessage(0);

                                    //bluetoothHandler.onDataReceived(data);
                                }else{
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }catch (Exception e){
                        listenerState = false;
                    }
                }
                Log.i(TAG,"Bluetooth Listener Stopped");
                listenerState = false;
            }
        });

//        listenerThread.start();
    }


    public interface BluetoothMessageHandler {
        void onDataReceived(String data);
    }


}
