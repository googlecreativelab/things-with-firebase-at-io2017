package net.trippedout.android.thingswithfirebase.drivers;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

/**
 * SensorDriver implementation of the {@link SparkfunADCV20 ADC V20 Module}
 */

public class SparkfunADCV20SensorDriver implements AutoCloseable {
    private static final String TAG = SparkfunADCV20SensorDriver.class.getSimpleName();
    private static final String DRIVER_NAME = "SparkfunADCV20";
    private static final String DRIVER_VENDOR = "Sparkfun";
    private static final int DRIVER_MIN_DELAY_US = Math.round(1000000.f/120.f);
    private static final int DRIVER_MAX_DELAY_US = Math.round(1000000.f/1.f);
    private static final int DRIVER_VERSION = 1;
    private SparkfunADCV20 mDevice;
    private UserSensor mUserSensor;

    public SparkfunADCV20SensorDriver(String bus) throws IOException {
        mDevice = new SparkfunADCV20(bus);
    }

    @Override
    public void close() throws Exception {
        unregister();
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    /**
     * Register the driver in the framework.
     * @see #unregister()
     */
    public void register() {
        if (mDevice == null) {
            throw new IllegalStateException("cannot registered closed driver");
        }
        if (mUserSensor == null) {
            mUserSensor = build(mDevice);
            UserDriverManager.getManager().registerSensor(mUserSensor);
        }
    }

    /**
     * Unregister the driver from the framework.
     */
    public void unregister() {
        if (mUserSensor != null) {
            UserDriverManager.getManager().unregisterSensor(mUserSensor);
            mUserSensor = null;
        }
    }

    static UserSensor build(final SparkfunADCV20 adc) {
        return UserSensor.builder()
                .setCustomType(
                        Sensor.TYPE_DEVICE_PRIVATE_BASE,
                        "com.sparkfun.adcv20",
                        Sensor.REPORTING_MODE_CONTINUOUS
                )
                .setName(DRIVER_NAME)
                .setVendor(DRIVER_VENDOR)
                .setVersion(DRIVER_VERSION)
                .setMinDelay(DRIVER_MIN_DELAY_US)
                .setMaxDelay(DRIVER_MAX_DELAY_US)
                .setUuid(UUID.randomUUID())
                .setDriver(new UserSensorDriver() {
                    @Override
                    public UserSensorReading read() throws IOException {

                        // TODO - read all 3 channels immediately in order and use?
                        float[] result = new float[1];
                        result[0] = adc.getResult(0);
                        return new UserSensorReading(
                                result,
                                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM // 120Hz
                        );
                    }

                    @Override
                    public void setEnabled(boolean enabled) throws IOException {
                        // nothing in datasheet spec to handle this
                    }
                })
                .build();
    }

}
