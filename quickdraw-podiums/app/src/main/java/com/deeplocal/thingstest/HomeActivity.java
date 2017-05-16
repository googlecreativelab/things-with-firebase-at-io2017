package com.deeplocal.thingstest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import com.google.android.things.pio.UartDevice;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class HomeActivity extends Activity {

    private static final String TAG = "HomeActivity";

    String whichPlayer;
    String deviceIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        setContentView(R.layout.activity_home);

        deviceIP = getLocalIpAddress();

        //set the player based on the jumper pins on the Raspberry Pi
        whichPlayer = "";
        try {
            whichPlayer = checkDevicePins();
        } catch (Exception e) {

        }

        try {

            final UartDevice uart = new PeripheralManagerService().openUartDevice("UART0");

            uart.setBaudrate(9600);
            uart.setDataSize(8);
            uart.setParity(UartDevice.PARITY_NONE);
            uart.setStopBits(1);

            //we have a few different refs here to make things easy, this can be streamlined            
            DatabaseReference dbRef = database.getReference("io");
            DatabaseReference stateRef = database.getReference("io/triggers/player" + whichPlayer + "/state");
            final DatabaseReference ipRef = database.getReference("io/triggers/player" + whichPlayer + "/ip");
            final DatabaseReference heartbeatRef = database.getReference("io/triggers/player" + whichPlayer + "/heartbeat");

            //update our UP and "heartbeat" on a timer.  This is just useful for debugging / making sure the Pi's are talking to 
            //firebase
            heartbeatRef.setValue(new Date());
            ipRef.setValue(deviceIP);

              
            
            //set our initial value and start listening for firebase changes
            stateRef.setValue("0");
            stateRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String value = dataSnapshot.getValue(String.class);

                    if (value == null) {
                        return;
                    }

                    //massage some data -- tie is the same as a win in terms of what to display
                    if (value.equals("tie")) {
                        value = "win";
                    }

                    //change the c in correct to C so that we can differentiate it from 'countdown' on the pi
                    if (value.equals("correct")) {
                        value = "Correct";
                    }

                    //we only need to send the first letter over serial
                    byte[] bytes = value.getBytes();
                    byte[] onebyte = new byte[1];
                    if (bytes.length > 0) {
                        onebyte[0] = bytes[0];
                    }

                    try {
                        int count = uart.write(onebyte, onebyte.length);
                        Log.d(TAG, "Wrote " + count + " bytes to peripheral");
                    } catch (Exception e) {
                        Log.d("HEY", "UART Write Exception: " + e);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

        } catch (Exception e) {
            Log.d("HEY", "UART ISSUE");
        }

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = inetAddress.getHostAddress().toString();
                        return ip;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }


    //we use these pins on the Pi as jumpers to decide which podium (1, 2, or 3) we are
    public String checkDevicePins() throws IOException {

        Gpio pin1, pin2, pin3;

        PeripheralManagerService manager = new PeripheralManagerService();
        pin1 = manager.openGpio("BCM21");
        pin2 = manager.openGpio("BCM20");
        pin3 = manager.openGpio("BCM16");


        pin1.setDirection(Gpio.DIRECTION_IN);
        pin1.setActiveType(Gpio.ACTIVE_HIGH);

        pin2.setDirection(Gpio.DIRECTION_IN);
        pin2.setActiveType(Gpio.ACTIVE_HIGH);

        pin3.setDirection(Gpio.DIRECTION_IN);
        pin3.setActiveType(Gpio.ACTIVE_HIGH);


        if (pin1.getValue()) {
            return "1";
        }

        if (pin2.getValue()) {
            return "2";
        }

        if (pin3.getValue()) {
            return "3";
        }

        Log.e(TAG, "No Jumper Set");
        return "1";
    }


}
