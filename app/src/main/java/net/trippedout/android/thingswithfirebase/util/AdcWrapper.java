package net.trippedout.android.thingswithfirebase.util;

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

        return mNormalized;
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
