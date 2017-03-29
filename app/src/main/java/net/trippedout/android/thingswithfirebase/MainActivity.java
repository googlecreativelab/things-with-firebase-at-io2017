package net.trippedout.android.thingswithfirebase;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.adcv2x.Adcv2x;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.trippedout.android.thingswithfirebase.listeners.SimpleValueListener;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupFirebase();
        setupThings();
    }

    private void setupFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("things/dial");

        // Read from the database
        myRef.addValueEventListener(new SimpleValueListener() {
            @Override
            public void onValue(DataSnapshot snap) {
                Log.d(TAG, "Key is: " + snap.getKey() + " Value is: " + snap.getValue());
            }

            @Override
            public void onChildValues(Iterable<DataSnapshot> children) {
                for (DataSnapshot snap : children) {
                    Log.d(TAG, "Key is: " + snap.getKey() + " Value is: " + snap.getValue());
                }
            }
        });
    }
    float currentVoltage = 0.f;

    private void setupThings() {
        PeripheralManagerService manager = new PeripheralManagerService();
        List<String> deviceList = manager.getI2cBusList();
        if (deviceList.isEmpty()) {
            Log.i(TAG, "No I2C bus available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + deviceList);
        }

        try {
            final Adcv2x adc = new Adcv2x("I2C1");
            adc.setRange(Adcv2x._4_096V);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        float v = adc.getResult(0);
                        float diff = currentVoltage - v;

                        if(Math.abs(diff) > .01f) {
                            Log.d(TAG, "voltage: " + v);

                            currentVoltage = v;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mHandler.postDelayed(this, 100);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
