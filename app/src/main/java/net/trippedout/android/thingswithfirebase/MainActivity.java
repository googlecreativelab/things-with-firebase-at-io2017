package net.trippedout.android.thingswithfirebase;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.things.contrib.driver.adcv2x.Adcv2x;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.trippedout.android.thingswithfirebase.listeners.SimpleValueListener;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The amount of time between reading ADC on {@link Adcv2x} so we don't have to
     * update our database too much.
     */
    private static final long DELAY_CHECK_MS = 250;

    /**
     * So we don't constantly update the database, only update if it goes over a set amount.
     *
     * .4 is arbitrary - seems voltage sometimes spiked ~.03 every now and then for no reason.
     */
    private static final float MIN_DIFFERENCE_IN_VOLTAGE = .04f;

    /**
     * We know that we are feeding the pot 3.3v but the readout tends to max out at 3.2 when
     * range is set above 2048.
     */
    private static final float MAX_VOLTAGE_FROM_POT = 3.2f;

    private float currentVoltage = 0.f;

    private Handler mHandler = new Handler();

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDialRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupFirebase();
        setupThings();
    }

    private void setupFirebase() {
        //TODO auth doesnt work yet with Things
//        mAuth = FirebaseAuth.getInstance();
//        mAuth.signInAnonymously().addOnCompleteListener(this);

        mDatabase = FirebaseDatabase.getInstance();
        mDialRef = mDatabase.getReference("things/dial");

        // Read from the database
        mDialRef.addValueEventListener(new SimpleValueListener() {
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
            final Adcv2x adc = new Adcv2x("I2C1");

            // set the range here to 4 volts even tho we know we're only feeding it ~3.3
            // the dial turned all the way up will read ~3.2
            adc.setRange(Adcv2x._4_096V);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        float v = adc.getResult(0);
                        float diff = currentVoltage - v;

                        // only update database if we're beyond the previous difference
                        if(Math.abs(diff) > MIN_DIFFERENCE_IN_VOLTAGE) {
                            currentVoltage = v;
                            updateVoltageInDatabase(currentVoltage);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mHandler.postDelayed(this, DELAY_CHECK_MS);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateVoltageInDatabase(float currentVoltage) {
        Log.d(TAG, "updateVoltageInDatabase: " + currentVoltage);

        float percentage = currentVoltage / MAX_VOLTAGE_FROM_POT;
        if(percentage < 0.f) percentage = 0.f;
        if(percentage > 1.f) percentage = 1.f;

        // This will work for testing since we have database open without auth <- BAD
        mDialRef.setValue(percentage);
    }

//    @Override
//    public void onComplete(@NonNull Task<AuthResult> task) {
//        Log.d(TAG,"onComplete: " + task.getResult());
//    }
}
