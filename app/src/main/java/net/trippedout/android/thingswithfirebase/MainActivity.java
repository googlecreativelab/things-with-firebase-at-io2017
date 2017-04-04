package net.trippedout.android.thingswithfirebase;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.adcv2x.Adcv2x;
import com.google.android.things.contrib.driver.button.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.trippedout.android.thingswithfirebase.listeners.SimpleValueListener;

import java.io.IOException;

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
    private DatabaseReference mDialRef, mPrevRef, mNextRef;
    private Button mPrevButton, mNextButton;
    private Adcv2x mAdc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupFirebase();
        setupThings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if(mNextButton != null) {
                mNextButton.close();
            }

            if(mAdc != null) {
                mAdc.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupFirebase() {
        //TODO auth doesnt work yet with Things
//        mAuth = FirebaseAuth.getInstance();
//        mAuth.signInAnonymously().addOnCompleteListener(this);

        mDatabase = FirebaseDatabase.getInstance();
        mDialRef = mDatabase.getReference("things/dial");
        mPrevRef = mDatabase.getReference("things/prev");
        mNextRef = mDatabase.getReference("things/next");
    }

    private void setupThings() {
        setupButtons();
        setupDial();
    }

    private void setupButtons() {
        try {
            mPrevButton = new Button("GP14", Button.LogicState.PRESSED_WHEN_HIGH);
            mPrevButton.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean b) {
                    mPrevRef.setValue(b);
                }
            });

            mNextButton = new Button("GP15", Button.LogicState.PRESSED_WHEN_HIGH);
            mNextButton.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean b) {
                    mNextRef.setValue(b);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupDial() {
        try {
            // Create the driver for our ADC board, needed for the analog signal from our pot
            mAdc = new Adcv2x("I2C1");

            // set the range here to 4 volts even tho we know we're only feeding it ~3.3
            // the dial turned all the way up will read ~3.2
            mAdc.setRange(Adcv2x._4_096V);

            // loop through reads
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        float v = mAdc.getResult(0);
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
