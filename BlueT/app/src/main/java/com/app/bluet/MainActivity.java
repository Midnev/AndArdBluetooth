package com.app.bluet;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    BlueToothManager mg;
    BlueToothManager.BluetoothMessageHandler handler;
    TextView v1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mg = new BlueToothManager(this);
        mg.setTargetAddress("98:D3:32:30:72:FE");
        v1= findViewById(R.id.view1) ;

        handler = new BlueToothManager.BluetoothMessageHandler() {
            @Override
            public void onDataReceived(String data) {
                v1.setText(data);
                Log.i("Listener", "got:"+ data);
            }
        };
        mg.addMessageReceiveHandler(handler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //and connect to device
        mg.connectToDevice();


    }

    @Override
    protected void onPause() {
        super.onPause();
        //disconnect on pause

    }

    @Override
    protected void onStop() {
        super.onStop();
        mg.disconnect();
    }





    public void snd(View v){
        //send data String
        mg.sendData("hi");
    }


}


