# AndArdBluetooth
Android and Arduino Bluetooth Communication
This project auto connects to a bluetooth device from Android 
Purposed for communicating with String values

# Usage
Class field	
 - Create Manager variable on class field

	---
	BlueToothManager mg;

Android - onCreate
 - Create object

	---
	mg = new BlueToothManager(this);

Android - onResume
 - connect to device 
 - register handler to handle message from device
	
	---
	mg.connectToDevice();
    mg.addMessageReceiveHandler(new BlueToothManager.BluetoothMessageHandler() {
        @Override
        public void onDataReceived(String data) {
            Log.i("Listener", "got:"+ data);
        }
    });

Android - onPause
 - disconnect from device and close stream and stop handler

 	---
 	mg.disconnect();

Android - other Methods
 - use after connect is accomplished

 	---
 	mg.sendData("hi");

