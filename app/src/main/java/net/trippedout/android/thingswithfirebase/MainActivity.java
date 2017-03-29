package net.trippedout.android.thingswithfirebase;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.trippedout.android.thingswithfirebase.drivers.SparkfunADCV20;
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

    private void setupThings() {
        PeripheralManagerService manager = new PeripheralManagerService();
        List<String> deviceList = manager.getI2cBusList();
        if (deviceList.isEmpty()) {
            Log.i(TAG, "No I2C bus available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + deviceList);
        }

        try {
            final SparkfunADCV20 adc = new SparkfunADCV20("I2C1");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "channel: " + adc.getResult(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mHandler.postDelayed(this, 250);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
