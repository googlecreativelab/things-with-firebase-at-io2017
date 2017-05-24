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
package com.androidexperiments.thingscontroller.util;

import com.google.android.things.contrib.driver.adcv2x.Adcv2x;

import java.io.IOException;

/**
 * Simple wrapper to help enumerate through ADC devices. Helps opening specific channels
 * and associating with proper Firebase path.
 */

public class AdcWrapper {
    /**
     * Reference to our device driver, so we can get results from that driver directly.
     */
    private final Adcv2x mDevice;

    /**
     * Channel number as it corresponds to the board we're plugged in to.
     */
    private final int mChannel;

    /**
     * Optional name for this device, mainly used for corresponding Firebase database updates.
     */
    private final String mName;

    private float mCurrentVoltage, mLastVoltage, mNormalized;

    public AdcWrapper(Adcv2x controller, int channel, String name) {
        this.mDevice = controller;
        this.mChannel = channel;
        this.mName = name;
    }

    public float update() throws IOException {
        mLastVoltage = mDevice.getResult(mChannel);
        return mLastVoltage;
    }

    public void setVoltage(float voltage) {
        mCurrentVoltage = voltage;
    }

    public float getNormalized(float maxVoltage) {
        mNormalized = mCurrentVoltage / maxVoltage;
        if(mNormalized < 0.f) mNormalized = 0.f;
        if(mNormalized > 1.f) mNormalized = 1.f;

        return 1.0f - mNormalized;
    }

    public Adcv2x getController() {
        return mDevice;
    }

    public int getChannel() {
        return mChannel;
    }

    public String getName() {
        return mName;
    }

    public float getCurrentVoltage() {
        return mCurrentVoltage;
    }

    @Override
    public String toString() {
        return "AnalogDevice{" +
                "mDevice=" + mDevice +
                ", mChannel=" + mChannel +
                ", mName='" + mName + '\'' +
                ", mCurrentVoltage=" + mCurrentVoltage +
                '}';
    }
}
