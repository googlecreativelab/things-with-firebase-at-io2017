package net.trippedout.android.thingswithfirebase;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.things.contrib.driver.adcv2x.Adcv2x;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.FirebaseDatabase;

import net.trippedout.android.thingswithfirebase.util.AdcWrapper;

import java.io.IOException;
import java.util.ArrayList;
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

    private Handler mHandler = new Handler();

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    private List<AutoCloseable> mCloseableThings = new ArrayList<>();
    private List<AdcWrapper> mAnalogDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupThings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if(mCloseableThings.size() > 0) {
                for(AutoCloseable thing : mCloseableThings) {
                    thing.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupThings() {
        try {
            // Query which I2C buses are attached, but typically its only the default.
            PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
            Log.d(TAG, "Available I2C Buses: " + peripheralManagerService.getI2cBusList());

            // Create the driver for the two ADC boards we've connected
            final Adcv2x upperAdc = new Adcv2x(Adcv2x.DEFAULT_BUS, Adcv2x.I2C_ADDRESS_48);
            final Adcv2x lowerAdc = new Adcv2x(Adcv2x.DEFAULT_BUS, Adcv2x.I2C_ADDRESS_49);
            mCloseableThings.add(upperAdc);
            mCloseableThings.add(lowerAdc);

            // Add all our individual devices to our read array
            mAnalogDevices.add(new AdcWrapper(upperAdc, 0, "speed"));
            mAnalogDevices.add(new AdcWrapper(upperAdc, 1, "dieSpeed"));
            mAnalogDevices.add(new AdcWrapper(lowerAdc, 1, "radius"));
            mAnalogDevices.add(new AdcWrapper(lowerAdc, 2, "curlSize"));
            mAnalogDevices.add(new AdcWrapper(lowerAdc, 3, "attraction"));

            // loop through reads
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        updateAnalogDevices();
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

    private void updateAnalogDevices() throws IOException {
        for(AdcWrapper device : mAnalogDevices) {
            float v = device.update();
            float diff = device.getCurrentVoltage() - v;

            if(Math.abs(diff) > MIN_DIFFERENCE_IN_VOLTAGE) {
                device.setVoltage(v);
                updateVoltageInDatabase(device);
            }
        }
    }

    private void updateVoltageInDatabase(AdcWrapper device) {
        Log.d(TAG, "updateVoltage: " + device);
        mDatabase
                .getReference("things/" + device.getName())
                .setValue(device.getNormalized(MAX_VOLTAGE_FROM_POT));
    }
}
