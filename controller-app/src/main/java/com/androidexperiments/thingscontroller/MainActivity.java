/**
 * Copyright 2017 Google, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.androidexperiments.thingscontroller;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.things.contrib.driver.adcv2x.Adcv2x;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.FirebaseDatabase;

import com.androidexperiments.thingscontroller.util.AdcWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The amount of time between reading ADC on {@link Adcv2x} so we don't have to
     * update our database too much.
     */
    private static final long DELAY_CHECK_MS = 150;

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
        // Query which I2C buses are attached, but typically its only the default.
        // This is just for visibility, we get our devices below
        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        Log.d(TAG, "Available I2C Buses: " + peripheralManagerService.getI2cBusList());

        // Handle the addition of all three boards separately, so different testing environments
        // don't break the whole project. ie one of our devs only had the bottom board to work with
        // Altho note - if a channel is open below and not connected to any sliders or dials, it
        // will fluctuate wildly around 1.5v, sending constant updates to you FB database.
        try {
            final Adcv2x upperAdc = new Adcv2x(Adcv2x.DEFAULT_BUS, Adcv2x.I2C_ADDRESS_48);
            mCloseableThings.add(upperAdc);

            mAnalogDevices.add(new AdcWrapper(upperAdc, 3, "curlSize"));
            mAnalogDevices.add(new AdcWrapper(upperAdc, 2, "radius"));
            mAnalogDevices.add(new AdcWrapper(upperAdc, 1, "dieSpeed"));
            mAnalogDevices.add(new AdcWrapper(upperAdc, 0, "speed"));
        } catch (IOException e) {
            handleAdcIOException(e, "I2C_ADDRESS_48 : upper");
        }

        try {
            final Adcv2x middleAdc = new Adcv2x(Adcv2x.DEFAULT_BUS, Adcv2x.I2C_ADDRESS_49);
            mCloseableThings.add(middleAdc);

            mAnalogDevices.add(new AdcWrapper(middleAdc, 2, "motionMultiplier"));
            mAnalogDevices.add(new AdcWrapper(middleAdc, 0, "attraction"));
        } catch (IOException e) {
            handleAdcIOException(e, "I2C_ADDRESS_49 : middle");
        }

        try {
            final Adcv2x lowerAdc = new Adcv2x(Adcv2x.DEFAULT_BUS, Adcv2x.I2C_ADDRESS_4A);
            mCloseableThings.add(lowerAdc);

            mAnalogDevices.add(new AdcWrapper(lowerAdc, 3, "shadowDarkness"));
            mAnalogDevices.add(new AdcWrapper(lowerAdc, 2, "bgColor"));
            mAnalogDevices.add(new AdcWrapper(lowerAdc, 1, "color1"));
            mAnalogDevices.add(new AdcWrapper(lowerAdc, 0, "color2"));
        } catch (IOException e) {
            handleAdcIOException(e, "I2C_ADDRESS_4A : lower");
        }

        // loop through all available devices and read
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
    }

    private void handleAdcIOException(IOException e, String which) {
        Log.e(TAG, "handleAdcIOException on: " + which + " e: " + e.getMessage());
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
