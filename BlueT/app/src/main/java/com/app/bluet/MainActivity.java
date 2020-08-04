package com.app.bluet;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    BlueToothManager mg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mg = new BlueToothManager(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mg.connectToDevice();
        mg.addMessageReceiveHandler(new BlueToothManager.BluetoothMessageHandler() {
            @Override
            public void onDataRecvied(String data) {
                Log.i("Listener", "got:"+ data);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mg.disconnect();
    }

    public void snd(View v){
        mg.sendData("hi");
    }


}


